package com.pawandubey.griffin;

import java.nio.file.Path;

public class DirectoryStructure {
	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public String ROOT_DIRECTORY = System.getProperty("user.dir");
	public String CONFIG_FILE = ROOT_DIRECTORY + FILE_SEPARATOR + "config.toml";
	public String SOURCE_DIRECTORY = ROOT_DIRECTORY + FILE_SEPARATOR + "content";
	public String OUTPUT_DIRECTORY = ROOT_DIRECTORY + FILE_SEPARATOR + "output";
	public String INFO_FILE = ROOT_DIRECTORY + FILE_SEPARATOR + ".info";
	public String TAGS_DIRECTORY = OUTPUT_DIRECTORY + FILE_SEPARATOR + "tags";

	static String THEMES_FOLDER_NAME = "themes";
	public String THEMES_DIRECTORY;

	private static DirectoryStructure instance;

	public static DirectoryStructure create(Path path) {
		instance = new DirectoryStructure();
		instance.ROOT_DIRECTORY = (path == null) ? System.getProperty("user.dir"): path.toAbsolutePath().toString();
		instance.CONFIG_FILE = instance.ROOT_DIRECTORY + FILE_SEPARATOR + "config.toml";
		instance.SOURCE_DIRECTORY = instance.ROOT_DIRECTORY + FILE_SEPARATOR + "content";
		instance.OUTPUT_DIRECTORY = instance.ROOT_DIRECTORY + FILE_SEPARATOR + "output";
		instance.INFO_FILE = instance.ROOT_DIRECTORY + FILE_SEPARATOR + ".info";
		instance.TAGS_DIRECTORY = instance.OUTPUT_DIRECTORY + FILE_SEPARATOR + "tags";
		instance.THEMES_DIRECTORY = instance.ROOT_DIRECTORY + DirectoryStructure.FILE_SEPARATOR
				+ THEMES_FOLDER_NAME
				+ DirectoryStructure.FILE_SEPARATOR + Data.config.getTheme();
		return instance;
	}

	public static DirectoryStructure getInstance() {
		return instance;
	}
}
