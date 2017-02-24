package org.python.antlr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
import java.util.List;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

public class Python27ParserTest {

	private static final String CPYTHON_ROOT = "/Users/oti/stuff/gitrepo/python/cpython";

	@Test
	public void testParseSingleFile() throws FileNotFoundException {
		Path file = Paths.get(CPYTHON_ROOT, "Tools/scripts/findnocoding.py");
		assertParseable(file);
	}

	@Test
	public void testParseWholePython27Library() throws IOException {
		Path startingDir = Paths.get(CPYTHON_ROOT);
		ParseFiles pf = new ParseFiles();
		Files.walkFileTree(startingDir, pf);
		List<String> failedFiles = pf.getFailedFiles();
		for (String failedFile : failedFiles) {
			System.out.println(failedFile);
		}
		assertEquals(0, failedFiles.size());
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
		assertParseable(new ANTLRInputStream(b.toString()));
	}

	private static void assertParseable(Path file) throws FileNotFoundException {
		try (InputStream inputStream = Files.newInputStream(file)) {
			System.out.println(System.out.format("parsing file: %s ", file));
			assertParseable(new ANTLRInputStream(inputStream));
		} catch (IOException e) {
			throw new FileNotFoundException(file.toString());
		}

	}

	private static void assertParseable(ANTLRInputStream input) {
		Python27Lexer lexer = new Python27Lexer(input);
		DescriptiveBailErrorListener errorListener = new DescriptiveBailErrorListener();
		lexer.addErrorListener(errorListener);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		Python27Parser parser = new Python27Parser(tokens);
		parser.addErrorListener(errorListener);
		ParseTree tree = parser.file_input();
		assertNotNull(tree);
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
				if (is_not_a_pure_3_grammar(fileName) || print_is_not_a_function(fileName)
						|| except_is_not_a_function(fileName) || raise_is_not_a_function(fileName)
						|| bad_syntax(fileName) || not_sure_whats_wrong(fileName)) {
					canParse = false;
				}
			}
			return canParse;
		}

		private boolean is_not_a_pure_3_grammar(String name) {
			return name.contains("/test2to3/") || name.contains("/lib2to3/");
		}

		private boolean print_is_not_a_function(String name) {
			return name.endsWith("/PC/VC6/rmpyc.py") || name.endsWith("/PC/VS7.1/build_ssl.py")
					|| name.endsWith("/PC/VS7.1/field3.py") || name.endsWith("/PC/VS7.1/rmpyc.py")
					|| name.endsWith("/Tools/msi/msilib.py");
		}

		private boolean except_is_not_a_function(String name) {
			return name.endsWith("/Tools/hg/hgtouch.py");
		}

		private boolean raise_is_not_a_function(String name) {
			return name.endsWith("/Tools/msi/msi.py");
		}

		private boolean bad_syntax(String name) {
			return name.endsWith("/Lib/test/bad_coding2.py") || name.endsWith("/Lib/test/badsyntax_3131.py")
					|| name.endsWith("/Lib/test/test_pep3131.py") || name.endsWith("/Misc/Vim/syntax_test.py");
		}

		private boolean not_sure_whats_wrong(String name) {
			// a re.compile() statment fails to parse:
			return name.endsWith("/Tools/scripts/findnocoding.py");
		}
	}

}