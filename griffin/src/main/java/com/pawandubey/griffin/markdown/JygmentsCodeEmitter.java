package com.pawandubey.griffin.markdown;

import com.github.rjeschke.txtmark.BlockEmitter;
import com.threecrickets.jygments.ResolutionException;
import com.threecrickets.jygments.contrib.InplaceHtmlFormatter;
import com.threecrickets.jygments.format.Formatter;
import com.threecrickets.jygments.grammar.Lexer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;

public class JygmentsCodeEmitter implements BlockEmitter {

	private final BlockEmitter fallback = new CodeBlockEmitter();
	private final Formatter formatter;

	public JygmentsCodeEmitter() {
		try {
			this.formatter = new InplaceHtmlFormatter();
		} catch (ResolutionException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void emitBlock(StringBuilder out, List<String> lines, String meta) {
		if (meta == null || meta.isEmpty()) {
			fallback.emitBlock(out, lines, meta);
			return;
		}

		try {
			Lexer lexer = Lexer.getByName(meta);
			Objects.requireNonNull(lexer);
			String code = String.join("\n", lines);
			final StringWriter writer = new StringWriter();
			formatter.format(lexer.getTokens(code), writer);
			final String result = writer.toString();

			out.append(result);
		} catch (IOException | ResolutionException e) {
			e.printStackTrace();
			fallback.emitBlock(out, lines, meta);
		}
	}
}
