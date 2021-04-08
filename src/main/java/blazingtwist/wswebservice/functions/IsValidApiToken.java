package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.ApiTokenStatus;
import java.util.Map;

public class IsValidApiToken extends WebServiceFunction {
	public static final String PARAM_API_TOKEN = "apiToken";
	public static final String PARAM_API_KEY = "apiKey";

	@WebServiceFunctionConstructor
	public IsValidApiToken(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if(!WebFunctionUtils.checkKeysPresent(body, PARAM_API_TOKEN, PARAM_API_KEY)){
			respond(exchange, 500, INVALID_BODY);
			return;
		}

		ApiTokenStatus result = ApiTokenStatus.TokenValid; // TODO
		respondXml(exchange, 200, result, "ApiTokenStatus", false);
	}
}
