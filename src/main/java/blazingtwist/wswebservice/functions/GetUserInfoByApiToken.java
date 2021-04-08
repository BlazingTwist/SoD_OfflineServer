package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.Gender;
import generated.UserInfo;
import java.util.Map;

public class GetUserInfoByApiToken extends WebServiceFunction {
	public static final String PARAM_API_TOKEN = "apiToken";
	public static final String PARAM_API_KEY = "apiKey";

	@WebServiceFunctionConstructor
	public GetUserInfoByApiToken(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if (!WebFunctionUtils.checkKeysPresent(body, PARAM_API_TOKEN, PARAM_API_KEY)) {
			respond(exchange, 500, INVALID_BODY);
			return;
		}

		UserInfo result = new UserInfo();
		result.setUserID("TestUser1"); // TODO (used clientside A LOT, i.e. important)
		result.setParentUserID("ParentID"); // TODO mainly used for playerprefs, null when on Parent-Account
		result.setUsername("TestUserName"); // TODO
		result.setMultiplayerEnabled(true); // TODO
		result.setBirthDate(null); // TODO has some special stuff attached to it
		result.setGenderID(Gender.female); // TODO
		result.setAge(0); // TODO calculate based on birthdate (fairly inconsequential)
		result.setOpenChatEnabled(true); // TODO
		result.setCreationDate(null); // TODO used for some special offers (more research)

		respondXml(exchange, 200, result, "UserInfo", false);
	}
}
