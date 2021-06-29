package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GetKeyValuePair extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetKeyValuePair(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// TODO to be honest, I have no clue just how many things are tied to this, so we'll keep it null for now

		InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream("TestPairData.xml");
		if (xmlStream == null) {
			logger.error("Failed to load TestPairData.xml!");
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		try {
			respond(exchange, 200, new String(xmlStream.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.error("Unexpected error in {}", this.getClass().getSimpleName(), e);
			respond(exchange, 500, INTERNAL_ERROR);
		}
	}
}
