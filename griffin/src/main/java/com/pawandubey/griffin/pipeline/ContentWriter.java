package com.pawandubey.griffin.pipeline;

import com.pawandubey.griffin.model.Parsable;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

/**
 * A {@code Consumer} that writes {@Content} to disk.
 */
public class ContentWriter implements Consumer<Parsable> {

  private final Path targetDirectory;

  /**
   * Creates a new {@code ContentWriter}
   */
  public ContentWriter(Path targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(Parsable content) {
    Path path = targetDirectory.resolve(content.getLocation());
    try (Writer writer = writer(path)) {
      writer.write(content.getBody());
      System.out.println(String.format("Write %s", content.getLocation()));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private Writer writer(Path path) throws IOException {
    Files.createDirectories(path.getParent());
    return Files.newBufferedWriter(path,
        StandardOpenOption.CREATE,
        StandardOpenOption.TRUNCATE_EXISTING);
  }

}