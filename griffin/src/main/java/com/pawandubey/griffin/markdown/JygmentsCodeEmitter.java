package com.pawandubey.griffin.markdown;

import com.github.rjeschke.txtmark.BlockEmitter;
import com.threecrickets.jygments.ResolutionException;
import com.threecrickets.jygments.format.Formatter;
import com.threecrickets.jygments.grammar.Lexer;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public class JygmentsCodeEmitter implements BlockEmitter {

	private BlockEmitter fallback = new CodeBlockEmitter();
	private Formatter formatter;

	{
		try {
			formatter = new JygmentsInplaceHtmlFormatter();
		} catch (ResolutionException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void emitBlock(StringBuilder out, List<String> lines, String meta) {
		if (meta == null || meta.isEmpty()) {
			fallback.emitBlock(out, lines, meta);
			return;
		}
		Lexer lexer = null;
		try {
			lexer = Lexer.getByName(meta);
			String code = String.join("\n", lines);
			final StringWriter writer = new StringWriter();
			formatter.format(lexer.getTokens(code), writer);
			final String result = writer.toString();

			out.append(result);
		} catch (ResolutionException | IOException e) {
			e.printStackTrace();
			fallback.emitBlock(out, lines, meta);
		}
	}
}
