package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class GetKeyValuePairByUserID extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetKeyValuePairByUserID(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// TODO to be honest, I have no clue just how many things are tied to this, so we'll keep it null for now
		respond(exchange, 200, "");
	}
}
