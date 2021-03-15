package blazingtwist.wswebservice;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class WebServiceFunction implements HttpHandler {
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
		HashMap<String, String> params = new HashMap<>();
		if (uriSplit.length == 2) {
			String[] paramSplit = uriSplit[1].split("&");
			Pattern paramPattern = Pattern.compile("^(.*?)=(.*)$");
			for (String urlParam : paramSplit) {
				Matcher matcher = paramPattern.matcher(urlParam);
				if (matcher.matches() && matcher.groupCount() == 2) {
					params.put(matcher.group(1), matcher.group(2));
				}
			}
		}

		System.out.println("Called " + contextName + " with params: ");
		for (Map.Entry<String, String> paramEntry : params.entrySet()) {
			System.out.println("\t" + paramEntry.getKey() + " = " + paramEntry.getValue());
		}

		this.handle(exchange, params);
	}

	public abstract void handle(HttpExchange exchange, Map<String, String> params);

	public void respond(HttpExchange exchange, int responseCode, String response){
		try{
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
