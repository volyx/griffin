/*
 * Copyright 2015 Pawan Dubey pawandubey@outlook.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pawandubey.griffin;

import com.pawandubey.griffin.cache.Cacher;
import com.pawandubey.griffin.cli.NewCommand;
import com.pawandubey.griffin.cli.PreviewCommand;
import com.pawandubey.griffin.cli.PublishCommand;
import com.pawandubey.griffin.model.Parsable;
import com.pawandubey.griffin.model.Post;
import com.pawandubey.griffin.pipeline.ContentCollectors;
import com.pawandubey.griffin.pipeline.ContentFilter;
import com.pawandubey.griffin.pipeline.ContentParser;
import com.pawandubey.griffin.pipeline.ContentRenderer;
import com.pawandubey.griffin.pipeline.ContentWriter;
import com.pawandubey.griffin.pipeline.DirectoryCleaner;
import com.pawandubey.griffin.pipeline.FileCopier;
import com.pawandubey.griffin.renderer.HandlebarsRenderer;
import com.pawandubey.griffin.renderer.Renderer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.pawandubey.griffin.Data.*;

/**
 * @author Pawan Dubey pawandubey@outlook.com
 */
@Command(name = "griffin",
		version = Griffin.VERSION,
		mixinStandardHelpOptions = true,
		synopsisSubcommandLabel = "COMMAND",
		subcommands = {
				NewCommand.class,
				PublishCommand.class,
				PreviewCommand.class
		},
		description = "a simple and fast static site generator. ")
public class Griffin implements Runnable {

	public static final String VERSION = "0.3.1";
	public static final String EXCERPT_MARKER = "##more##";


	private Cacher cacher;

	@CommandLine.Spec
	CommandLine.Model.CommandSpec spec;

	/**
	 * Creates a new instance of Griffin
	 */
	public Griffin() {
	}

	public Griffin(Cacher cacher) {
		this.cacher = cacher;
	}


	/**
	 * Parses the content of the site in the 'content' directory and produces
	 * the output. It parses incrementally i.e it only parses the content which
	 * has changed since the last parsing event if the fastParse variable is
	 * true.
	 *
	 * @param fastParse Do a fast incremental parse
	 * @param rebuild   Do force a full rebuild
	 * @param verbose
	 * @throws IOException the exception
	 */
	public void publish(boolean fastParse, boolean rebuild, boolean verbose) throws IOException {
		long start = System.currentTimeMillis();

		Path directory = Paths.get(DirectoryStructure.getInstance().ROOT_DIRECTORY);
		Path content = directory.resolve(DirectoryStructure.getInstance().SOURCE_DIRECTORY);
		Path output = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY);
		Path themesPath = Paths.get(DirectoryStructure.getInstance().THEMES_DIRECTORY, Data.config.getTheme());
		Path infoFilePath = Paths.get(DirectoryStructure.getInstance().INFO_FILE);

		Path assetsPath = themesPath.resolve("assets");
		Path outputAssetsPath = output.resolve("assets");

		if (cacher.cacheExists() && !rebuild) {
			System.out.println("Reading from the cache for your pleasure...");

			ConcurrentMap<String, List<Parsable>> tag = cacher.getTags();
			List<Parsable> qu = cacher.getFileQueue();
			Data.parsables.addAll(qu);
			Data.tags.putAll(tag);
			int st = Data.parsables.size();
			System.out.println("Read " + st + " objects from the cache. Woohooo!!");

			Data.parsables.addAll(
					Files.walk(content)
							.filter(new ContentFilter())
							.filter(greaterThenLastModified())
							.map(new ContentParser(content))
							.collect(Collectors.toList())
			);

			System.out.println("Found " + (Data.parsables.size() - st) + " new objects!");
		} else {
			if (fastParse && !rebuild) {
				Data.parsables.addAll(
						Files.walk(content)
								.filter(new ContentFilter())
								.filter(greaterThenLastModified())
								.map(new ContentParser(content))
								.collect(Collectors.toList())
				);
			} else {
				System.out.println("Rebuilding site from scratch...");

				new DirectoryCleaner().accept(output);
				System.out.println("Carefully copying the assets...");

				if (Files.notExists(outputAssetsPath)) {
					Files.createDirectory(outputAssetsPath);
				}

				Files.walk(assetsPath)
						.forEach(new FileCopier(assetsPath, outputAssetsPath));

				final Path images = content.resolve("images");
				final Path outputImages = output.resolve("images");

				if (Files.notExists(outputImages)) {
					Files.createDirectory(outputImages);
				}

				Files.walk(images)
						.forEach(new FileCopier(images, outputImages));

				// Compile the markdown files
				Data.parsables.addAll(
						Files.walk(content)
								.filter(new ContentFilter())
								.map(new ContentParser(content))
								.collect(Collectors.toList())
				);
			}
		}

