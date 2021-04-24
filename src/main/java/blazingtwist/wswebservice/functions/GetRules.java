package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.GetProductRulesResponse;
import java.util.Map;

public class GetRules extends WebServiceFunction {
	public static final String PARAM_API_KEY = "apiKey";

	@WebServiceFunctionConstructor
	public GetRules(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if(!WebFunctionUtils.checkKeysPresent(body, PARAM_API_KEY)){
			respond(exchange, 500, INVALID_BODY);
			return;
		}

		GetProductRulesResponse response = new GetProductRulesResponse();
		response.setGlobalSecretKey("11A0CC5A-C4DF-4A0E-931C-09A44C9966AE");
		respondXml(exchange, 200, response, "getProductRulesResponse", true);
		// TODO
	}
}
