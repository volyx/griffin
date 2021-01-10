package com.pawandubey.griffin.pipeline;


import com.github.rjeschke.txtmark.BlockEmitter;
import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;
import com.pawandubey.griffin.Configurator;
import com.pawandubey.griffin.markdown.CodeBlockEmitter;
import com.pawandubey.griffin.markdown.JygmentsCodeEmitter;
import com.pawandubey.griffin.model.Parsable;
import com.pawandubey.griffin.renderer.Renderer;
import com.threecrickets.jygments.contrib.InplaceStyleHtmlFormatter;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Function;

/**
 * A {@code Function} that renders a markdown {@code Content} into a HTML {@code Content} with handlebars templates.
 */
public class ContentRenderer implements Function<Parsable, Parsable> {

	private final Renderer renderer;
	private final Configuration renderConfig;

	/**
	 * Creates a new {@ContentRenderer} with a template directory.
	 */
	public ContentRenderer(Renderer renderer, Configurator config) {
		this.renderer = renderer;
		BlockEmitter blockEmitter = config.getCode().equals(Configurator.Code.block) ?
				new CodeBlockEmitter(): new JygmentsCodeEmitter(new InplaceStyleHtmlFormatter());

		this.renderConfig = Configuration.builder().enableSafeMode()
				.forceExtentedProfile()
				.setAllowSpacesInFencedCodeBlockDelimiters(true)
				.setEncoding("UTF-8")
				.setCodeBlockEmitter(blockEmitter)
				.build();

	}

	/**
	 * {@code }
	 */
	@Override
	public Parsable apply(Parsable parsable) {
		String parsedContent = Processor.process(parsable.getContent(), renderConfig);
		parsable.setContent(parsedContent);
		try {
			parsable.setContent(renderer.renderParsable(parsable));
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return parsable;
	}

}