		Data.parsables.sort(Comparator.comparing(Parsable::getDate));

		cacher.cacheFileQueue(parsables);

		Data.latestPosts.addAll(
				ContentCollectors.findLatestPosts(parsables, config.getIndexPosts())
		);

		Data.navPages.addAll(
				ContentCollectors.findNavigationPages(parsables)
		);

		if (verbose) {
			System.out.println(config.toString());

			for (Parsable parsable : parsables) {
				System.out.println(parsable.getLocation() + "...");
			}
			System.out.println("latest posts...");
			for (Parsable parsable : latestPosts) {
				System.out.println(parsable.getLocation() + "...");
			}
		}

		System.out.println("Parsing " + Data.parsables.size() + " objects...");

		final Renderer renderer = new HandlebarsRenderer(themesPath);

		if (Files.notExists(output.resolve("SITEMAP.xml")) && Files.exists(themesPath.resolve("SITEMAP.html"))) {
			try {
				Files.write(output.resolve("SITEMAP.xml"), renderer.renderSitemap().getBytes(StandardCharsets.UTF_8));
			} catch (IOException ex) {
				Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		Indexer indexer = new Indexer();
		indexer.initIndexes();

		parsables.stream()
				.peek(p -> {
					    if (p instanceof Post) {
      						indexer.addToIndex(p);
    					}
				})
				.map(new ContentRenderer(renderer, config))
				.forEach(new ContentWriter(output));



		if (config.getRenderTags()) {
			Data.tags.putAll(
					ContentCollectors.findTags(parsables)
			);

			renderTags(renderer);
		}

		indexer.sortIndexes();

		renderIndexRssAnd404(renderer, indexer, output, themesPath);
		InfoHandler info = new InfoHandler();
		info.writeInfoFile(infoFilePath);
		cacher.cacheTaggedParsables(Data.tags);

		long end = System.currentTimeMillis();
		System.out.println("Time (hardly) taken: " + (end - start) + " ms");
	}

	private Predicate<Path> greaterThenLastModified() {
		return path -> {
			try {
				return Files.getLastModifiedTime(path).toMillis() > InfoHandler.LAST_PARSE_DATE;
			} catch (IOException e) {
				Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, e);
				return true;
			}
		};
	}

	public static void printAsciiGriffin() {
		System.out.println("                       ___     ___                  ");
		System.out.println("                __   /'___\\  /'___\\  __             ");
		System.out.println("   __    _ __  /\\_\\ /\\ \\__/ /\\ \\__/ /\\_\\     ___    ");
		System.out.println(" /'_ `\\ /\\`'__\\\\/\\ \\\\ \\ ,__\\\\ \\ ,__\\\\/\\ \\  /' _ `\\  ");
		System.out.println("/\\ \\L\\ \\\\ \\ \\/  \\ \\ \\\\ \\ \\_/ \\ \\ \\_/ \\ \\ \\ /\\ \\/\\ \\ ");
		System.out.println("\\ \\____ \\\\ \\_\\   \\ \\_\\\\ \\_\\   \\ \\_\\   \\ \\_\\\\ \\_\\ \\_\\");
		System.out.println(" \\/___L\\ \\\\/_/    \\/_/ \\/_/    \\/_/    \\/_/ \\/_/\\/_/");
		System.out.println("   /\\____/                                          ");
		System.out.println("   \\_/__/                                           ");
	}

