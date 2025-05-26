package core.http;

import config.WebServerConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class HttpResponse {
		private String statusLine;
		private final Map<String, String> headers = new LinkedHashMap<>();
		private byte[] body;

		private final OutputStream out;

		public HttpResponse(OutputStream out) {
				this.out = out;
				this.statusLine = "HTTP/1.1 200 OK";
				headers.put("Connection", "close");
		}

		public void setStatus(int code, String reason) {
				this.statusLine = "HTTP/1.1 " + code + " " + reason;
		}

		public void setHeader(String key, String value) {
				headers.put(key, value);
		}

		public void setBody(String content, String contentType) {
				this.body = content.getBytes(StandardCharsets.UTF_8);
				headers.put("Content-Type", contentType);
				headers.put("Content-Length", String.valueOf(body.length));
		}

		public void setBody(byte[] content, String contentType) {
				this.body = content;
				headers.put("Content-Type", contentType);
				headers.put("Content-Length", String.valueOf(body.length));
		}

		public void send(OutputStream out) throws IOException {
				String CRLF = WebServerConfig.getCrlf();
				StringBuilder response = new StringBuilder();

				response.append(statusLine).append(CRLF);
				for (Map.Entry<String, String> header : headers.entrySet()) {
						response.append(header.getKey()).append(": ").append(header.getValue()).append(CRLF);
				}

				response.append(CRLF); // end of headers

				out.write(response.toString().getBytes(StandardCharsets.UTF_8));

				if (body != null) {
						out.write(body);
				}

				out.flush();
		}


		public void sendNotFound() throws IOException {
				setStatus(404, "Not Found");
				setBody("<h1>404 Not Found</h1>", "text/html");
				send(this.out);
		}

		public void sendBadRequest() throws IOException {
				setStatus(400, "Bad Request");
				setBody("<h1>400 Bad Request</h1>", "text/html");
				send(this.out);
		}

		public void sendNotImplemented() throws IOException {
				setStatus(501, "Not Implemented");
				setBody("<h1>501 Not Implemented</h1>", "text/html");
				send(this.out);
		}

		public void sendGet(String content, String contentType) throws IOException {
				setStatus(200, "OK");
				setBody(content, contentType);
				send(this.out);
		}

		public void serveFile(Path filePath) throws IOException {
				if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
						sendNotFound();
						return;
				}

				byte[] responseFileBytes = Files.readAllBytes(filePath);
				String responseFileContentType = Files.probeContentType(filePath);

				if (responseFileContentType == null) {
						responseFileContentType = "application/octet-stream";
				}

				if (filePath.getFileName().toString().endsWith(".html") || filePath.getFileName().toString().endsWith(".htm")) {
						responseFileContentType = "text/html";
				}

				setStatus(200, "OK");
				setBody(responseFileBytes, responseFileContentType);
				send(this.out);
		}
}
