package blazingtwist.wswebservice.functions;

import blazingtwist.crypto.MD5;
import blazingtwist.crypto.TripleDes;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class SetGameData extends WebServiceFunction {
	public static final String PARAM_API_TOKEN = "apiToken";
	public static final String PARAM_API_KEY = "apiKey";
	public static final String PARAM_USER_ID = "userId";
	public static final String PARAM_GAME_ID = "gameId";
	public static final String PARAM_IS_MULTIPLAYER = "isMultiplayer";
	public static final String PARAM_DIFFICULTY = "difficulty";
	public static final String PARAM_GAME_LEVEL = "gameLevel";
	public static final String PARAM_XML_DOCUMENT_DATA = "xmlDocumentData";
	public static final String PARAM_WIN = "win";
	public static final String PARAM_LOSS = "loss";
	public static final String PARAM_TICKS = "ticks";
	public static final String PARAM_SIGNATURE = "signature";

	@WebServiceFunctionConstructor
	public SetGameData(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if (!WebFunctionUtils.checkKeysPresent(body, PARAM_API_TOKEN, PARAM_API_KEY, PARAM_USER_ID, PARAM_GAME_ID,
				PARAM_IS_MULTIPLAYER, PARAM_DIFFICULTY, PARAM_GAME_LEVEL, PARAM_XML_DOCUMENT_DATA, PARAM_WIN,
				PARAM_LOSS, PARAM_TICKS, PARAM_SIGNATURE)) {
			respond(exchange, 500, INVALID_BODY);
			return;
		}

		/*
		 * Notes:
		 * TODO
		 */

		String calculatedSignature = MD5.getAsciiHashHex(
				body.get(PARAM_TICKS),
				TripleDes.KEY,
				body.get(PARAM_API_TOKEN),
				body.get(PARAM_USER_ID),
				body.get(PARAM_GAME_ID),
				body.get(PARAM_IS_MULTIPLAYER),
				body.get(PARAM_DIFFICULTY),
				body.get(PARAM_GAME_LEVEL),
				body.get(PARAM_XML_DOCUMENT_DATA),
				body.get(PARAM_WIN),
				body.get(PARAM_LOSS)
		);
		logger.trace("calculated sig: " + calculatedSignature);
		if (!calculatedSignature.equalsIgnoreCase(body.get(PARAM_SIGNATURE))) {
			respond(exchange, 500, ERROR_INVALID_SIGNATURE);
			return;
		}

		// TODO
		respondXml(exchange, 200, true, "bool", false);
	}
}
