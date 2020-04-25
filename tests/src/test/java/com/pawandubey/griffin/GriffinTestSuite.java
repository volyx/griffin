package com.pawandubey.griffin;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;

public class GriffinTestSuite {

	private static final Random RANDOM = new Random();

	@TempDir
	File tempDir;

	@ParameterizedTest(name = "{0} -V")
	@MethodSource("griffinProvider")
	void testVersion(GriffinRun griffinRun) throws IOException, InterruptedException {

		final File tempOut = createTempFile();
		final File tempIn = createTempFile();
		final File tempErr = createTempFile();

		List<String> command = concat(griffinRun.command, "-V");

		final Process griffinJar = new ProcessBuilder()
				.directory(griffinRun.workDir)
				.command(command)
				.redirectOutput(tempOut)
				.redirectInput(tempIn)
				.redirectError(tempErr)
				.start();

		griffinJar.waitFor(2, TimeUnit.SECONDS);

		System.out.println("in\n" + new String(Files.readAllBytes(tempOut.toPath())));
		System.out.println("out\n" + new String(Files.readAllBytes(tempIn.toPath())));
		System.out.println("err\n" + new String(Files.readAllBytes(tempErr.toPath())));


		final String output = new String(Files.readAllBytes(tempOut.toPath()));
		System.out.println(output);

		assertThat(output, Matchers.notNullValue());
		assertThat(output.trim(), Matchers.equalTo(Griffin.VERSION));
	}


	@ParameterizedTest(name = "{0} init")
	@MethodSource("griffinProvider")
	void testInit(GriffinRun griffinRun) throws IOException, InterruptedException {

		final File tempOut = createTempFile();
		final File tempIn = createTempFile();
		final File tempErr = createTempFile();

		List<String> command = concat(griffinRun.command,
				"new",
				"-n",
				"griffin-" + RANDOM.nextInt(),
				tempDir.toPath().toAbsolutePath().toString()
				);

		final Process griffinJar = new ProcessBuilder()
				.directory(griffinRun.workDir)
				.command(command)
				.redirectOutput(tempOut)
				.redirectInput(tempIn)
				.redirectError(tempErr)
				.start();

		OutputStream os = griffinJar.getOutputStream();
		try (PrintWriter writer=new PrintWriter(os)) {
			writer.write("supername\n");
		}


		griffinJar.waitFor(2, TimeUnit.SECONDS);

		System.out.println("in\n" + new String(Files.readAllBytes(tempOut.toPath())));
		System.out.println("out\n" + new String(Files.readAllBytes(tempIn.toPath())));
		System.out.println("err\n" + new String(Files.readAllBytes(tempErr.toPath())));


		System.out.println(FileAssert.printDirectoryTree(tempDir));;
	}

	private File createTempFile() throws IOException {
		final Path temp = tempDir.toPath().resolve(RANDOM.nextInt() + "input.txt");
		Assertions.assertTrue(temp.toFile().createNewFile());
		return temp.toFile();
	}

	private static List<String> concat(List<String> first, String ... args) {
		List<String> commands = new ArrayList<>(first);
		if (args != null && args.length != 0) {
			commands.addAll(asList(args));
		}

		return commands;
	}

	static List<GriffinRun> griffinProvider() {

		final Path parentModuleDirectory = Paths.get(System.getProperty("user.dir")).getParent();

		return asList(
				new GriffinRun(
						parentModuleDirectory.resolve("griffin").resolve("build").resolve("libs").toFile(),
						asList("java",
								"-jar",
								"griffin-all.jar")
				),
				new GriffinRun(
						parentModuleDirectory.resolve("griffin").resolve("build").resolve("native-image").toFile(),
						Collections.singletonList("./griffin")
				)
		);
	}

	public static class GriffinRun {
		final File workDir;
		final List<String> command;

		public GriffinRun(File workDir, List<String> command) {
			this.workDir = workDir;
			this.command = command;
		}

		@Override
		public String toString() {
			return workDir.toPath().toString() + command;
		}
	}
}
