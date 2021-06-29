package blazingtwist.wswebservice.functions;

import blazingtwist.crypto.MD5;
import blazingtwist.crypto.TripleDes;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class RespondToChallenge extends WebServiceFunction {
	public static final String PARAM_API_TOKEN = "apiToken";
	public static final String PARAM_API_KEY = "apiKey";
	public static final String PARAM_CHALLENGE_ID = "challengeID";
	public static final String PARAM_MESSAGE_ID = "messageID";
	public static final String PARAM_POINTS = "points";
	public static final String PARAM_POINTS_TYPE = "pointsType";
	public static final String PARAM_TICKS = "ticks";
	public static final String PARAM_SIGNATURE = "signature";

	@WebServiceFunctionConstructor
	public RespondToChallenge(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if (!WebFunctionUtils.checkKeysPresent(body, PARAM_API_TOKEN, PARAM_API_KEY, PARAM_CHALLENGE_ID,
				PARAM_MESSAGE_ID, PARAM_POINTS, PARAM_POINTS_TYPE, PARAM_TICKS, PARAM_SIGNATURE)) {
			respond(exchange, 500, INVALID_BODY);
			return;
		}

		/*
		 * notes:
		 * TODO
		 */

		String calculatedSignature = MD5.getAsciiHashHex(
				body.get(PARAM_TICKS),
				TripleDes.KEY,
				body.get(PARAM_API_TOKEN),
				body.get(PARAM_CHALLENGE_ID),
				body.get(PARAM_MESSAGE_ID),
				body.get(PARAM_POINTS),
				body.get(PARAM_POINTS_TYPE)
		);
		logger.trace("calculated sig: {}", calculatedSignature);
		if (!calculatedSignature.equalsIgnoreCase(body.get(PARAM_SIGNATURE))) {
			respond(exchange, 500, ERROR_INVALID_SIGNATURE);
			return;
		}

		// TODO
		respondXml(exchange, 200, true, "bool", false);
	}
}
