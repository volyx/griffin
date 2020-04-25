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

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Class to track some meta information about the parsing process, including,
 * but not limited to the last parse time and date, and the latest posts etc.
 *
 * @author Pawan Dubey pawandubey@outlook.com
 */
public class InfoHandler {

    public static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MM dd HH:mm:ss");
    static String LAST_PARSE_DATE;

    public InfoHandler() throws IOException {
        final Path infoFilePath = Paths.get(DirectoryStructure.getInstance().INFO_FILE);
        if (!Files.exists(infoFilePath)) {
            throw new IOException(DirectoryStructure.getInstance().INFO_FILE + " doesn't exist");
        }
        try (BufferedReader br = Files.newBufferedReader(infoFilePath,
                                                         StandardCharsets.UTF_8)) {
            LAST_PARSE_DATE = br.readLine();
        }
        catch (IOException ex) {
            Logger.getLogger(InfoHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    /**
     * Writes the info file with the parsing timestamp and a list of paths to
     * the latest posts.
     */
    protected void writeInfoFile() {
        Path infoFilePath = Paths.get(DirectoryStructure.getInstance().INFO_FILE);

        List<String> lines = new ArrayList<>();
        lines.add(calculateTimeStamp());

        lines.addAll(Data.latestPosts.stream()
                .map(p -> p.getLocation().toAbsolutePath().toString()).collect(Collectors.toSet()));

        try  {
            Files.write(infoFilePath, lines,  StandardCharsets.UTF_8,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        }
        catch (IOException ex) {
            Logger.getLogger(InfoHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /**
     * Calculates the current time stamp according to the System's default
     * timezone and formats it as per the given formatter.
     *
     * @return the string representation of the timestamp
     */
    private String calculateTimeStamp() {
        return LocalDateTime.now().format(formatter);
    }
}
