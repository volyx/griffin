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

import com.pawandubey.griffin.DirectoryCrawler;
import com.pawandubey.griffin.DirectoryStructure;
import com.pawandubey.griffin.Griffin;
import com.pawandubey.griffin.cache.Cacher;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pawan Dubey pawandubey@outlook.com
 */
@Command(name = "publish", mixinStandardHelpOptions = true,
        description = "Publish the content in the current Griffin directory.")
public class PublishCommand implements Callable<Integer> {

    @Option(names = {"--quick", "-q"}, description = "Publish only the files which have changed since the last modification."
    )
    private boolean fastParse = false;

    @Option(names = {"--rebuild", "-r"}, description = "Rebuild the site from scratch. This may take time for more number of posts.")
    private boolean rebuild = false;

    @Option(names = {"--verbose", "-v"}, description = "Verbose logging")
    private boolean verbose = false;

    @Option(names = {"--source", "-s"}, description = "Filesystem path to read files relative from")
    private Path source;

    @Override
    public Integer call() {
        try {
            DirectoryStructure.create(source);
            Griffin griffin = new Griffin(Cacher.getCacher());
            griffin.printAsciiGriffin();
            griffin.publish(fastParse, rebuild, verbose);
            System.out.println("All done for now! I will be bach!");
        }
        catch (IOException ex) {
            Logger.getLogger(PublishCommand.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }
        return 0;
    }
}
