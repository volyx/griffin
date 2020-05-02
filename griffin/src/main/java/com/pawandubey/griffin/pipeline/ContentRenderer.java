package com.pawandubey.griffin.pipeline;


import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.pawandubey.griffin.markdown.JygmentsCodeEmitter;
import com.pawandubey.griffin.model.Parsable;
import com.pawandubey.griffin.renderer.Renderer;

import java.io.IOException;
import java.util.function.Function;

/**
 * A {@code Function} that renders a markdown {@code Content} into a HTML {@code Content} with handlebars templates.
 */
public class ContentRenderer implements Function<Parsable, Parsable> {

	private final Renderer renderer;
	private Configuration renderConfig;

	/**
	 * Creates a new {@ContentRenderer} with a template directory.
	 */
	public ContentRenderer(Renderer renderer) {
		this.renderer = renderer;
	}

	/**
	 * {@code }
	 */
	@Override
	public Parsable apply(Parsable parsable) {

		renderConfig = Configuration.builder().enableSafeMode()
				.forceExtentedProfile()
				.setAllowSpacesInFencedCodeBlockDelimiters(true)
				.setEncoding("UTF-8")
//                .setCodeBlockEmitter(new CodeBlockEmitter())
				.setCodeBlockEmitter(new JygmentsCodeEmitter())
				.build();


		String parsedContent = Processor.process(parsable.getContent(), renderConfig);
		parsable.setContent(parsedContent);
		try {
			parsable.setContent(renderer.renderParsable(parsable));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return parsable;
	}

}