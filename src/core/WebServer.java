package core;

import config.WebServerConfig;
import core.http.HttpClientSocketHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;

public class WebServer {

		private final int port;
		private final ServerSocket server;

		public WebServer(int port) throws IOException {
				this.port = port;
				this.server = new ServerSocket(port);
		}

		public WebServer() throws IOException {
				this.port = WebServerConfig.getPort();
				this.server = new ServerSocket(WebServerConfig.getPort());
		}

		public void start() throws IOException {

				System.out.println("Server started on port " + this.port);

				if (!Files.exists(WebServerConfig.getWebRoot()) || !Files.isDirectory(WebServerConfig.getWebRoot())) {
						System.err.println("Error: 'webroot' directory not found or is not a directory at " + WebServerConfig.getWebRoot().toAbsolutePath());
						System.err.println("Please create a 'webroot' directory and place your 'index.html' inside it.");
						return;
				}

				while (true) {
						Socket clientSocket = this.server.accept();

						System.out.println("Accepted connection from " + clientSocket.getInetAddress().getHostAddress());

						new Thread(new HttpClientSocketHandler(clientSocket)).start();
				}

		}
}
