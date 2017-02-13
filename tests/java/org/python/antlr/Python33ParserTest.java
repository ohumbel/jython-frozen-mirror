package org.python.antlr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

public class Python33ParserTest {

	private static final String CPYTHON_ROOT = "/Users/oti/stuff/gitrepo/python/cpython";

	@Test
	public void testParseSingleFile() {
		Path file = Paths.get(CPYTHON_ROOT, "Lib/email/__init__.py");
		assertTrue(checkParseable(file));
	}

	@Test
	public void testParseWholePython33Library() throws IOException {
		Path startingDir = Paths.get(CPYTHON_ROOT);
		ParseFiles pf = new ParseFiles();
		Files.walkFileTree(startingDir, pf);
		List<String> failedFiles = pf.getFailedFiles();
		for (String failedFile : failedFiles) {
			System.out.println(failedFile);
		}
		assertEquals(0, failedFiles.size());

		// the following files failed:
		// /Users/oti/stuff/gitrepo/python/cpython/Lib/test/bad_coding2.py
		// /Users/oti/stuff/gitrepo/python/cpython/Lib/test/badsyntax_3131.py
		// /Users/oti/stuff/gitrepo/python/cpython/Lib/test/test_pep3131.py
		// /Users/oti/stuff/gitrepo/python/cpython/Misc/Vim/syntax_test.py
		// /Users/oti/stuff/gitrepo/python/cpython/PC/VC6/rmpyc.py
		// /Users/oti/stuff/gitrepo/python/cpython/PC/VS7.1/build_ssl.py
		// /Users/oti/stuff/gitrepo/python/cpython/PC/VS7.1/field3.py
		// /Users/oti/stuff/gitrepo/python/cpython/PC/VS7.1/rmpyc.py
		// /Users/oti/stuff/gitrepo/python/cpython/Tools/hg/hgtouch.py
		// /Users/oti/stuff/gitrepo/python/cpython/Tools/msi/msi.py
		// /Users/oti/stuff/gitrepo/python/cpython/Tools/msi/msilib.py
		// /Users/oti/stuff/gitrepo/python/cpython/Tools/scripts/findnocoding.py
	}

	@Test
	public void testParseSimpleInput() {
		StringBuilder b = new StringBuilder();
		b.append("for index, name, type in fields:\n");
		b.append("    index -= 1\n");
		b.append("    unk = type & ~knownbits\n");
		b.append("    if unk:\n");
		b.append("        print('%s %s unknown bits %x' % (name, name, unk))\n");
		b.append("    size = type & datasizemask\n");

		assertTrue(checkParseable(new ANTLRInputStream(b.toString())));
	}

	private static boolean checkParseable(Path file) {
		try (InputStream inputStream = Files.newInputStream(file)) {
			return checkParseable(new ANTLRInputStream(inputStream));
		} catch (IOException e) {
			return false;
		}
	}

	private static boolean checkParseable(ANTLRInputStream input) {
		boolean parseable = true;
		try {
			Python33Lexer lexer = new Python33Lexer(input);
			DescriptiveBailErrorListener errorListener = new DescriptiveBailErrorListener();
			lexer.addErrorListener(errorListener);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			Python33Parser parser = new Python33Parser(tokens);
			parser.addErrorListener(errorListener);
			ParseTree tree = parser.file_input();
			assertNotNull(tree);
		} catch (Exception e) {
			parseable = false;
		}
		return parseable;
	}

	private static final class ParseFiles extends SimpleFileVisitor<Path> {
		private final List<String> failedFiles = new ArrayList<>();

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
			if (attr.isRegularFile()) {
				String fileAsString = file.toString();
				if (fileAsString.endsWith(".py") && //
						!fileAsString.contains("/test2to3/") && //
						!fileAsString.contains("/lib2to3/")) {
					System.out.println(System.out.format("parsing file: %s ", fileAsString));
					if (!checkParseable(file)) {
						failedFiles.add(fileAsString);
					}
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
	}

}