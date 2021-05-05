package blazingtwist.wswebservice.functions;

import blazingtwist.database.MainDBAccessor;
import blazingtwist.database.SSOParentTokenInfo;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.Gender;
import generated.UserInfo;
import java.sql.SQLException;
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

		/*
		 * Notes:
		 *   UserID - used a lot, we'll keep it the same as the userName, as those are required to be unique
		 *   ParentUserID - used for playerprefs, same as UserID for ParentUsers
		 *   BirthDate - has some special stuff attached to it (might be fun to add, currently unused)
		 *   Age - used for some eligibility checks, hard-code to 25? (fairly inconsequential) TODO
		 *   CreationDate - used for some special offers (more research) TODO
		 *
		 * relevant fields:
		 *   UserID | ParentUserID | Username | MultiplayerEnabled | BirthDate | GenderID | Age | OpenChatEnabled | CreationDate
		 * */

		SSOParentTokenInfo parentTokenInfo = null;
		try {
			parentTokenInfo = MainDBAccessor.getSSOParentTokenInfo(body.get(PARAM_API_TOKEN));
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}

		UserInfo result = new UserInfo();
		if (parentTokenInfo != null) {
			if (!parentTokenInfo.isExpired) {
				// For ParentUsers, ParentID and UserID are identical
				result.setUserID(parentTokenInfo.parentUserInfo.userID);
				result.setParentUserID(parentTokenInfo.parentUserInfo.userID);
				result.setUsername(parentTokenInfo.parentUserInfo.userName);
				result.setAge(25);

				result.setMultiplayerEnabled(true); // ParentUsers can't join games either way
				result.setOpenChatEnabled(true); // ParentUsers can't write in chat either way

				result.setBirthDate(null);
				result.setCreationDate(null);
				result.setGenderID(Gender.unknown); // TODO do we care about this?
			}
			// otherwise: Token Expired, leave result as null
		} else {
			// TODO create SSOTokens for ChildUsers
		}

		respondXml(exchange, 200, result, "UserInfo", false);
	}
}
