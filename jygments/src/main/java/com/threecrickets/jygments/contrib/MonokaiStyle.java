package com.threecrickets.jygments.contrib;

import com.threecrickets.jygments.ResolutionException;
import com.threecrickets.jygments.style.Style;

/**
 * @author volyx
 */
public class MonokaiStyle extends Style
{
	public MonokaiStyle()
	{
		super();
		add( "Text", "#f8f8f2");
//		add( "Whitespace", "");
		add( "Error", "#960050","bg:#1e0010");
//		add( "Other", "");

		add( "Comment", "#75715e");
//		add( "Comment.Multiline", "");
//		add( "Comment.Preproc", "");
//		add( "Comment.Single", "");
//		add( "Comment.Special", "");

		add( "Keyword", "#66d9ef");
		add( "Keyword.Constant", "");
//		add( "Keyword.Declaration", "");
		add( "Keyword.Namespace", "#f92672");
//		add( "Keyword.Pseudo", "");
//		add( "Keyword.Reserved", "");
//		add( "Keyword.Type", "");

		add( "Operator", "#f92672");
//		add( "Operator.Word", "");

		add( "Punctuation", "#f8f8f2");

		add( "Name", "#f8f8f2");
		add( "Name.Attribute", "#a6e22e");
//		add( "Name.Builtin", "");
//		add( "Name.Builtin.Pseudo", "");
		add( "Name.Class", "#a6e22e");
		add( "Name.Constant", "#66d9ef");
		add( "Name.Decorator", "#a6e22e");
//		add( "Name.Entity", "");
		add( "Name.Exception", "#a6e22e");
		add( "Name.Function", "#a6e22e");
//		add( "Name.Property", "");
//		add( "Name.Label", "");
//		add( "Name.Namespace", "");
		add( "Name.Other", "#a6e22e");
		add( "Name.Tag", "#f92672");
//		add( "Name.Variable", "");
//		add( "Name.Variable.Class", "");
//		add( "Name.Variable.Global", "");
//		add( "Name.Variable.Instance", "");

		add( "Number", "#ae81ff");
//		add( "Number.Float", "");
//		add( "Number.Hex", "");
//		add( "Number.Integer", "");
//		add( "Number.Integer.Long", "");
//		add( "Number.Oct", "");

		add( "Literal", "#ae81ff");
		add( "Literal.Date", "#e6db74");

		add( "String", "#e6db74");
//		add( "String.Backtick", "");
//		add( "String.Char", "");
//		add( "String.Doc", "");
//		add( "String.Double", "");
		add( "String.Escape", "#ae81ff");
//		add( "String.Heredoc", "");
//		add( "String.Interpol", "");
//		add( "String.Other", "");
//		add( "String.Regex", "");
//		add( "String.Single", "");
//		add( "String.Symbol", "");

//		add( "Generic", "");
		add( "Generic.Deleted", "#f92672");
		add( "Generic.Emph", "italic");
//		add( "Generic.Error", "");
//		add( "Generic.Heading", "");
		add( "Generic.Inserted", "#a6e22e");
		add( "Generic.Output", "#66d9ef");
		add( "Generic.Prompt", "bold","#f92672");
		add( "Generic.Strong", "bold");
		add( "Generic.Subheading", "#75715e");
//		add( "Generic.Traceback", "");

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
		return "monokai";
	}

	@Override
	public java.lang.String backgroundColor() {
		return  "#272822";
	}

	@Override
	public java.lang.String lineHeight() {
		return  "line-height: 125%";
	}
}
