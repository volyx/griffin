package com.pawandubey.griffin.markdown;

import com.threecrickets.jygments.ResolutionException;
import com.threecrickets.jygments.Util;
import com.threecrickets.jygments.format.Formatter;
import com.threecrickets.jygments.grammar.Token;
import com.threecrickets.jygments.grammar.TokenType;
import com.threecrickets.jygments.style.ColorStyleElement;
import com.threecrickets.jygments.style.EffectStyleElement;
import com.threecrickets.jygments.style.FontStyleElement;
import com.threecrickets.jygments.style.Style;
import com.threecrickets.jygments.style.StyleElement;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

public class JygmentsInplaceHtmlFormatter extends Formatter {
	public JygmentsInplaceHtmlFormatter() throws ResolutionException
	{
		this( Style.getByName( "default" ), false, null, null );
	}

	public JygmentsInplaceHtmlFormatter(Style style, boolean full, String title, String encoding) {
		super(style, full, title, encoding);
	}

	@Override
	public void format(Iterable<Token> tokenSource, Writer writer) throws IOException {
//		writer.write(getDocHeader3());
//		writer.write(getCssFileTemplate());
//		formatStyleSheet(writer);
//		writer.write(getDocHeader4());
		writer.write("<div><pre>\n");
		StringBuilder line = new StringBuilder();
		int line_no = 1;
		for (Token token : tokenSource) {
			String[] toks = token.getValue().split("\n", -1);
			for (int i = 0; i < toks.length - 1; i++) {
				format_partial_token(token, toks[i], line);
				format_line(line.toString(), writer, line_no++);
				line = new StringBuilder();
			}
			format_partial_token(token, toks[toks.length - 1], line);
		}
		if (line.length() > 0)
			format_line(line.toString(), writer, line_no++);

		writer.write("</pre></div>\n");
//		writer.write(getDocFooter());
//		writer.flush();
	}

	private void format_partial_token(Token token, String part_tok, StringBuilder line) {
		if (token.getType().getShortName().length() > 0) {
			line.append("<span class=\"");
			line.append(token.getType().getShortName());
			line.append("\">");
			line.append(Util.escapeHtml(part_tok));
			line.append("</span>");
		} else
			line.append(Util.escapeHtml(part_tok));
	}

	public void format_line(String line, Writer writer, int line_no) throws IOException {
		writer.write(line);
		writer.write("\n");
	}


	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected String getClassPrefix() {
		return "";
	}

	protected String getCssFileTemplate() {
		return "td.linenos { background-color: #f0f0f0; padding-right: 10px; }\n"
				+ "    span.lineno { background-color: #f0f0f0; padding: 0 5px 0 5px; }\n"
				+ "    pre { line-height: 125%; }\n";
	}

	protected String getDocHeader1() {
		return "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">\n" + "<html>\n" + "<head>\n";
	}

	protected String getDocHeader2() {
		return "  <meta http-equiv=\"content-type\" content=\"text/html; charset=";
	}

	protected String getDocHeader3() {
		return "\">\n" + "  <style type=\"text/css\">\n";
	}

	protected String getDocHeader4() {
		return "  </style>\n" + "</head>\n" + "<body>\n";
	}

	/*
	 * private static final String DOC_HEADER_EXTERNAL_CSS =
	 * "<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 4.01//EN\"\n" +
	 * "   \"http://www.w3.org/TR/html4/strict.dtd\">\n" + "\n" + "<html>\n" +
	 * "<head>\n" + "  <title>%(title)s</title>\n" +
	 * "  <meta http-equiv=\"content-type\" content=\"text/html; charset=%(encoding)s\">\n"
	 * + "  <link rel=\"stylesheet\" href=\"%(cssfile)s\" type=\"text/css\">\n"
	 * + "</head>\n" + "<body>\n" + "<h2>%(title)s</h2>\n";
	 */

	protected String getDocFooter() {
		return "</body>\n" + "</html>\n";
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private void formatStyleSheet(Writer writer) throws IOException {
		for (Map.Entry<TokenType, List<StyleElement>> entry : getStyle().getStyleElements().entrySet()) {
			TokenType tokenType = entry.getKey();
			List<StyleElement> styleElementsForTokenType = entry.getValue();
			writer.write("    .");
			writer.write(getClassPrefix());
			writer.write(tokenType.getShortName());
			writer.write(" { ");
			for (StyleElement styleElement : styleElementsForTokenType) {
				if (styleElement instanceof ColorStyleElement) {
					ColorStyleElement colorStyleElement = (ColorStyleElement) styleElement;
					if (colorStyleElement.getType() == ColorStyleElement.Type.Foreground)
						writer.write("color: ");
					else if (colorStyleElement.getType() == ColorStyleElement.Type.Background)
						writer.write("background-color: ");
					else if (colorStyleElement.getType() == ColorStyleElement.Type.Border)
						writer.write("border: 1px solid ");
					writer.write(colorStyleElement.getColor());
					writer.write("; ");
				} else if (styleElement instanceof EffectStyleElement) {
					if (styleElement == EffectStyleElement.Bold)
						writer.write("font-weight: bold; ");
					else if (styleElement == EffectStyleElement.Italic)
						writer.write("font-style: italic; ");
					else if (styleElement == EffectStyleElement.Underline)
						writer.write("text-decoration: underline; ");
				} else if (styleElement instanceof FontStyleElement) {
					// We don't want to set fonts in this formatter
				}
			}
			writer.write("} /* ");
			writer.write(tokenType.getName());
			writer.write(" */\n");
		}
	}
}
