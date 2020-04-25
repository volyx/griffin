package com.pawandubey.griffin.model;

import com.pawandubey.griffin.Data;
import com.pawandubey.griffin.DirectoryStructure;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.Random;

class ContentTest {

	@TempDir
	File anotherTempDir;

	Random random = new Random();

	@Test
	void parse() {
		DirectoryStructure.create(anotherTempDir.toPath());

		final Path contentPath = anotherTempDir.toPath().resolve(random.nextInt() + "");

		new Data();
		final Parsable post = Content.parse(contentPath,
				"---\n" +
						"layout = \"post\"\n" +
						"title = \"Middle of the Linked List\"\n" +
						"date = \"2020 04 12\"\n" +
						"summary = \"Middle of the Linked List\"\n" +
						"categories = \"leetcode\"\n" +
						"\n" +
						"---\n" +
						"\n" +
						"Given a non-empty\n");

		Assert.assertThat(post, CoreMatchers.notNullValue());
		Assert.assertThat(post.getContent(), CoreMatchers.notNullValue());
	}

	@Test()
	void parseExceptionMissingStartDelimiter() {
		DirectoryStructure.create(anotherTempDir.toPath());
		new Data();
		final Path contentPath = anotherTempDir.toPath().resolve(random.nextInt() + "");
		Assertions.assertThrows(RuntimeException.class, () -> {
			final Parsable post = Content.parse(contentPath,
					"layout = \"post\"\n" +
							"title = \"Middle of the Linked List\"\n" +
							"date = \"2020 04 12\"\n" +
							"summary = \"Middle of the Linked List\"\n" +
							"categories = \"leetcode\"\n" +
							"\n" +
							"---\n" +
							"\n" +
							"Given a non-empty\n");
		});
	}

	@Test()
	void parseExceptionMissingEndDelimiter() {
		DirectoryStructure.create(anotherTempDir.toPath());
		new Data();

		final Path contentPath = anotherTempDir.toPath().resolve(random.nextInt() + "");
		Assertions.assertThrows(RuntimeException.class, () -> {
			final Parsable post = Content.parse(contentPath,
					"---\n" +
							"layout = \"post\"\n" +
							"title = \"Middle of the Linked List\"\n" +
							"date = \"2020 04 12\"\n" +
							"summary = \"Middle of the Linked List\"\n" +
							"categories = \"leetcode\"\n" +
							"\n" +
							"\n" +
							"Given a non-empty\n");
		});
	}

}