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
import com.pawandubey.griffin.model.Parsable;
import com.pawandubey.griffin.model.Post;
import com.pawandubey.griffin.pipeline.ContentCollectors;
import com.pawandubey.griffin.pipeline.ContentFilter;
import com.pawandubey.griffin.pipeline.ContentParser;
import com.pawandubey.griffin.pipeline.ContentRenderer;
import com.pawandubey.griffin.pipeline.ContentWriter;
import com.pawandubey.griffin.pipeline.FileCopier;
import com.pawandubey.griffin.renderer.HandlebarsRenderer;
import com.pawandubey.griffin.renderer.Renderer;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.pawandubey.griffin.Configurator.LINE_SEPARATOR;
import static com.pawandubey.griffin.Data.*;

/**
 *
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
    private final DirectoryCrawler crawler;
    private Indexer indexer;
    private Configuration renderConfig;
    private Renderer renderer;

    private Cacher cacher;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    /**
     * Creates a new instance of Griffin
     */
    private Griffin() {
        this.crawler = null;
    }

    /**
     * Creates a new instance of Griffin with the root directory of the site set
     * to the given path.
     *
     * @param path The path to the root directory of the griffin site.
     */
    public Griffin(Path path) {
        this.crawler = new DirectoryCrawler();
    }

    public Griffin(Cacher cacher) {
        this.cacher = cacher;
        this.crawler = new DirectoryCrawler();
    }

    /**
     * Creates(scaffolds out) a new Griffin directory at the given path.
     *
     * @param path The path at which to scaffold.
     * @param name The name to give to the directory
     * @throws IOException the exception
     */
    public void initialize(Path path, String name) throws IOException {
        checkPathValidity(path, name);

        DirectoryStructure.create(path);

        initializeConfigurationSettings(path, name);

        Initializer init = new Initializer();
        init.scaffold(path, name);
        Data.config.writeConfig(path.resolve(name));
    }

    /**
     * Parses the content of the site in the 'content' directory and produces
     * the output. It parses incrementally i.e it only parses the content which
     * has changed since the last parsing event if the fastParse variable is
     * true.
     *
     * @param fastParse Do a fast incremental parse
     * @param rebuild Do force a full rebuild
     * @param verbose
     * @throws IOException the exception
     */
    public void publish(boolean fastParse, boolean rebuild, boolean verbose) throws IOException {
        long start = System.currentTimeMillis();

        if (cacher.cacheExists() && !rebuild) {
            System.out.println("Reading from the cache for your pleasure...");
            
            ConcurrentMap<String, List<Parsable>> tag = cacher.getTags();
            List<Parsable> qu = cacher.getFileQueue();
            Data.parsables.addAll(qu);
            Data.tags.putAll(tag);
            int st = Data.parsables.size();
            System.out.println("Read " + st + " objects from the cache. Woohooo!!");
            crawler.fastReadIntoQueue(Paths.get(DirectoryStructure.getInstance().SOURCE_DIRECTORY).normalize());
            System.out.println("Found " + (Data.parsables.size() - st) + " new objects!");
        } else {
            if (fastParse && !rebuild) {
                crawler.fastReadIntoQueue(Paths.get(DirectoryStructure.getInstance().SOURCE_DIRECTORY).normalize());
            }
            else {
                System.out.println("Rebuilding site from scratch...");
//                crawler.readIntoQueue(Paths.get(DirectoryStructure.getInstance().SOURCE_DIRECTORY).normalize());

                Path directory = Paths.get(DirectoryStructure.getInstance().ROOT_DIRECTORY);

                Path content = directory.resolve(DirectoryStructure.getInstance().SOURCE_DIRECTORY);

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

        if (Files.notExists(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("SITEMAP.xml"))
                && Files.exists(Paths.get(DirectoryStructure.getInstance().THEMES_DIRECTORY).resolve("SITEMAP.html"))) {
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("SITEMAP.xml"), StandardCharsets.UTF_8)) {
                bw.write(renderer.renderSitemap());
            }
            catch (IOException ex) {
                Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        indexer = new Indexer();
        renderConfig = Configuration.builder().enableSafeMode()
                .forceExtentedProfile()
                .setAllowSpacesInFencedCodeBlockDelimiters(true)
                .setEncoding("UTF-8")
//                .setCodeBlockEmitter(new CodeBlockEmitter())
                .setCodeBlockEmitter(new JygmentsCodeEmitter())
                .build();
        indexer.initIndexes();

        for (Parsable p : parsables) {
            writeParsedFile(p);
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
     * @param rebuild Do force a full rebuild
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

    /**
     * Creates the server and starts a preview at the given port
     *
     * @param port the port number for the server to run on.
     */
    public void preview(Integer port) {
        InternalServer server = new InternalServer(port);
        server.startPreview();
        server.openBrowser();

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {

        }
    }

    private void initializeConfigurationSettings(Path path, String name) throws NumberFormatException, IOException {
        String nam, tag, auth, src, out, date;// = config.getSiteName();//,
        String port;// = config.getPort();

        showWelcomeMessage(path, name);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        System.out.println("1. What would you like to call your site?(" + config.getSiteName() + "):");
        nam = br.readLine();
        System.out.println("2. Who's authoring this site?");
        auth = br.readLine();
        System.out.println("3. What will be the tagline for the site?(" + config.getSiteTagline() + "):");
        tag = br.readLine();
        System.out.println("4. What will you like to name the folder where your posts will be stored?(" + config.getSourceDir() + "):");
        src = br.readLine();
        System.out.println("5. What will you like to name the folder where the generated site will be stored?(" + config.getOutputDir() + "):");
        out = br.readLine();
        System.out.println("6. What will you like to format the dates on yourData. posts and pages as?(" + config.getInputDateFormat() + "):");
        date = br.readLine();
        System.out.println("7. On what port will you like to see the live preview of your site?(" + config.getPort() + "):");
        port = br.readLine();
        finalizeConfigurationSettings(nam, auth, tag, src, out, date, port);
    }

    private void finalizeConfigurationSettings(String nam, String auth, String tag, String src, String out, String date, String port) {
        if (nam != null && !nam.equals(config.getSiteName()) && !nam.equals("")) {
            config.withSiteName(nam);
        }
        if (auth != null && !auth.equals(config.getSiteAuthor()) && !auth.equals("")) {
            config.withSiteAuthour(auth);
        }
        if (tag != null && !tag.equals(config.getSiteTagline()) && !tag.equals("")) {
            config.withSiteTagline(tag);
        }
        if (src != null && !src.equals(config.getSourceDir()) && !src.equals("")) {
            config.withSourceDir(src);
        }
        if (out != null && !out.equals(config.getOutputDir()) && !out.equals("")) {
            config.withOutputDir(out);
        }
        if (date != null && !date.equals(config.getInputDateFormat()) && !date.equals("")) {
            config.withDateFormat(date);
        }
        if (port != null && !port.equals(config.getPort().toString()) && !port.equals("")) {
            config.withPort(Integer.parseInt(port));
        }
    }

    private void showWelcomeMessage(Path path, String name) {
        StringBuilder welcomeMessage = new StringBuilder();
        printAsciiGriffin();
        welcomeMessage.append("Heya! This is griffin. Your personal, fast and easy static site generator. (And an overall good guy)").append(LINE_SEPARATOR);
        welcomeMessage.append("You have chosen to create your new griffin site at: ").append(path.resolve(name).toString()).append(LINE_SEPARATOR);
        welcomeMessage.append("I'd love to help you set up some initial settings for your site, so let's go.").append(LINE_SEPARATOR);
        welcomeMessage.append("I'll ask you a set of simple questions and you can type in your answer. Some questions have a default answer, which will be marked in brackets.").append(LINE_SEPARATOR).append("You can just press enter to accept the default value in those cases.").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        System.out.println(welcomeMessage);
    }

    public void printAsciiGriffin() {
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

    private void checkPathValidity(Path path, String name) throws FileSystemException {
        if (!Files.isWritable(path)) {
            System.out.println("That path doesn't seem to be writable :(" + LINE_SEPARATOR + "Check if you have write permission to that path and try again.");
            throw new java.nio.file.FileSystemException(path.toString());
        }
        if (Files.exists(path.resolve(name))) {
            System.out.println("Aw shucks! It seems like there is already a file of that name at that path :(" + LINE_SEPARATOR + "Try again with another name.");
            throw new FileAlreadyExistsException(path.resolve(name).toString());
        }
        if (!Files.isDirectory(path)) {
            System.out.println("Aw, man. That path does not seem to be a valid directory :(" + LINE_SEPARATOR + "Try with another path again.");
            throw new java.nio.file.NotDirectoryException(path.toString());
        }
    }

    public static void main(String[] args) {
        System.exit(new CommandLine(new Griffin()).execute(args));
    }

    @Override
    public void run() {
        throw new ParameterException(spec.commandLine(), "Missing required subcommand");
    }


    private void renderIndexRssAnd404() throws IOException {
        List<SingleIndex> list = indexer.getIndexList();

        if (Files.notExists(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("index.html"))) {
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("index.html"), StandardCharsets.UTF_8)) {
                bw.write(renderer.renderIndex(list.get(0)));
            }
            catch (IOException ex) {
                Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (Files.notExists(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY, "page"))) {
            Files.createDirectory(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY, "page"));
        }
        list.remove(0);

        for (SingleIndex s : list) {
            Path secondaryIndexPath = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY, "page", "" + (list.indexOf(s) + 2));

            if (Files.notExists(secondaryIndexPath)) {
                Files.createDirectory(secondaryIndexPath);
            }

            try (BufferedWriter bw = Files.newBufferedWriter(secondaryIndexPath.resolve("index.html"), StandardCharsets.UTF_8)) {
                bw.write(renderer.renderIndex(s));
            }
        }

        if (Files.notExists(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("feed.xml")) && Files.exists(Paths.get(DirectoryStructure.getInstance().THEMES_DIRECTORY).resolve("feed.htm;"))) {
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("feed.xml"), StandardCharsets.UTF_8)) {
                bw.write(renderer.renderRssFeed());
            }
            catch (IOException ex) {
                Logger.getLogger(Griffin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (Files.notExists(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY, "404.html")) && Files.exists(Paths.get(DirectoryStructure.getInstance().THEMES_DIRECTORY, "404.html"))) {
            try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve("404.html"), StandardCharsets.UTF_8)) {
                bw.write(renderer.render404());
            }
            catch (IOException ex) {
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
     *
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

    /**
     * Writes the content of the Parsable to the path resolved from the slug by
     * creating a directory from the given slug and then writing the contents
     * into the index.html file inside it for pretty links.
     *
     * @param p the Parsable instance
     */
    void writeParsedFile(Parsable p) throws IOException {
        Path htmlPath = resolveHtmlPath(p);

//        if (config.getRenderTags() && p instanceof Post) {
//            resolveTags(p, htmlPath);
//        }

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

    private static Path resolveHtmlPath(Parsable p) throws IOException {
        String name = p.getSlug();
        Path parsedDir = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve(name);
        if (Files.notExists(parsedDir)) {
            Files.createDirectory(parsedDir);
        }
        return parsedDir.resolve("index.html");

    }
}
