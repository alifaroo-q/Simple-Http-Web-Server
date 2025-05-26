package config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WebServerConfig {
		private static final int PORT = 8080;

		private static final Path WEB_ROOT = Paths.get("public");

		private static final String CRLF = "\r\n";

		public static Path getWebRoot() {
				return WEB_ROOT;
		}

		public static String getCrlf() {
				return CRLF;
		}

		public static int getPort() {
				return PORT;
		}
}
