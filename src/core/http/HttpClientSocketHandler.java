package core.http;

import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HttpClientSocketHandler implements Runnable {

		private final Socket clientSocket;
		private static final Path WEB_ROOT = Paths.get("webroot");

		public HttpClientSocketHandler(Socket socket) {
				this.clientSocket = socket;
		}

		@Override
		public void run() {
				try (
								BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
								OutputStream out = clientSocket.getOutputStream();
				) {

						HttpRequest request = HttpRequest.parse(in);
						HttpResponse response = new HttpResponse(out);

						if (request == null) {
								response.sendBadRequest();
								return;
						}

						if (!"GET".equalsIgnoreCase(request.getMethod())) {
								response.sendNotImplemented();
								return;
						}

						switch (request.getPath()) {
								case "/":
										response.sendGet("Hello, World!", "text/plain");
										return;
								case "/index.html":
										response.serveFile(WEB_ROOT.resolve("index.html"));
										return;
								default:
										response.sendNotFound();
										return;
						}

				} catch (IOException e) {
						System.out.println("Error handling client socket: " + e.getMessage());
				} finally {
						try {
								clientSocket.close();
						} catch (IOException e) {
								System.err.println("Error closing client socket: " + e.getMessage());
						}
				}
		}
}
