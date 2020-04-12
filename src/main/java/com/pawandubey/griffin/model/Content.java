package com.pawandubey.griffin.model;

import com.moandjiezana.toml.Toml;
import com.pawandubey.griffin.DirectoryCrawler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.pawandubey.griffin.Configurator.LINE_SEPARATOR;
import static com.pawandubey.griffin.Data.config;

public class Content {
	public static final String HEADER_DELIMITER = "#####";
	private static final StringBuilder header = new StringBuilder();
	private static final Toml toml = new Toml();
	public static String author = config.getSiteAuthor();

	/**
	 * Creates an appropriate instance of a Parsable implementation depending
	 * upon the header of the file.
	 *
	 * @param file the path of the file from which to create a Parsable.
	 * @return the created Parsable.
	 */
	@Nullable
	public static Parsable createParsable(Path file) {
		final String relativePath = file.toString();
		try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			return parse(relativePath, br);
		} catch (IOException ex) {
			Logger.getLogger(DirectoryCrawler.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	static Parsable parse(String relativePath, BufferedReader br) throws IOException {
		StringBuilder sb = new StringBuilder();
		header.setLength(0);
		String line;
		while ((line = br.readLine()) != null && !line.equals(HEADER_DELIMITER)) {
			header.append(line).append(LINE_SEPARATOR);
		}

		toml.parse(header.toString());
		String title = toml.getString("title");
		author = toml.getString("author") != null ? toml.getString("author") : author;
		String date = toml.getString("date");
		String slug = toml.getString("slug");
		LocalDate publishDate = LocalDate.parse(date, DateTimeFormatter.ofPattern(config.getInputDateFormat()));
		publishDate = LocalDate.parse(publishDate.format(DateTimeFormatter.ofPattern(config.getOutputDateFormat())), DateTimeFormatter.ofPattern(config.getOutputDateFormat()));
		String layout = toml.getString("layout");
		List<String> tag = toml.getList("tags");
		String img = toml.getString("image");
		StringBuilder content = new StringBuilder();
		String[] halves;
		while ((line = br.readLine()) != null) {
			content.append(line).append(LINE_SEPARATOR);
		}

		sb.append(header).append(HEADER_DELIMITER).append(content);
		if (layout.equals("post")) {
			return new Post(title, author, publishDate, relativePath, content.toString(), sb.toString(), img, slug, layout, tag);
		} else {
			return new Page(title, author, relativePath, content.toString(), sb.toString(), img, slug, layout, tag);
		}
	}
	@Nullable
	public static Parsable parse(String filename, String content) {
		try (BufferedReader br = new BufferedReader(new StringReader(content))) {
			return parse(filename, br);
		} catch (IOException e) {
			Logger.getLogger(DirectoryCrawler.class.getName()).log(Level.SEVERE, null, e);
		}
		return null;
	}
}
