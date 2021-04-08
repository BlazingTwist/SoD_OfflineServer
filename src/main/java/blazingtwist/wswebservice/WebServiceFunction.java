package blazingtwist.wswebservice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class WebServiceFunction implements HttpHandler {
	public static final String CONTENT_LENGTH = "Content-Length";
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String WWW_FORM_URL_ENCODED = "application/x-www-form-urlencoded";

	public static final String INVALID_PARAMS = "Received invalid Parameters";
	public static final String INVALID_BODY = "Received invalid Body";
	public static final String INTERNAL_ERROR = "Internal Server Error";

	private final String contextName;

	public WebServiceFunction(String contextName) {
		this.contextName = contextName;
	}

	public void register(HttpServer server) {
		server.createContext("/" + contextName, this);
	}

	@Override
	public void handle(HttpExchange exchange) throws IOException {
		String requestURI = exchange.getRequestURI().toString();
		String[] uriSplit = requestURI.split("\\?");
		Map<String, String> params = null;
		if (uriSplit.length == 2) {
			params = WebFunctionUtils.readUrlMap(uriSplit[1]);

			System.out.println("Called " + contextName + " with params: ");
			for (Map.Entry<String, String> paramEntry : params.entrySet()) {
				System.out.println("\t" + paramEntry.getKey() + " = " + paramEntry.getValue());
			}
		}

		Map<String, String> body = null;
		String contentLengthString = exchange.getRequestHeaders().getFirst(CONTENT_LENGTH);
		int contentLength = contentLengthString != null ? Integer.parseInt(contentLengthString) : 0;
		if (contentLength > 0) {
			if (WWW_FORM_URL_ENCODED.equalsIgnoreCase(exchange.getRequestHeaders().getFirst(CONTENT_TYPE))) {
				byte[] dataBytes = exchange.getRequestBody().readAllBytes();
				String dataString = URLDecoder.decode(new String(dataBytes), StandardCharsets.UTF_8);
				body = WebFunctionUtils.readUrlMap(dataString);

				System.out.println("Called " + contextName + " with body: ");
				for (Map.Entry<String, String> bodyEntry : body.entrySet()) {
					System.out.println("\t" + bodyEntry.getKey() + " = " + bodyEntry.getValue());
				}
			}
		}

		this.handle(exchange, params, body);
	}

	public abstract void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body);

	public void respond(HttpExchange exchange, int responseCode, String response) {
		try {
			exchange.sendResponseHeaders(responseCode, response.length());
			OutputStream outputStream = exchange.getResponseBody();
			outputStream.write(response.getBytes());
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			System.err.println("unable to send response! context: " + contextName);
			e.printStackTrace();
		}
	}
}
