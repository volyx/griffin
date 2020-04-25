package com.pawandubey.griffin.pipeline;

import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * A {@code Predicate} that filters content files ending with the markdown extension.
 */
public class ContentFilter implements Predicate<Path> {

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean test(Path path) {
    return path.toString().endsWith(".md");
  }

}