package com.pawandubey.griffin.model;

import com.pawandubey.griffin.Parser;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ContentTest {

	@Test
	void parse() throws IOException {
		final Parsable post = Content.parse("test", "layout = \"post\"\n" +
				"title = \"Middle of the Linked List\"\n" +
				"date = \"2020 04 12\"\n" +
				"summary = \"Middle of the Linked List\"\n" +
				"categories = \"leetcode\"\n" +
				"\n" +
				"#####\n" +
				"\n" +
				"Given a non-empty\n");

		Assert.assertThat(post, CoreMatchers.notNullValue());
		Assert.assertThat(post.getContent(), CoreMatchers.notNullValue());


	}
}