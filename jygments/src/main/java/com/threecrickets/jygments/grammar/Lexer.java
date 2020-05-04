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

package com.threecrickets.jygments.grammar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.threecrickets.jygments.Filter;
import com.threecrickets.jygments.Jygments;
import com.threecrickets.jygments.ResolutionException;
import com.threecrickets.jygments.Util;
import com.threecrickets.jygments.grammar.def.ChangeStateTokenRuleDef;
import com.threecrickets.jygments.grammar.def.IncludeDef;
import com.threecrickets.jygments.grammar.def.TokenRuleDef;

/**
 * @author Tal Liron
 */
public class Lexer extends Grammar
{
	//
	// Static operations
	//

	public static Lexer getByName( String name ) throws ResolutionException
	{
		if( ( name == null ) || ( name.length() == 0 ) )
			name = "Lexer";
		else if( Character.isLowerCase( name.charAt( 0 ) ) )
			name = Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 ) + "Lexer";

		Lexer lexer = getByFullName( name );
		if( lexer != null )
			return lexer;
		else
		{
			// Try contrib package
			String pack = Jygments.class.getPackage().getName() + ".contrib";
			lexer = getByFullName( pack + "." + name );
			if( lexer == null )
			{
				// Try this package
				pack = Lexer.class.getPackage().getName();
				lexer = getByFullName( pack + "." + name );
			}
			return lexer;
		}
	}

	@SuppressWarnings("unchecked")
	public static Lexer getByFullName( String fullName ) throws ResolutionException
	{
		// Try cache
		Lexer lexer = lexers.get( fullName );
		if( lexer != null )
			return lexer;

		try
		{
			return (Lexer) Jygments.class.getClassLoader().loadClass( fullName ).newInstance();
		}
		catch( InstantiationException x )
		{
		}
		catch( IllegalAccessException x )
		{
		}
		catch( ClassNotFoundException x )
		{
		}

		InputStream stream = Jygments.class.getClassLoader().getResourceAsStream( fullName.replace( '.', '/' ) + ".json" );
		if( stream != null )
		{
			try
			{
				String converted = Util.rejsonToJson( stream );
				ObjectMapper objectMapper = new ObjectMapper();
				objectMapper.getFactory().configure( JsonParser.Feature.ALLOW_COMMENTS, true );
				JsonNode jsonNode = objectMapper.readTree( converted);
				String className = jsonNode.has( "class" ) ? jsonNode.get("class").asText("") : "";

				lexer = getByName(className);
				Objects.requireNonNull(lexer, "lexer is null " + className);
				lexer.addJsonTree( jsonNode );
				lexer.resolve();

				if( lexer != null )
				{
					// Cache it
					Lexer existing = lexers.putIfAbsent( fullName, lexer );
					if( existing != null )
						lexer = existing;
				}

				return lexer;
			}
			catch( JsonParseException x )
			{
				throw new ResolutionException( x );
			}
			catch( JsonMappingException x )
			{
				throw new ResolutionException( x );
			}
			catch( IOException x )
			{
				throw new ResolutionException( x );
			}
		}

		return null;
	}

	public static Lexer getForFileName( String fileName ) throws ResolutionException
	{
		if( lexerMap.isEmpty() )
		{
			try
			{
				File jarFile = new File( Jygments.class.getProtectionDomain().getCodeSource().getLocation().toURI() );
				JarInputStream jarInputStream = new JarInputStream( new FileInputStream( jarFile ) );
				try
				{
					for( JarEntry jarEntry = jarInputStream.getNextJarEntry(); jarEntry != null; jarEntry = jarInputStream.getNextJarEntry() )
					{
						if( jarEntry.getName().endsWith( ".json" ) )
						{
							String lexerName = jarEntry.getName();
							// strip off the .json
							lexerName = lexerName.substring( 0, lexerName.length() - 5 );
							Lexer lexer = Lexer.getByFullName( lexerName );
							for( String filename : lexer.filenames )
								if( filename.startsWith( "*." ) )
									lexerMap.put( filename.substring( filename.lastIndexOf( '.' ) ), lexer );
						}
					}
				}
				finally
				{
					jarInputStream.close();
				}
			}
			catch( URISyntaxException x )
			{
				throw new ResolutionException( x );
			}
			catch( FileNotFoundException x )
			{
				throw new ResolutionException( x );
			}
			catch( IOException x )
			{
				throw new ResolutionException( x );
			}
		}

		return lexerMap.get( fileName.substring( fileName.lastIndexOf( '.' ) ) );
	}

	//
	// Construction
	//

	public Lexer()
	{
		this( false, false, 4, "utf8" );
	}

	public Lexer( boolean stripNewlines, boolean stripAll, int tabSize, String encoding )
	{
		this.stripNewLines = stripNewlines;
		this.stripAll = stripAll;
		this.tabSize = tabSize;
	}

	//
	// Attributes
	//

	public List<Filter> getFilters()
	{
		return filters;
	}

	public boolean isStripNewLines()
	{
		return stripNewLines;
	}

	public void setStripNewLines( boolean stripNewLines )
	{
		this.stripNewLines = stripNewLines;
	}

	public boolean isStripAll()
	{
		return stripAll;
	}

	public void setStripAll( boolean stripAll )
	{
		this.stripAll = stripAll;
	}

	public int getTabSize()
	{
		return tabSize;
	}

	public void setTabSize( int tabSize )
	{
		this.tabSize = tabSize;
	}

	public void addFilter( Filter filter )
	{
		filters.add( filter );
	}

	public float analyzeText( String text )
	{
		return 0;
	}

	public Iterable<Token> getTokens( String text )
	{
		return getTokens( text, false );
	}

	public Iterable<Token> getTokens( String text, boolean unfiltered )
	{
		// text = text.replace( "\r\n", "\n" ).replace( "\r", "\n" );
		// if( stripAll )
		// text = text.trim();
		// if( stripNewLines )
		// text = text.replace( "\n", "" );
		if( tabSize > 0 )
		{
			// expand tabs
		}
		if( !text.endsWith( "\n" ) )
			text += "\n";
		Iterable<Token> tokens = getTokensUnprocessed( text );
		if( !unfiltered )
		{
			// apply filters
		}
		return tokens;
	}

	public Iterable<Token> getTokensUnprocessed( String text )
	{
		ArrayList<Token> list = new ArrayList<Token>( 1 );
		list.add( new Token( 0, TokenType.Text, text ) );
		return list;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected void addAlias( String alias )
	{
		aliases.add( alias );
	}

	protected void addFilename( String filename )
	{
		filenames.add( filename );
	}

	protected void addMimeType( String mimeType )
	{
		mimeTypes.add( mimeType );
	}

	protected void include( String stateName, String includedStateName )
	{
		getState( stateName ).addDef( new IncludeDef( stateName, includedStateName ) );
	}

	protected void rule( String stateName, String pattern, int flags, String tokenTypeName )
	{
		getState( stateName ).addDef( new TokenRuleDef( stateName, pattern, flags, tokenTypeName ) );
	}

	protected void rule( String stateName, String pattern, int flags, String tokenTypeName, String nextStateName )
	{
		getState( stateName ).addDef( new ChangeStateTokenRuleDef( stateName, pattern, flags, new String[]
		{
			tokenTypeName
		}, nextStateName ) );
	}

	protected void rule( String stateName, String pattern, int flags, String[] tokenTypeNames )
	{
		getState( stateName ).addDef( new TokenRuleDef( stateName, pattern, flags, tokenTypeNames ) );
	}

	protected void rule( String stateName, String pattern, int flags, String[] tokenTypeNames, String... nextStateNames )
	{
		getState( stateName ).addDef( new ChangeStateTokenRuleDef( stateName, pattern, flags, tokenTypeNames, nextStateNames ) );
	}

	protected void addJson( Map<String, Object> json ) throws ResolutionException
	{
		@SuppressWarnings("unchecked")
		List<String> filenames = (List<String>) json.get( "filenames" );
		if( filenames == null )
			return;
		for( String filename : filenames )
			addFilename( filename );
	}

	protected void addJsonTree(JsonNode jsonNode)
	{

		if (!jsonNode.has("filenames")) {
			return;
		}
		final JsonNode filenamesNode = jsonNode.get("filenames");
		if (!filenamesNode.isArray()) {
			throw new RuntimeException("is not array");
		}
		final ArrayNode arrayNode = (ArrayNode) filenamesNode;

		for (JsonNode node : arrayNode) {
			String filename = node.asText();
			addFilename(filename);
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final ConcurrentMap<String, Lexer> lexers = new ConcurrentHashMap<String, Lexer>();

	private static final ConcurrentMap<String, Lexer> lexerMap = new ConcurrentHashMap<String, Lexer>();

	private final List<Filter> filters = new ArrayList<Filter>();

	private boolean stripNewLines;

	private boolean stripAll;

	private int tabSize;

	private final List<String> aliases = new ArrayList<String>();

	private final List<String> filenames = new ArrayList<String>();

	private final List<String> mimeTypes = new ArrayList<String>();
}
