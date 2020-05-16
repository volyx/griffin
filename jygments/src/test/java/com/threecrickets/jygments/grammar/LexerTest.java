package com.threecrickets.jygments.grammar;

import com.threecrickets.jygments.Jygments;
import com.threecrickets.jygments.ResolutionException;
import org.junit.jupiter.api.Test;

class LexerTest {

	@Test
	public void getByName() throws ResolutionException {

		final Lexer javaLexer = Lexer.getByName("java");
	}
}