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
package com.pawandubey.griffin.cli;

import com.pawandubey.griffin.DirectoryStructure;
import com.pawandubey.griffin.Griffin;
import com.pawandubey.griffin.cache.Cacher;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.pawandubey.griffin.Data.config;

/**
 *
 * @author Pawan Dubey pawandubey@outlook.com
 */
@Command(name = "preview", mixinStandardHelpOptions = true,
        description = "Preview the site on the given port: default: 9090")
public class PreviewCommand implements Callable<Integer> {

    @Option(names = {"--port", "-p"}, paramLabel = "<PORT>", description = "Port on which to launch the preview. Default to your configuredData. port.")
    private Integer port;

    @Option(names = {"--source", "-s"}, description = "Filesystem path to read files relative from")
    private Path source;

    @Override
    public Integer call() {
        try {
            DirectoryStructure.create(source);
            if (port == null) {
                port = config.getPort();
            }

            Griffin griffin = new Griffin(Cacher.getCacher());
            griffin.printAsciiGriffin();
            System.out.println("Starting preview on port " + port);
            griffin.preview(port);
        }
        catch (Exception ex) {
            Logger.getLogger(PublishCommand.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }

        return 0;
    }
}
