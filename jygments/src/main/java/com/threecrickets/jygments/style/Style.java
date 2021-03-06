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

package com.threecrickets.jygments.style;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.threecrickets.jygments.Jygments;
import com.threecrickets.jygments.NestedDef;
import com.threecrickets.jygments.ResolutionException;
import com.threecrickets.jygments.grammar.TokenType;
import com.threecrickets.jygments.style.def.StyleElementDef;

/**
 * @author Tal Liron
 */
public class Style extends NestedDef<Style>
{
	//
	// Static operations
	//

	public static Style getByName( String name )
	{
		if( Character.isLowerCase( name.charAt( 0 ) ) )
			name = Character.toUpperCase( name.charAt( 0 ) ) + name.substring( 1 ) + "Style";

		Style style = getByFullName( name );
		if( style != null )
			return style;
		else
		{
			// Try contrib package
			String pack = Jygments.class.getPackage().getName() + ".contrib";
			name = pack + "." + name;
			style = getByFullName( name );
			if( style == null )
				throw new RuntimeException( "Could not load style: " + name );
			return style;
		}
	}

	@SuppressWarnings("unchecked")
	public static Style getByFullName( String fullName )
	{
		// Try cache
		Style style = styles.get( fullName );
		if( style != null )
			return style;

		try
		{
			return (Style) Jygments.class.getClassLoader().loadClass( fullName ).newInstance();
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
			ObjectMapper objectMapper = new ObjectMapper();
			objectMapper.getFactory().configure( JsonParser.Feature.ALLOW_COMMENTS, true );
			try
			{
				Map<String, Object> json = objectMapper.readValue( stream, HashMap.class );
				style = new Style();
				style.addJson( json );
				style.resolve();

				// Cache it
				Style existing = styles.putIfAbsent( fullName, style );
				if( existing != null )
					style = existing;

				return style;
			}
			catch( JsonParseException x )
			{
				throw new RuntimeException( x );
			}
			catch( JsonMappingException x )
			{
				throw new RuntimeException( x );
			}
			catch( IOException x )
			{
				throw new RuntimeException( x );
			} catch (ResolutionException e) {
				throw new RuntimeException( e );
			}
		}

		return null;
	}

	//
	// Attributes
	//

	public Map<TokenType, List<StyleElement>> getStyleElements()
	{
		return styleElements;
	}


	public String getName() {
		return null;
	}

	public String backgroundColor() {
		return null;
	}

	public java.lang.String lineHeight() {
		return null;
	}

	//
	// Operations
	//

	public void addStyleElement( TokenType tokenType, StyleElement styleElement )
	{
		List<StyleElement> styleElementsForTokenType = styleElements.get( tokenType );
		if( styleElementsForTokenType == null )
		{
			styleElementsForTokenType = new ArrayList<StyleElement>();
			styleElements.put( tokenType, styleElementsForTokenType );
		}
		styleElementsForTokenType.add( styleElement );
	}

	public void resolve() throws ResolutionException
	{
		resolve( this );
	}

	//
	// Def
	//

	@Override
	public boolean resolve( Style style ) throws ResolutionException
	{
		if( super.resolve( style ) )
		{
			boolean done = false;
			while( !done )
			{
				done = true;
				for( TokenType tokenType : TokenType.getTokenTypes() )
				{
					if( tokenType != TokenType.Token )
					{
						if( !styleElements.containsKey( tokenType ) )
						{
							boolean doneOne = false;
							TokenType parent = tokenType.getParent();
							while( parent != null )
							{
								if( parent == TokenType.Token )
								{
									doneOne = true;
									break;
								}

								List<StyleElement> parentElements = styleElements.get( parent );
								if( parentElements != null )
								{
									styleElements.put( tokenType, parentElements );
									doneOne = true;
									break;
								}

								parent = parent.getParent();
							}

							if( !doneOne )
								done = false;
						}
					}
				}
			}

			return true;
		}
		else
			return false;
	}

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected void add( String tokenTypeName, String... styleElementNames )
	{
		ArrayList<String> list = new ArrayList<String>( styleElementNames.length );
		for( String styleElementName : styleElementNames )
			list.add( styleElementName );
		addDef( new StyleElementDef( tokenTypeName, list ) );
	}

	@SuppressWarnings("unchecked")
	protected void addJson( Map<String, Object> json ) throws ResolutionException
	{
		for( Map.Entry<String, Object> entry : json.entrySet() )
		{
			String tokenTypeName = entry.getKey();
			if( entry.getValue() instanceof Iterable<?> )
			{
				for( String styleElementName : (Iterable<String>) entry.getValue() )
					add( tokenTypeName, styleElementName );
			}
			else if( entry.getValue() instanceof String )
				add( tokenTypeName, (String) entry.getValue() );
			else
				throw new ResolutionException( "Unexpected value in style definition: " + entry.getValue() );
		}
	}

	// //////////////////////////////////////////////////////////////////////////
	// Private

	private static final ConcurrentMap<String, Style> styles = new ConcurrentHashMap<String, Style>();

	private final Map<TokenType, List<StyleElement>> styleElements = new HashMap<TokenType, List<StyleElement>>();
}
