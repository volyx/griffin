package com.threecrickets.jygments.grammar;

import com.threecrickets.jygments.ResolutionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LexerTest {

	@Test
	void getByName() throws ResolutionException {

		final Lexer javaLexer = Lexer.getByName("java");
	}
}