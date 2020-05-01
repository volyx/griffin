package com.pawandubey.griffin.pipeline;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

public class DirectoryCleaner implements Consumer<Path> {
	@Override
	public void accept(Path pathToClean) {
		System.out.println("Cleaning up the output area...");

		try {
			Files.walkFileTree(pathToClean, new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					Files.delete(file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					return FileVisitResult.TERMINATE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					Files.delete(dir);
					return FileVisitResult.CONTINUE;
				}

			});
			Files.createDirectory(pathToClean);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		System.out.println("Cleanup done.");
	}
}
