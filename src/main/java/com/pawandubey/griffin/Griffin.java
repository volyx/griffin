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
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.ParameterException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

import static com.pawandubey.griffin.Configurator.LINE_SEPARATOR;
import static com.pawandubey.griffin.Data.config;
import static com.pawandubey.griffin.Data.fileQueue;

/**
 *
 * @author Pawan Dubey pawandubey@outlook.com
 */
@Command(name = "griffin",
        version = "griffin 0.3.1",
        mixinStandardHelpOptions = true,
        synopsisSubcommandLabel = "COMMAND",
        subcommands = {
                NewCommand.class,
                PublishCommand.class,
                PreviewCommand.class
        },
        description = "a simple and fast static site generator. ")
public class Griffin implements Runnable {

    private final DirectoryCrawler crawler;
    private Parser parser;
    private Cacher cacher;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    /**
     * Creates a new instance of Griffin
     */
    private Griffin() {
        this.crawler = new DirectoryCrawler();
    }

    /**
     * Creates a new instance of Griffin with the root directory of the site set
     * to the given path.
     *
     * @param path The path to the root directory of the griffin site.
     */
    public Griffin(Path path) {
        this.crawler = new DirectoryCrawler(path.toString());
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
     * @throws IOException the exception
     * @throws InterruptedException the exception
     */
    public void publish(boolean fastParse, boolean rebuild) throws IOException, InterruptedException {
        long start = System.currentTimeMillis();
        InfoHandler info = new InfoHandler();
        if (cacher.cacheExists() && !rebuild) {
            System.out.println("Reading from the cache for your pleasure...");
            
            ConcurrentMap<String, List<Parsable>> tag = cacher.getTags();
            List<Parsable> qu = cacher.getFileQueue();
            Data.fileQueue.addAll(qu);
            Data.tags.putAll(tag);
            int st = Data.fileQueue.size();
            System.out.println("Read " + st + " objects from the cache. Woohooo!!");
            crawler.fastReadIntoQueue(Paths.get(DirectoryCrawler.SOURCE_DIRECTORY).normalize());
            System.out.println("Found " + (Data.fileQueue.size() - st) + " new objects!");
        } else {
            if (fastParse && !rebuild) {
                crawler.fastReadIntoQueue(Paths.get(DirectoryCrawler.SOURCE_DIRECTORY).normalize());
            }
            else {
                System.out.println("Rebuilding site from scratch...");
                crawler.readIntoQueue(Paths.get(DirectoryCrawler.SOURCE_DIRECTORY).normalize());
            }          
        }
        info.findLatestPosts(fileQueue);
        info.findNavigationPages(fileQueue);
        cacher.cacheFileQueue(fileQueue);
        System.out.println("Parsing " + Data.fileQueue.size() + " objects...");
        
        parser = new Parser();
        parser.parse(fileQueue);
        info.writeInfoFile();
        parser.shutDownExecutors();
        cacher.cacheTaggedParsables(Data.tags);
        
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
}
