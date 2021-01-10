package com.threecrickets.jygments.contrib;

import com.threecrickets.jygments.ResolutionException;
import com.threecrickets.jygments.grammar.TokenType;
import com.threecrickets.jygments.style.Style;

import static com.threecrickets.jygments.grammar.TokenType.*;

/**
 * @author volyx
 */
public class BasicStyle extends Style
{
	public BasicStyle()
	{
		super();

		add(TokenType.Whitespace.getName(), "bold", "#000080" );
		add( TokenType.Generic_Heading.getName(), "#BA2121" ); // sh
		add( String_Heredoc.getName(), "italic", "#408080" ); // c1
		add( "Comment.Single", "bold", "#008000" ); // k
		add( "Keyword", "#04D" ); // gt
		add( "Generic.Traceback", "italic", "#408080" ); // c
		add( "Comment", "#19177C" ); // nv
		add( "Name.Variable", "#19177C" ); // vi
		add( "Name.Variable.Instance", "#666666" ); // mi
		add( "Number.Integer", "bold", "#000080" ); // gp
		add( "Number.Integer", "bold", "#000080" ); // gp

		add( "Generic.Prompt", "#0000F" ); // nf
		add( "Name.Function", "#666666" ); // o
		add( "Operator", "bold", "#0000FF" ); // nc
		add( "Name.Class", "bold", "#BB6688" ); // si
		add( "String.Interpol", "#BA2121" ); // s2
		add( "String.Double", "#666666" ); // mf
		add( "String.Double", "#666666" ); // mf
		add( "Number.Float", "#FF0000" ); // gr
		add( "Generic.Error", "bold", "#800080" ); // gu
		add( "Generic.Subheading", "bold", "#008000" ); // nb
		add( "Name.Builtin", "bold", "#008000" ); // kd
		add( "Keyword.Declaration", "#B00040" ); // kt
		add( "Keyword.Type", "#008000" ); // sx
		add( "String.Other", "#BA2121" ); // s
		add( "String", "#666666" ); // m
		add( Number.getName(), "#666666" ); // m
		add( Number_Hex.getName(), "#BB6688" ); // mh
		add( String_Regex.getName(), "#7D9029" ); // na
		add( "Name.Attribute", "#BA2121" ); // sb
		add( "String.Backtick", "border", "#FF0000" ); // sb
		add( "Error",  "#A0A000" ); // nl
		add( "Name.Label",  "bold", "#008000" ); // nt
		add( "Name.Tag",  "italic", "#408080" ); // cs
		add( "Comment.Special",   "#19177C" ); // vg
		add( "Name.Variable.Global",   "#19177C" ); // ss
		add( "String.Symbol",   "italic", "#408080" ); // cm
		add( "Comment.Multiline",   "italic" ); // ge
		add( "Generic.Emph",   "#00A000" ); // gi
		add( "Generic.Inserted",   "#BA2121" ); // sc
		add( "Name.Decorator",   "#AA22FF" ); // nd
		add( "String.Char",   "bold", "#008000" ); // kn
		add( "Keyword.Namespace",   "#666666" ); // il

		add( "Number.Integer.Long",   "bold", "#D2413A" ); // ne
		add( "Name.Exception",   "#008000" ); // bp
		add( "Name.Builtin.Pseudo",   "bold", "#0000FF" ); // nn
		add( "Name.Namespace",   "bold", "#999999" ); // ni

		add( "Name.Entity",   "#A00000" ); // gd
		add( "Generic.Deleted" ); // kp
		add( "Keyword.Pseudo" , "#880000"); // no

		add( "Name.Constant" , "bold", "#BB6622"); // se

		add( "String.Escape" , "bold", "#008000"); // kc
		add( "Keyword.Constant" , "bold"); // gs
		add( "Generic.Strong" , "italic"); // sd
		add( "String.Doc" , "#19177C"); // vc
		add( "Name.Variable.Class" , "#666666"); // mo
		add( "Number.Oct" , "bold", "#AA22FF"); // ow
		add( "Operator.Word" , "#BA2121"); // s1
		add( "String.Single" , "#BC7A00"); // cp
		add( "Comment.Preproc" , "#888"); // go
		add( "Generic.Output" , "bold", "#008000"); // kr

		add( "Generic.Output" , "bold", "#008000"); // kr

		try
		{
			resolve();
		}
		catch( ResolutionException x )
		{
			throw new RuntimeException( x );
		}
	}

	@Override
	public java.lang.String getName() {
		return "basic";
	}

	@Override
	public java.lang.String backgroundColor() {
		return null;
	}
}
