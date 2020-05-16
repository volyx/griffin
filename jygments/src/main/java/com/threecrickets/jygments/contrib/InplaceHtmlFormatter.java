/**
 * Copyright 2010-2016 Three Crickets LLC.
 * <p>
 * The contents of this file are subject to the terms of a BSD license. See
 * attached license.txt.
 * <p>
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly from Three Crickets
 * at http://threecrickets.com/
 */

package com.threecrickets.jygments.contrib;

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

/**
 * @author Tal Liron
 */
public class InplaceHtmlFormatter extends Formatter
{
	//
	// Construction
	//

	public InplaceHtmlFormatter() throws ResolutionException
	{
		this( Style.getByName( "default2" ), false, null, null );
	}

	public InplaceHtmlFormatter(Style style, boolean full, String title, String encoding )
	{
		super( style, full, title, encoding );
	}

	//
	// Formatter
	//

	@Override
	public void format( Iterable<Token> tokenSource, Writer writer ) throws IOException
	{
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
		writer.flush();
	}

    private void format_partial_token(Token token, String part_tok, StringBuilder line)
    {	
		if( token.getType().getShortName().length() > 0 )
		{
			line.append( "<span class=\"" );
			line.append( token.getType().getShortName() );
			line.append( "\">" );
			line.append( Util.escapeHtml( part_tok ) );
			line.append( "</span>" );
		}
		else
			line.append( Util.escapeHtml( part_tok ) );
    }
	
	public void format_line(String line, Writer writer, int line_no) throws IOException
    {
        writer.write(line);
        writer.write("\n");
    }
}
