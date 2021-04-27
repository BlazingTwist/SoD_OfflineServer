package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.GetStoreResponse;
import java.util.Map;

public class GetStore extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetStore(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// TODO
		GetStoreResponse response = new GetStoreResponse();
		respondXml(exchange, 200, response, "GetStoreResponse", false);
	}
}
