package com.pawandubey.griffin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileAssert {

	/**
	 * Pretty print the directory tree and its file names.
	 *
	 * @param folder must be a folder.
	 * @return
	 */
	public static String printDirectoryTree(File folder) {
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("folder is not a Directory");
		}
		int indent = 0;
		StringBuilder sb = new StringBuilder();
		printDirectoryTree(folder, indent, sb);
		return sb.toString();
	}

	private static void printDirectoryTree(File folder, int indent,
										   StringBuilder sb) {
		if (!folder.isDirectory()) {
			throw new IllegalArgumentException("folder is not a Directory");
		}
		sb.append(getIndentString(indent));
		sb.append("+--");
		sb.append(folder.getName());
		sb.append("/");
		sb.append("\n");
		for (File file : folder.listFiles()) {
			if (file.isDirectory()) {
				printDirectoryTree(file, indent + 1, sb);
			} else {
				printFile(file, indent + 1, sb);
			}
		}

	}

	private static void printFile(File file, int indent, StringBuilder sb) {
		sb.append(getIndentString(indent));
		sb.append("+--");
		sb.append(file.getName());
		sb.append("\n");
	}

	private static String getIndentString(int indent) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			sb.append("|  ");
		}
		return sb.toString();
	}

	public static void main(String[] args) throws FileNotFoundException {
		RandomAccessFile aFile = new RandomAccessFile("/Users/volyx/Projects/griffin/tests/build.gradle", "rw");

		try (FileChannel fileChannel = aFile.getChannel();) {
			ByteBuffer str = ByteBuffer.allocate(10);
			while (fileChannel.read(str) > 0) {
				str.flip();
				while (str.hasRemaining()) {
					System.out.println("str = " + str.get());
				}
				str.clear();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}