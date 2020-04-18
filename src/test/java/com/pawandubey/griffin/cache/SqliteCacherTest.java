package com.pawandubey.griffin.cache;

import com.pawandubey.griffin.Data;
import com.pawandubey.griffin.model.Content;
import com.pawandubey.griffin.model.Parsable;
import com.pawandubey.griffin.model.Post;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SqliteCacherTest {
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@Test
	public void test() throws IOException {
		final File file = folder.newFile();

		final SqliteCacher sqliteCacher = new SqliteCacher(file.getPath());
	}

	@Test
	public void testGetTags() throws IOException {
		final File file = folder.newFile();

		final SqliteCacher sqliteCacher = new SqliteCacher(file.getPath());

		Assertions.assertEquals(0, sqliteCacher.getTags().size());
	}

	@Test
	public void testCacheContent() throws IOException {
		final File file = folder.newFile();

		final SqliteCacher sqliteCacher = new SqliteCacher(file.getPath());

		String wholeContent = "title = \"Hello\"\n" +
				"date = \"2016 08 07\"\n" +
				"layout = \"post\"\n" +
				"slug = \"first-post\"\n" +
				"tags = [\"t1\",\"t2\"]\n" +
				"image = \"/images/path.jpeg\"\n" +
				"#####\n" +
				"## Hello World!\n";


		Parsable post = Content.parse("first.md", wholeContent);
		final ArrayList<Parsable> posts = new ArrayList<>();
		posts.add(post);
		sqliteCacher.cacheFileQueue(posts);

		sqliteCacher.printTables();

		Assertions.assertEquals(1, sqliteCacher.getFileQueue().size());
	}

	@Test
	public void testCacheTags() throws IOException {
		final File file = folder.newFile();

		final SqliteCacher sqliteCacher = new SqliteCacher(file.getPath());

		String wholeContent = "title = \"Hello\"\n" +
				"date = \"2016 08 07\"\n" +
				"layout = \"post\"\n" +
				"slug = \"first-post\"\n" +
				"tags = [\"t1\",\"t2\"]\n" +
				"image = \"/images/path.jpeg\"\n" +
				"#####\n" +
				"## Hello World!\n";


		Parsable post = Content.parse("first.md", wholeContent);
		final List<Parsable> posts = new ArrayList<>();
		posts.add(post);
		ConcurrentMap<String, List<Parsable>> tags = new ConcurrentHashMap<>();

		for (String tag : post.getTags()) {
			tags.put(tag, Arrays.asList(post));
		}

		sqliteCacher.cacheTaggedParsables(tags);

		sqliteCacher.printTables();

		Assertions.assertEquals(post.getTags().size(), sqliteCacher.getTags().size());
	}

}