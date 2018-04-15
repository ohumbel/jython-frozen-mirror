package org.python.antlr;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Ignore;
import org.junit.Test;

public class Python27ParserTest {

	private static final String CPYTHON_ROOT = "/Users/oti/stuff/gitrepo/python/cpython";
	private static final String JYTHON_ROOT = "/Users/oti/stuff/gitrepo/ohumbel/jythontools/jython";

	@Test
	@Ignore
	public void testParseSingleFile() throws FileNotFoundException {
		Path file = Paths.get(JYTHON_ROOT, "lib-python/2.7/test/test_io.py");
		assertParseable(file);
	}

	@Test
	@Ignore
	public void testParseWholeCPython27Library() throws IOException {
		parseDirectory(CPYTHON_ROOT, loadExpectedFailures("CPython27.max.failing.files.properties"));
	}

	@Test
	//@Ignore
	public void testParseWholeJython27Library() throws IOException {
		parseDirectory(JYTHON_ROOT, loadExpectedFailures("Jython27.max.failing.files.properties"));
	}

	@Test
	public void testParseSimpleInput() {
		StringBuilder b = new StringBuilder();
		b.append("def test():\n");
		b.append("    d = Dbm('@dbm', 'rw', 0600)\n");
		b.append("    for obj in (123, 123.45, 123L):\n");
		b.append("        self.assertEqual(unicode(obj), unicode(str(obj)))\n");
		b.append("    for obj in (123, 123.45, 123l):\n");
		b.append("        self.assertEqual(unicode(obj), unicode(str(obj)))\n");
		b.append("    self.crc = zlib.crc32('') & 0xffffffffL\n");
		b.append("    os.chmod(tempname, statbuf[ST_MODE] & 07777)\n");
		b.append("    self.assertEqual(2147483647, 017777777777)\n");
		b.append("    self.assertEqual(2147483647, 0o17777777777)\n");
		b.append("    x = 077777777777777777L\n");
		b.append("    x = 077777777777777777l\n");
		b.append("    big = 012345670123456701234567012345670L  # 32 octal digits\n");
		b.append("    tarinfo.uid = 04000000000000000000L\n");
		assertParseable(CharStreams.fromString(b.toString()));
	}

	@Test
	public void testInvalidInput() {
		StringBuilder b;

		b = new StringBuilder();
		// the '*' as single argument is not allowed
		b.append("def fetch_zip(commit_hash, zip_dir, *, org='python', binary=False, verbose):\n");
		b.append("    pass\n");
		assertNotParseable(b);

		b = new StringBuilder();
		// the '*' in front of state is not allowed
		b.append("for *state, val in items:\n");
		b.append("    pass\n");
		assertNotParseable(b);

		b = new StringBuilder();
		// the '.False' is unknown at the moment (test_xmlrpclib.py)
		b.append("b = xmlrpclib.False\n");
		assertNotParseable(b);
	}

	private static Set<String> loadExpectedFailures(String failurePropertiesFileName) throws IOException {
		Properties properties = new Properties();
		Path maxFailures = Paths.get(JYTHON_ROOT, "grammar/".concat(failurePropertiesFileName));
		try (InputStream inputStream = Files.newInputStream(maxFailures)) {
			properties.load(inputStream);
		}
		Set<String> expecedFailures = new HashSet<>();
		for (Object key : properties.keySet()) {
			assertTrue(key instanceof String);
			expecedFailures.add((String) key);
		}
		return expecedFailures;
	}

	private static void parseDirectory(String root, Set<String> expectedFailures) throws IOException {
		Path directory = Paths.get(root);
		ParseFiles pf = new ParseFiles();
		Files.walkFileTree(directory, pf);
		List<String> failedFiles = pf.getFailedFiles();
		if (!failedFiles.isEmpty()) {
			System.out.println("\n---Failures:");
		}
		failedFiles.stream().sorted().forEach(failedFile -> failureOutput(root, failedFile));

		int numberOfFailedFiles = failedFiles.size();
		int expectedNumberOfFailedFiles = expectedFailures.size();
		assertTrue("too much failing files: " + numberOfFailedFiles + " instead of " + expectedNumberOfFailedFiles,
				numberOfFailedFiles <= expectedNumberOfFailedFiles);
	}

