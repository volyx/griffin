package com.pawandubey.griffin;

import com.pawandubey.griffin.model.Content;
import com.pawandubey.griffin.model.Parsable;
import org.junit.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ParserTest {

	@Test
	public void test() throws IOException {
		Parser parser = new Parser();

		final Parsable post = Content.parse("test", "layout = \"post\"\n" +
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