package com.pawandubey.griffin.pipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * The {@code AssetCopier} copies files from a source directory to a target directory.
 */
public class FileCopier implements Consumer<Path> {

  private final Path sourceDirectory;
  private final Path targetDirectory;

  /**
   * Creates a new AssetCopier with a source and a target directory.
   */
  public FileCopier(Path sourceDirectory, Path targetDirectory) {
    this.sourceDirectory = sourceDirectory;
    this.targetDirectory = targetDirectory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void accept(Path sourceFile) {
    try {
      if (Files.isRegularFile(sourceFile)) {
        Path path = sourceDirectory.relativize(sourceFile);
        Path targetFile = targetDirectory.resolve(path);
        Files.createDirectories(targetFile.getParent());
        Files.copy(sourceFile, targetFile);
        System.out.println(String.format("Copy %s", path));
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}