	public static void main(String[] args) {
		System.exit(new CommandLine(new Griffin()).execute(args));
	}

	@Override
	public void run() {
		throw new ParameterException(spec.commandLine(), "Missing required subcommand");
	}

	private void renderIndexRssAnd404(Renderer renderer, Indexer indexer, Path output, Path themesPath) throws IOException {

		final Path pagePath = output.resolve("page");

		final List<SingleIndex> list = indexer.getIndexList();

		if (Files.notExists(output.resolve("index.html"))) {
			try {
				final SingleIndex firstIndex = list.get(0);
				Files.write(output.resolve("index.html"), renderer.renderIndex(firstIndex).getBytes(StandardCharsets.UTF_8));
			} catch (IOException ex) {
				Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		if (Files.notExists(pagePath)) {
			Files.createDirectory(pagePath);
		}
		list.remove(0);

		for (SingleIndex s : list) {
			Path secondaryIndexPath = pagePath.resolve("" + (list.indexOf(s) + 2));

			if (Files.notExists(secondaryIndexPath)) {
				Files.createDirectory(secondaryIndexPath);
			}

			Files.write(secondaryIndexPath.resolve("index.html"), renderer.renderIndex(s).getBytes(StandardCharsets.UTF_8));
		}

		if (Files.notExists(output.resolve("feed.xml")) && Files.exists(themesPath.resolve("feed.htm;"))) {
			try {
				Files.write(output.resolve("feed.xml"),renderer.renderRssFeed().getBytes(StandardCharsets.UTF_8));
			} catch (IOException ex) {
				Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		if (Files.notExists(output.resolve("404.html")) && Files.exists(themesPath.resolve("404.html"))) {
			try {
				Files.write(output.resolve("404.html"), renderer.render404().getBytes(StandardCharsets.UTF_8));
			} catch (IOException ex) {
				Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	//TODO Method doesn't fit on screen -> TOO LONG. Refactor.

	/**
	 * Renders the posts of each tag as symbolic links. This method calcualtes
	 * the number of threads needed based on the criteria that one thread will
	 * handle 10 tags. Then partitions the tags keyset into that many Lists of
	 * tags. Then each list is processed by a new thread in the executor
	 * service.
	 */
	protected void renderTags(Renderer renderer) throws IOException {

		if (config.getRenderTags()) {

			final Path output = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY);
			final Path tagsPath = Paths.get(DirectoryStructure.getInstance().TAGS_DIRECTORY);

			if (Files.notExists(tagsPath)) {
				Files.createDirectory(tagsPath);
			}

			for (List<Parsable> l : tags.values()) {
				l.sort((s, t) -> t.getDate().compareTo(s.getDate()));
			}

			for (Map.Entry<String, List<Parsable>> entry : tags.entrySet()) {
				String tag = entry.getKey();
				Path tagDir = tagsPath.resolve(tag);
				if (Files.notExists(tagDir)) {
					try {// (BufferedWriter bw = Files.newBufferedWriter(tagDir.resolve("index.html"), StandardCharsets.UTF_8)) {
						Files.createDirectory(tagDir);
						//bw.write(renderer.renderTagIndex(a, tags.get(a)));
					} catch (IOException ex) {
						Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
					}
				}

				for (Parsable p : entry.getValue()) {
					Path slugPath = tagDir.resolve(p.getSlug());
					if (Files.notExists(slugPath)) {
						try {
							if (Files.notExists(slugPath)) {
								Files.createDirectory(slugPath);

								Path parsedDir = output.resolve(p.getSlug());
								if (Files.notExists(parsedDir)) {
									Files.createDirectory(parsedDir);
								}

								Path linkedFile = parsedDir.resolve("index.html");
								Files.createSymbolicLink(slugPath.resolve("index.html"), Paths.get("/").resolve(output).relativize(linkedFile));
							}
						} catch (IOException ex) {
							Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}


				try {
					Files.write(tagDir.resolve("index.html"),  renderer.renderTagIndex(tag, parsables).getBytes(StandardCharsets.UTF_8));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			}

		}
	}
}
