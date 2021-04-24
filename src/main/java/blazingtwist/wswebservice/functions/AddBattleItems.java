package blazingtwist.wswebservice.functions;

import blazingtwist.crypto.MD5;
import blazingtwist.crypto.TripleDes;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class AddBattleItems extends WebServiceFunction {
	public static final String PARAM_API_TOKEN = "apiToken";
	public static final String PARAM_API_KEY = "apiKey";
	public static final String PARAM_REQUEST = "request";
	public static final String PARAM_TICKS = "ticks";
	public static final String PARAM_SIGNATURE = "signature";

	@WebServiceFunctionConstructor
	public AddBattleItems(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if (!WebFunctionUtils.checkKeysPresent(body, PARAM_API_TOKEN, PARAM_API_KEY, PARAM_REQUEST,
				PARAM_TICKS, PARAM_SIGNATURE)) {
			respond(exchange, 500, INVALID_BODY);
			return;
		}

		/*
		 * notes:
		 *   request
		 *     itemID: TODO check ItemDump
		 */

		String calculatedSignature = MD5.getAsciiHashHex(
				body.get(PARAM_TICKS),
				TripleDes.KEY,
				body.get(PARAM_API_TOKEN),
				body.get(PARAM_REQUEST)
		);
		if(!calculatedSignature.equalsIgnoreCase(body.get(PARAM_SIGNATURE))){
			respond(exchange, 500, ERROR_INVALID_SIGNATURE);
			return;
		}

		// TODO parse input, build output
		respondXml(exchange, 200, true, "bool", false);
	}
}
