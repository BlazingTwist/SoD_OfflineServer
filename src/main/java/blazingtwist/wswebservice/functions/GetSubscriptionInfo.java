package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebDataUtils;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.SubscriptionInfo;
import java.util.Map;

public class GetSubscriptionInfo extends WebServiceFunction {
	public static final String PARAM_API_TOKEN = "apiToken";
	public static final String PARAM_API_KEY = "apiKey";

	@WebServiceFunctionConstructor
	public GetSubscriptionInfo(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// TODO static response, remove serverside and fill in on clientside instead

		if (!WebFunctionUtils.checkKeysPresent(body, PARAM_API_TOKEN, PARAM_API_KEY)) {
			respond(exchange, 500, INVALID_BODY);
			return;
		}

		SubscriptionInfo result = WebDataUtils.getSubscriptionInfo();
		respondXml(exchange, 200, result, "SubscriptionInfo", false);
	}
}
