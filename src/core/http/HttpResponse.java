package core.http;

import config.WebServerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

public class HttpResponse {

		private static final Logger logger = Logger.getLogger(HttpResponse.class.getName());

		private byte[] body;
		private String statusLine;
		private final Map<String, String> headers = new LinkedHashMap<>();


		private final OutputStream out;

		private final Map<String, String> contentTypes = Map.ofEntries(
						Map.entry("html", "text/html"),
						Map.entry("js", "application/javascript"),
						Map.entry("css", "text/css"),
						Map.entry("default", "application/octet-stream")
		);

		public HttpResponse(OutputStream out) {
				this.out = out;
				this.statusLine = "HTTP/1.1 200 OK";
				this.headers.put("Connection", "close");
		}

		public void setStatus(int code, String reason) {
				this.statusLine = "HTTP/1.1 " + code + " " + reason;
		}

		public void setHeader(String key, String value) {
				this.headers.put(key, value);
		}

		public void setBody(String content, String contentType) {
				this.body = content.getBytes(StandardCharsets.UTF_8);
				this.headers.put("Content-Type", contentType);
				this.headers.put("Content-Length", String.valueOf(body.length));
		}

		public void setBody(byte[] content, String contentType) {
				this.body = content;
				this.headers.put("Content-Type", contentType);
				this.headers.put("Content-Length", String.valueOf(body.length));
		}

		public void setContentType(String contentType) {
				this.headers.put("Content-Type", contentType);
		}

		public void send() throws IOException {
				this.sendHeaders();

				if (this.body != null) {
						this.out.write(this.body);
				}

				this.out.flush();
		}


		public void sendNotFound() throws IOException {
				setStatus(404, "Not Found");
				setBody("<h1>404 Not Found</h1>", "text/html");
				send();
		}

		public void sendBadRequest() throws IOException {
				setStatus(400, "Bad Request");
				setBody("<h1>400 Bad Request</h1>", "text/html");
				send();
		}

		public void sendNotImplemented() throws IOException {
				setStatus(501, "Not Implemented");
				setBody("<h1>501 Not Implemented</h1>", "text/html");
				send();
		}

		public void sendGet(String content, String contentType) throws IOException {
				setStatus(200, "OK");
				setBody(content, contentType);
				send();
		}

		private void streamFileContent(InputStream inputStream) throws IOException {
				byte[] buffer = new byte[8192];  // 8KB buffer
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
						this.out.write(buffer, 0, bytesRead);
				}
		}

		private void sendHeaders() throws IOException {
				String CRLF = WebServerConfig.getCrlf();
				StringBuilder response = new StringBuilder();

				response.append(statusLine).append(CRLF);
				for (Map.Entry<String, String> header : headers.entrySet()) {
						response.append(header.getKey()).append(": ").append(header.getValue()).append(CRLF);
				}
				response.append(CRLF); // end of headers

				this.out.write(response.toString().getBytes(StandardCharsets.UTF_8));
		}


		public void serveFile(Path filePath) throws IOException {
				if (!isValidFile(filePath)) {
						sendNotFound();
						return;
				}

				// Set status and headers first
				setStatus(200, "OK");
				setHeader("Content-Type", determineContentType(filePath));

				// Add Content-Length header if possible
				try {
						long fileSize = Files.size(filePath);
						setHeader("Content-Length", String.valueOf(fileSize));
				} catch (IOException e) {
						System.out.println("Can't get file size: " + e.getMessage());
						setHeader("Transfer-Encoding", "chunked");
				}

				this.sendHeaders();

				logger.info("Serving file: " + filePath);

				try (InputStream fileStream = Files.newInputStream(filePath)) {
						this.streamFileContent(fileStream);
				}

				this.out.flush();
		}


		private boolean isValidFile(Path filePath) {
				return Files.exists(filePath) && !Files.isDirectory(filePath);
		}

		private String determineContentType(Path filePath) {
				String contentType = null;

				try {
						contentType = Files.probeContentType(filePath);
				} catch (IOException e) {
						logger.info("Can't determine content type: " + e.getMessage());
				}

				if (contentType == null || contentType.equals("application/octet-stream")) {
						String fileName = filePath.getFileName().toString();
						if (fileName.endsWith(".html") || fileName.endsWith(".htm")) {
								contentType = contentTypes.get("html");
						} else if (fileName.endsWith(".css")) {
								contentType = contentTypes.get("css");
						} else if (fileName.endsWith(".js")) {
								contentType = contentTypes.get("js");
						}
				}

				if (contentType == null) {
						contentType = contentTypes.get("default");
				}

				return contentType;
		}

}
