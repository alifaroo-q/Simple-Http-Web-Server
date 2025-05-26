package core.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class HttpRequest {
		private final String method;
		private final String path;
		private final Map<String, String> headers;
		private final String protocol;

		public HttpRequest(String method, String path, String protocol, Map<String, String> headers) {
				this.method = method;
				this.path = path;
				this.protocol = protocol;
				this.headers = headers != null ? headers : new HashMap<>();
		}

		public String getMethod() {
				return method;
		}

		public String getPath() {
				return path;
		}

		public Map<String, String> getHeaders() {
				return headers;
		}

		public String getProtocol() {
				return protocol;
		}

		public String getHeader(String name) {
				return headers.getOrDefault(name, "");
		}

		public static HttpRequest parse(BufferedReader in) throws IOException {
				String requestLine = in.readLine();
				if (requestLine == null || requestLine.isEmpty()) return null;

				String[] parts = requestLine.split(" ");
				if (parts.length < 3) return null;

				String method = parts[0];
				String path = parts[1];
				String protocol = parts[2];

				String line;
				Map<String, String> headers = new HashMap<>();

				while ((line = in.readLine()) != null && !line.isEmpty()) {
						String[] headerParts = line.split(": ", 2);
						if (headerParts.length == 2) {
								headers.put(headerParts[0], headerParts[1]);
						}
				}

				return new HttpRequest(method, path, protocol, headers);
		}

}
