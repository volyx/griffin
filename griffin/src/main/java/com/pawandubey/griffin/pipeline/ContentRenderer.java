package com.pawandubey.griffin.pipeline;


import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.CompositeTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import com.pawandubey.griffin.model.Parsable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Function;

/**
 * A {@code Function} that renders a markdown {@code Content} into a HTML {@code Content} with handlebars templates.
 */
public class ContentRenderer implements Function<Parsable, Parsable> {

  private final Path templateDirectory;

  /**
   * Creates a new {@ContentRenderer} with a template directory.
   */
  public ContentRenderer(Path templateDirectory) {
    this.templateDirectory = templateDirectory;
  }

  /**
   * {@code }
   */
  @Override
  public Parsable apply(Parsable content) {
//    try {
//      var loader = new CompositeTemplateLoader(new FileTemplateLoader(this.templateDirectory.toFile()), new ClassPathTemplateLoader());
//      var handlebars = new Handlebars(loader);
//      handlebars.registerHelper("md", new MarkdownHelper());
//      handlebars.setPrettyPrint(true);
//      var layout = (String) content
//          .headers
//          .getOrDefault("layout", "default");
//      var template = handlebars
//          .compile(layout);
//      var context = Context
//          .newBuilder(content)
//          .resolver(ContentResolver.INSTANCE, MapValueResolver.INSTANCE)
//          .build();
//      var result = template.apply(context);
//      var html = content
//          .path
//          .getFileName()
//          .toString()
//          .replace(markdownExtension, htmlExtension);
//      var path = content
//          .path
//          .resolveSibling(html);
//      return new Content(path, content.headers, result);
//    } catch (IOException e) {
//      throw new RuntimeException(e);
//    }
    return null;
  }

}