	private static void failureOutput(String root, String failedFile) {
		System.out.println(failedFile.replace(root + "/", "").concat("=\"failure\""));
	}

	private static void assertParseable(Path file) throws FileNotFoundException {
		try (InputStream inputStream = Files.newInputStream(file)) {
			System.out.println(System.out.format("parsing file: %s ", file));
			assertParseable(CharStreams.fromStream(inputStream));
		} catch (IOException e) {
			throw new FileNotFoundException(file.toString());
		}

	}

	private static void assertParseable(CharStream input) {
		Python27Lexer lexer = new Python27Lexer(input);
		DescriptiveBailErrorListener errorListener = new DescriptiveBailErrorListener();
		lexer.addErrorListener(errorListener);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		Python27Parser parser = new Python27Parser(tokens);
		parser.addErrorListener(errorListener);
		ParseTree tree = parser.file_input();
		assertNotNull(tree);
	}

	private static void assertNotParseable(StringBuilder builder) {
		try {
			assertParseable(CharStreams.fromString(builder.toString()));
			fail("RuntimeException expected");
		} catch (RuntimeException e) { // expected
		}
	}

	private static final class ParseFiles extends SimpleFileVisitor<Path> {
		private final List<String> failedFiles = new ArrayList<>();

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
			if (canParse(file, attr)) {
				try {
					assertParseable(file);
				} catch (Exception e) {
					failedFiles.add(file.toString());
				}
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
			return FileVisitResult.CONTINUE;
		}

		public List<String> getFailedFiles() {
			return failedFiles;
		}

		private boolean canParse(Path file, BasicFileAttributes attr) {
			boolean canParse = false;
			String fileName = file.toString();
			if (attr.isRegularFile() && fileName.endsWith(".py")) {
				canParse = true;
				// and now all the exclusions:
				if (is_not_a_pure_2_nor_3_grammar(fileName) || print_is_already_a_function(fileName)
						|| star_args(fileName) || bad_syntax(fileName) || unknown(fileName)
						|| isBuildOutput(fileName)) {
					canParse = false;
				}
			}
			return canParse;
		}

		private boolean is_not_a_pure_2_nor_3_grammar(String name) {
			return name.contains("/test2to3/") || name.contains("/lib2to3/");
		}

		/**
		 * But what about {@code from __future__ import print_function} ?
		 */
		private boolean print_is_already_a_function(String name) {
			return name.endsWith("/Tools/ccbench/ccbench.py") || name.endsWith("/Tools/scripts/byext.py")
					|| name.endsWith("/Tools/scripts/pindent.py ") || name.endsWith("/test/pythoninfo.py")
					|| name.endsWith("/test/bisect.py") || name.endsWith("/Tools/ssl/multissltests.py")
					|| name.endsWith("/Tools/scripts/pindent.py") || name.endsWith("/test/test_regrtest.py")
					|| name.endsWith("/test/test_future5.py") || name.endsWith("/ensurepip/__init__.py")
					|| name.endsWith("/idlelib/PyShell.py") || name.endsWith("/idlelib/configHandler.py")
					|| name.endsWith("/test/test_print.py") || name.endsWith("lib-python/2.7/test/test_file.py")
					|| name.endsWith("lib-python/2.7/test/test_io.py");
		}

		private boolean star_args(String name) {
			// invalid syntax (* as function def argument / * in front of a function def
			// argument)
			// with Python 2.7.2 -> check with 2.7.14 again
			return name.endsWith("/PCbuild/get_external.py") || name.endsWith("/tkinter/ttk.py");
		}

		private boolean bad_syntax(String name) {
			return name.endsWith("/test/bad_coding2.py") || name.endsWith("/test/bad_coding3.py");
		}

		private boolean unknown(String name) {
			return name.endsWith("/test/test_xmlrpc.py");
		}

		private boolean isBuildOutput(String name) {
			return name.contains("build/classes/");
		}
	}

}