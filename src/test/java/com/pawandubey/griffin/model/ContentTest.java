package com.pawandubey.griffin.model;

import com.pawandubey.griffin.Data;
import com.pawandubey.griffin.DirectoryStructure;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;

class ContentTest {

	@TempDir
	File anotherTempDir;

	@Test
	void parse() {
		DirectoryStructure.create(anotherTempDir.toPath());
		new Data();
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