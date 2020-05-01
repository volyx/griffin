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

import com.pawandubey.griffin.model.Content;
import com.pawandubey.griffin.model.Parsable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static com.pawandubey.griffin.Data.config;

/**
 *
 * @author Pawan Dubey pawandubey@outlook.com
 */
public class DirectoryCrawler {


    public static final String EXCERPT_MARKER = "##more##";

    public DirectoryCrawler() {

    }

    /**
     * Crawls the whole content directory and adds the files to the main queue
     * for parsing.
     *
     * @param rootPath path to the content directory
     * @throws IOException the exception
     */
//    protected void readIntoQueue(Path rootPath) throws IOException {
//
//        cleanOutputDirectory();
//        copyTemplateAssets();
//
//        Files.walkFileTree(rootPath, new FileVisitor<Path>() {
//
//            @Override
//            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                Path correspondingOutputPath = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve(rootPath.relativize(dir));
//                if (config.getExcludeDirs().contains(dir.getFileName().toString())) {
//                    return FileVisitResult.SKIP_SUBTREE;
//                }
//                if (Files.notExists(correspondingOutputPath)) {
//                    Files.createDirectory(correspondingOutputPath);
//                }
//
//                return FileVisitResult.CONTINUE;
//            }
//
//            @Override
//            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
//                Path resolvedPath = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve(rootPath.relativize(file));
//
//                if (file.getFileName().toString().endsWith(".md")) {
//
//                    Parsable parsable = Content.createParsable(file);
//                    Data.parsables.add(parsable);
//                }
//                else {
//                    Files.copy(file, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
//                }
//
//                return FileVisitResult.CONTINUE;
//            }
//
//            @Override
//            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
//                return FileVisitResult.TERMINATE;
//            }
//
//            @Override
//            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
//                return FileVisitResult.CONTINUE;
//            }
//        });
//    }

    //TODO refactor this method to make use of the above method someway.
    /**
     * Checks if the file has been modified after the last parse event and only
     * then adds the file into the queue for parsing, hence saving time.
     *
     * @param rootPath
     * @throws IOException the exception
     */
    protected void fastReadIntoQueue(Path rootPath) throws IOException {
//        cleanOutputDirectory();
//        copyTemplateAssets();

        Files.walkFileTree(rootPath, new FileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path correspondingOutputPath = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve(rootPath.relativize(dir));
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
                LocalDateTime fileModified = LocalDateTime.ofInstant(Files.getLastModifiedTime(file).toInstant(), ZoneId.systemDefault());
                LocalDateTime lastParse = LocalDateTime.parse(InfoHandler.LAST_PARSE_DATE, InfoHandler.formatter);
                Path resolvedPath = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY).resolve(rootPath.relativize(file));
                if (file.getFileName().toString().endsWith(".md")) {
                    if (fileModified.isAfter(lastParse)) {
                        Parsable parsable = Content.createParsable(file);
                        Data.parsables.removeIf(p -> p.getPermalink().equals(parsable.getPermalink()));
                        Data.parsables.add(parsable);
                    }
                }
                else {
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
