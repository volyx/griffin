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

import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.pawandubey.griffin.cache.Cacher;
import com.pawandubey.griffin.cli.NewCommand;
import com.pawandubey.griffin.cli.PreviewCommand;
import com.pawandubey.griffin.cli.PublishCommand;
import com.pawandubey.griffin.markdown.JygmentsCodeEmitter;
import com.pawandubey.griffin.model.Content;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
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
	private Indexer indexer;
	private Renderer renderer;

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

		if (cacher.cacheExists() && !rebuild) {
			System.out.println("Reading from the cache for your pleasure...");

			ConcurrentMap<String, List<Parsable>> tag = cacher.getTags();
			List<Parsable> qu = cacher.getFileQueue();
			Data.parsables.addAll(qu);
			Data.tags.putAll(tag);
			int st = Data.parsables.size();
			System.out.println("Read " + st + " objects from the cache. Woohooo!!");
			fastReadIntoQueue(Paths.get(DirectoryStructure.getInstance().SOURCE_DIRECTORY).normalize());
			System.out.println("Found " + (Data.parsables.size() - st) + " new objects!");
		} else {
			if (fastParse && !rebuild) {
				fastReadIntoQueue(Paths.get(DirectoryStructure.getInstance().SOURCE_DIRECTORY).normalize());
			} else {
				System.out.println("Rebuilding site from scratch...");

				System.out.println("Carefully copying the assets...");
				new DirectoryCleaner().accept(output);

				Path assetsPath = Paths.get(DirectoryStructure.getInstance().THEMES_DIRECTORY, "assets");
				Path outputAssetsPath = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY, "assets");

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

		parsables.sort(Comparator.comparing(Parsable::getDate));

		cacher.cacheFileQueue(parsables);

		Data.latestPosts.addAll(
				ContentCollectors.findLatestPosts(parsables, config.getIndexPosts())
		);

		Data.navPages.addAll(
				ContentCollectors.findNavigationPages(parsables)
		);

		if (verbose) {
			for (Parsable parsable : parsables) {
				System.out.println(parsable.getLocation() + "...");
			}
			System.out.println("latest posts...");
			for (Parsable parsable : latestPosts) {
				System.out.println(parsable.getLocation() + "...");
			}
		}

		System.out.println("Parsing " + Data.parsables.size() + " objects...");

		renderer = new HandlebarsRenderer();

		if (Files.notExists(output.resolve("SITEMAP.xml"))
				&& Files.exists(Paths.get(DirectoryStructure.getInstance().THEMES_DIRECTORY).resolve("SITEMAP.html"))) {
			try (BufferedWriter bw = Files.newBufferedWriter(output.resolve("SITEMAP.xml"), StandardCharsets.UTF_8)) {
				bw.write(renderer.renderSitemap());
			} catch (IOException ex) {
				Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		indexer = new Indexer();

		Configuration renderConfig = Configuration.builder().enableSafeMode()
				.forceExtentedProfile()
				.setAllowSpacesInFencedCodeBlockDelimiters(true)
				.setEncoding("UTF-8")
//                .setCodeBlockEmitter(new CodeBlockEmitter())
				.setCodeBlockEmitter(new JygmentsCodeEmitter())
				.build();
		indexer.initIndexes();

		for (Parsable p : parsables) {
			Path htmlPath = resolveHtmlPath(p);

			try {
				String parsedContent = Processor.process(p.getContent(), renderConfig);
				p.setContent(parsedContent);
				if (p instanceof Post) {
					indexer.addToIndex(p);
				}
				Files.write(htmlPath, renderer.renderParsable(p).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
			} catch (IOException ex) {
				Logger.getLogger(Griffin.class
						.getName()).log(Level.SEVERE, null, ex);
			}
		}

		if (config.getRenderTags()) {
			Data.tags.putAll(
					ContentCollectors.findTags(parsables)
			);

			if (Files.notExists(Paths.get(DirectoryStructure.getInstance().TAG_DIRECTORY))) {
				Files.createDirectory(Paths.get(DirectoryStructure.getInstance().TAG_DIRECTORY));
			}
			renderTags();
		}

		indexer.sortIndexes();

		renderIndexRssAnd404();
		InfoHandler info = new InfoHandler();

		info.writeInfoFile();
		cacher.cacheTaggedParsables(Data.tags);

		long end = System.currentTimeMillis();
		System.out.println("Time (hardly) taken: " + (end - start) + " ms");
	}

	/**
	 * Parses the content of the site in the 'content' directory and produces
	 * the output. It parses incrementally i.e it only parses the content which
	 * has changed since the last parsing event if the fastParse variable is
	 * true.
	 *
	 * @param fastParse Do a fast incremental parse
	 * @param rebuild   Do force a full rebuild
	 * @throws IOException the exception
	 */
	public void publish2(boolean fastParse, boolean rebuild) throws IOException {
		long start = System.currentTimeMillis();

		Path directory = Paths.get(DirectoryStructure.getInstance().ROOT_DIRECTORY);

		Path config = directory.resolve("config.yaml");
		Path assets = Paths.get(DirectoryStructure.getInstance().THEMES_DIRECTORY).resolve("assets");
		Path data = directory.resolve("data");
		Path content = directory.resolve(DirectoryStructure.getInstance().SOURCE_DIRECTORY);
		Path template = directory.resolve("layout");
		Path output = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY);

		System.out.println("Cleaning up the output area...");
		if (Files.exists(output)) {
			Files.walk(output)
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
		}
		System.out.println("Cleanup done.");

		System.out.println("Rebuilding site from scratch...");

		System.out.println("Carefully copying the assests...");
		Path assetsPath = Paths.get(DirectoryStructure.getInstance().THEMES_DIRECTORY, "assets");
		Path outputAssetsPath = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY, "assets");
		Files.walk(assetsPath).forEach(new FileCopier(assetsPath, outputAssetsPath));
		System.out.println("Copying done.");


		// Compile the markdown files
		final List<Parsable> parsables = Files.walk(content)
				.filter(new ContentFilter())
				.map(new ContentParser(content))
				.collect(Collectors.toList());


		parsables.stream()
				.map(new ContentRenderer(template))
				.forEach(new ContentWriter(output));


		long end = System.currentTimeMillis();
		System.out.println("Time (hardly) taken: " + (end - start) + " ms");
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


	private void renderIndexRssAnd404() throws IOException {
		final List<SingleIndex> list = indexer.getIndexList();

		if (Files.notExists(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("index.html"))) {
			try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("index.html"), StandardCharsets.UTF_8)) {
				bw.write(renderer.renderIndex(list.get(0)));
			} catch (IOException ex) {
				Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
		final Path pagePath = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY, "page");
		if (Files.notExists(pagePath)) {
			Files.createDirectory(pagePath);
		}
		list.remove(0);

		for (SingleIndex s : list) {
			Path secondaryIndexPath = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY, "page", "" + (list.indexOf(s) + 2));

			if (Files.notExists(secondaryIndexPath)) {
				Files.createDirectory(secondaryIndexPath);
			}

			Files.write(secondaryIndexPath.resolve("index.html"), renderer.renderIndex(s).getBytes(StandardCharsets.UTF_8));
		}

		if (Files.notExists(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("feed.xml")) && Files.exists(Paths.get(DirectoryStructure.getInstance().THEMES_DIRECTORY).resolve("feed.htm;"))) {
			try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("feed.xml"), StandardCharsets.UTF_8)) {
				bw.write(renderer.renderRssFeed());
			} catch (IOException ex) {
				Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
			}
		}

		if (Files.notExists(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY, "404.html")) && Files.exists(Paths.get(DirectoryStructure.getInstance().THEMES_DIRECTORY, "404.html"))) {
			try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("404.html"), StandardCharsets.UTF_8)) {
				bw.write(renderer.render404());
			} catch (IOException ex) {
				Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	//TODO Method doesnt fit on screen -> TOO LONG. Refactor.

	/**
	 * Renders the posts of each tag as symbolic links. This method calcualtes
	 * the number of threads needed based on the criteria that one thread will
	 * handle 10 tags. Then partitions the tags keyset into that many Lists of
	 * tags. Then each list is processed by a new thread in the executor
	 * service.
	 */
	protected void renderTags() {
		if (config.getRenderTags()) {
			for (List<Parsable> l : tags.values()) {
				l.sort((s, t) -> t.getDate().compareTo(s.getDate()));
			}

			for (String tag : tags.keySet()) {
				Path tagDir = Paths.get(DirectoryStructure.getInstance().TAG_DIRECTORY).resolve(tag);
				if (Files.notExists(tagDir)) {
					try {// (BufferedWriter bw = Files.newBufferedWriter(tagDir.resolve("index.html"), StandardCharsets.UTF_8)) {
						Files.createDirectory(tagDir);
						//bw.write(renderer.renderTagIndex(a, tags.get(a)));
					} catch (IOException ex) {
						Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
					}
				}

				List<Parsable> parsables = tags.get(tag);

				for (Parsable p : parsables) {
					Path slugPath = tagDir.resolve(p.getSlug());
					if (Files.notExists(slugPath)) {
						try {
							if (Files.notExists(slugPath)) {
								Files.createDirectory(slugPath);
								Path linkedFile = resolveHtmlPath(p);
								Files.createSymbolicLink(slugPath.resolve("index.html"), Paths.get("/").resolve(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY)).relativize(linkedFile));
							}
						} catch (IOException ex) {
							Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
						}
					}
				}
				try (BufferedWriter bw = Files.newBufferedWriter(tagDir.resolve("index.html"), StandardCharsets.UTF_8)) {
					bw.write(renderer.renderTagIndex(tag, parsables));
				} catch (IOException ex) {
					Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
				}
			}

		}
	}

	private static Path resolveHtmlPath(Parsable p) throws IOException {
		String name = p.getSlug();
		Path parsedDir = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve(name);
		if (Files.notExists(parsedDir)) {
			Files.createDirectory(parsedDir);
		}
		return parsedDir.resolve("index.html");

	}

	//TODO refactor this method to make use of the above method someway.

	/**
	 * Checks if the file has been modified after the last parse event and only
	 * then adds the file into the queue for parsing, hence saving time.
	 *
	 * @param rootPath
	 * @throws IOException the exception
	 */
	protected static void fastReadIntoQueue(Path rootPath) throws IOException {
//        cleanOutputDirectory();
//        copyTemplateAssets();

		Files.walkFileTree(rootPath, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				final Path correspondingOutputPath = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY)
						.resolve(rootPath.relativize(dir));

				if (config.getExcludeDirs().contains(dir.getFileName().toString())) {
					return FileVisitResult.SKIP_SUBTREE;
				}
				if (Files.notExists(correspondingOutputPath)) {
					Files.createDirectory(correspondingOutputPath);
				}

				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				final long fileModified = Files.getLastModifiedTime(file).toMillis();
				final Path resolvedPath = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve(rootPath.relativize(file));
				if (file.getFileName().toString().endsWith(".md")) {
					if (fileModified> InfoHandler.LAST_PARSE_DATE) {
						Parsable parsable = Content.createParsable(file);
						Data.parsables.removeIf(p -> p.getPermalink().equals(parsable.getPermalink()));
						Data.parsables.add(parsable);
					}
				} else {
					Files.copy(file, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc) {
				return FileVisitResult.TERMINATE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
				return FileVisitResult.CONTINUE;
			}
		});
	}


}
