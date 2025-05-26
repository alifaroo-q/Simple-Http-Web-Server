import config.WebServerConfig;
import core.WebServer;

import java.io.IOException;

public class Main {
		public static void main(String[] args) {
				try {
						WebServer server = new WebServer(WebServerConfig.getPort());
						server.start();
				} catch (IOException e) {
						System.err.println("Error starting or running the server: " + e.getMessage());
				}
		}
}