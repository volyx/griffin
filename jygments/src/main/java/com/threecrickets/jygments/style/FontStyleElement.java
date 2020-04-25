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

/**
 * @author Tal Liron
 */
public class FontStyleElement extends StyleElement
{
	//
	// Constants
	//

	public static final FontStyleElement Roman = create( "roman" );

	public static final FontStyleElement Sans = create( "sans" );

	public static final FontStyleElement Mono = create( "mono" );

	// //////////////////////////////////////////////////////////////////////////
	// Protected

	protected FontStyleElement( String name )
	{
		super( name );
	}

	private static FontStyleElement create( String name )
	{
		FontStyleElement fontStyleElement = new FontStyleElement( name );
		add( fontStyleElement );
		return fontStyleElement;
	}
}
