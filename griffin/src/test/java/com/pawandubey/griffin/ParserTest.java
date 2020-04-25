package com.pawandubey.griffin;

import com.pawandubey.griffin.model.Content;
import com.pawandubey.griffin.model.Parsable;
import org.junit.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class ParserTest {

	@TempDir
	File tempDir;

	@Test
	public void test() throws IOException {
		Griffin parser = new Griffin(tempDir.toPath());

		Path contentPath = tempDir.toPath().resolve("other");
		final Parsable post = Content.parse(contentPath, "layout = \"post\"\n" +
				"title = \"Middle of the Linked List\"\n" +
				"date = \"2020 04 12\"\n" +
				"summary = \"Middle of the Linked List\"\n" +
				"categories = \"leetcode\"\n" +
				"\n" +
				"#####\n" +
				"\n" +
				"Given a non-empty\n");


		parser.writeParsedFile(post);
	}

}