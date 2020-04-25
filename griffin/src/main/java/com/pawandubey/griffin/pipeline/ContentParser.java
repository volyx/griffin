package com.pawandubey.griffin.pipeline;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.rjeschke.txtmark.Run;
import com.pawandubey.griffin.model.Content;
import com.pawandubey.griffin.model.Parsable;

/**
 * A {@code Function} that transforms a file into a piece of {@code Parsable}.
 */
public class ContentParser implements Function<Path, Parsable> {

  private static final Pattern REGEX = Pattern.compile("^-{3}\n(.*)-{3}\n(.*)", Pattern.DOTALL);
//
  private final Path sourceDirectory;

  /**
   * Creates a new {@code ContentWriter}
   */
  public ContentParser(Path sourceDirectory) {
    this.sourceDirectory = sourceDirectory;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Parsable apply(Path sourceFile) {
    try {
      String text = new String(Files.readAllBytes(sourceFile));
      Matcher matcher = REGEX.matcher(text);
      if (!matcher.find()) {
//        return new Content(sourceFile, new HashMap(), text);
        throw new RuntimeException("");
      }
      String header = matcher.group(1);
      String body = matcher.group(2);

      return Content.parse(sourceFile, header, body);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}