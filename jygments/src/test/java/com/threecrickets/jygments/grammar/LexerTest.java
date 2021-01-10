package com.threecrickets.jygments.grammar;

import com.threecrickets.jygments.ResolutionException;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.stream.Collectors;

class LexerTest {

	@Test
	public void testJava() throws ResolutionException {

		final Lexer javaLexer = Lexer.getByName("java");
		String result;
		try (InputStream stream = ClassLoader.getSystemResourceAsStream("Solution.java")) {

			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(stream))) {
				result = bufferedReader
						.lines().collect(Collectors.joining("\n"));
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		for (Token token : javaLexer.getTokensUnprocessed(result)) {
			System.out.printf("token '%s' '%s'%n", token.getType(), token.getValue());
		}
	}
}