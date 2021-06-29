package blazingtwist.wswebservice.functions;

import blazingtwist.crypto.MD5;
import blazingtwist.crypto.TripleDes;
import blazingtwist.database.querydatatypes.userinfo.ChildUserInfo;
import blazingtwist.database.MainDBAccessor;
import blazingtwist.database.querydatatypes.tokeninfo.SSOParentTokenInfo;
import blazingtwist.wswebservice.SSOTokenManager;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.sql.SQLException;
import java.util.Map;

public class LoginChild extends WebServiceFunction {
	public static final String PARAM_API_KEY = "apiKey";
	public static final String PARAM_PARENT_API_TOKEN = "parentApiToken";
	public static final String PARAM_TICKS = "ticks";
	public static final String PARAM_SIGNATURE = "signature";
	public static final String PARAM_CHILD_USER_ID = "childUserID";
	public static final String PARAM_LOCALE = "locale";

	@WebServiceFunctionConstructor
	public LoginChild(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if (!WebFunctionUtils.checkKeysPresent(body, PARAM_API_KEY, PARAM_PARENT_API_TOKEN, PARAM_TICKS, PARAM_SIGNATURE, PARAM_CHILD_USER_ID, PARAM_LOCALE)) {
			respond(exchange, 500, INVALID_BODY);
			return;
		}

		String calculatedSignature = MD5.getAsciiHashHex(
				body.get(PARAM_TICKS),
				TripleDes.KEY,
				body.get(PARAM_PARENT_API_TOKEN),
				body.get(PARAM_CHILD_USER_ID),
				body.get(PARAM_LOCALE)
		);
		if (!calculatedSignature.equalsIgnoreCase(body.get(PARAM_SIGNATURE))) {
			respond(exchange, 400, ERROR_INVALID_SIGNATURE);
			return;
		}

		String childUserId = TripleDes.decrypt(body.get(PARAM_CHILD_USER_ID));
		logger.trace("ChildUserID: {}", childUserId);

		try {
			SSOParentTokenInfo ssoParentTokenInfo = MainDBAccessor.getSSOParentTokenInfo(body.get(PARAM_PARENT_API_TOKEN));
			if (ssoParentTokenInfo == null || ssoParentTokenInfo.isExpired) {
				respond(exchange, 401, "Token is expired!");
				return;
			}

			ChildUserInfo childUserInfo = MainDBAccessor.getChildUserInfo(childUserId);
			if (childUserInfo == null) {
				respond(exchange, 401, "Invalid ChildUserID");
				return;
			}
			if (!childUserInfo.parentUserInfo.userName.equals(ssoParentTokenInfo.parentUserInfo.userName)) {
				logger.warn("parent: {} tried logging into child: {} of parent: {}",
						ssoParentTokenInfo.parentUserInfo.userName, childUserId, childUserInfo.parentUserInfo.userName);
				respond(exchange, 401, "Invalid ChildUserID");
				return;
			}

			String ssoToken = SSOTokenManager.generateChildToken(childUserInfo.userName);
			respondEncryptedString(exchange, 200, ssoToken);
		} catch (SQLException throwables) {
			logger.error("Unexpected SQL error in {}", this.getClass().getSimpleName(), throwables);
			respond(exchange, 500, INTERNAL_ERROR);
		}
	}
}
