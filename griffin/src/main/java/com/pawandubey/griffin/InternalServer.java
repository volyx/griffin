/*
 * Copyright 2015 Pawan Dubey pawandubey@outlook.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pawandubey.griffin;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web Server for serving the static site for live preview,
 * on default port 9090.
 *
 * @author Pawan Dubey pawandubey@outlook.com
 */
public class InternalServer {
	private Integer port = 9090;

	protected InternalServer(Integer p) {
		port = p;
	}

	/**
	 * Creates and starts the server to serve the contents of OUTPUT_DIRECTORY on port
	 * 9090.
	 */
	protected void startPreview() {

		InetSocketAddress cd = new InetSocketAddress(port);
		HttpServer server;
		try {
			server = HttpServer.create(cd, 1000);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		final Path root = Paths.get(DirectoryStructure.getInstance().OUTPUT_DIRECTORY);

		server.createContext("/", httpExchange -> {
			URI uri = httpExchange.getRequestURI();

			String pathname = root + uri.getPath();
			if (pathname.endsWith("/")) {
				pathname = pathname + "index.html";
			}
			File file = new File(pathname).getCanonicalFile();

			if (file.isDirectory()) {
				pathname = pathname + "/index.html";
				file = new File(pathname).getCanonicalFile();
			}

			if (!file.toPath().startsWith(root)) {
				// Suspected path traversal attack: reject with 403 error.
				String response = "403 (Forbidden)\n";
				httpExchange.sendResponseHeaders(403, response.length());
				OutputStream os = httpExchange.getResponseBody();
				os.write(response.getBytes());
				os.close();
			} else if (!file.isFile()) {
				// Object does not exist or is not a file: reject with 404 error.
				String response = "404 (Not Found)\n";
				httpExchange.sendResponseHeaders(404, response.length());
				OutputStream os = httpExchange.getResponseBody();
				os.write(response.getBytes());
				os.close();
			} else {
				// Object exists and is a file: accept with response code 200.
				httpExchange.sendResponseHeaders(200, file.length());
				System.out.println(uri);
				try (OutputStream os = httpExchange.getResponseBody();
					 InputStream fs = Files.newInputStream(file.toPath())) {
					// replace with InputStream.transferTo after in Java 9+
					byte[] buffer = new byte[8192];
					int read;
					while ((read = fs.read(buffer, 0, 8192)) >= 0) {
						os.write(buffer, 0, read);
					}
				}
			}
		});
		server.setExecutor(null); // creates a default executor
		server.start();
	}

	/**
	 * Opens the system's default browser and tries to navigate to the URL at
	 * which the server is operational.
	 */

	protected void openBrowser() {
		String url = "http://localhost:" + port;
		String os = System.getProperty("os.name").toLowerCase();

		try {
			if (os.indexOf("win") >= 0) {
				Runtime rt = Runtime.getRuntime();
//			rt.exec("rundll32 url.dll,FileProtocolHandler " + url);
				rt.exec("start \"" + url + "\"");
			} else if (os.indexOf("mac") >= 0) {
				Runtime rt = Runtime.getRuntime();
				rt.exec("open " + url);
			} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0) {
				Runtime rt = Runtime.getRuntime();
				String[] browsers = {"chrome", "epiphany", "firefox", "mozilla", "konqueror",
						"netscape", "opera", "links", "lynx"};

				StringBuffer cmd = new StringBuffer();
				for (int i = 0; i < browsers.length; i++)
					if (i == 0)
						cmd.append(String.format("%s \"%s\"", browsers[i], url));
					else
						cmd.append(String.format(" || %s \"%s\"", browsers[i], url));
				// If the first didn't work, try the next browser and so on

				rt.exec(new String[]{"sh", "-c", cmd.toString()});
			}
		} catch (IOException e) {
			System.err.println("error open browser");
		}

	}
}
