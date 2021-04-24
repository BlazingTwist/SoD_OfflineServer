package blazingtwist.wswebservice.functions;

import blazingtwist.database.MainDBAccessor;
import blazingtwist.wswebservice.SSOTokenInfo;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.ApiTokenStatus;
import java.sql.SQLException;
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
		if (!WebFunctionUtils.checkKeysPresent(body, PARAM_API_TOKEN, PARAM_API_KEY)) {
			respond(exchange, 400, INVALID_BODY);
			return;
		}

		try {
			SSOTokenInfo tokenInfo = MainDBAccessor.getSSOParentTokenInfo(body.get(PARAM_API_TOKEN));
			ApiTokenStatus status;
			if (tokenInfo == null) {
				status = ApiTokenStatus.TokenNotFound;
			} else if (tokenInfo.isExpired) {
				status = tokenInfo.isExpiredByLogin
						? ApiTokenStatus.UserLoggedInFromAnotherLocation
						: ApiTokenStatus.TokenExpired;
			} else {
				status = ApiTokenStatus.TokenValid;
			}
			respondXml(exchange, 200, status, "ApiTokenStatus", false);
		} catch (SQLException throwables) {
			throwables.printStackTrace();
			respond(exchange, 500, INTERNAL_ERROR);
		}
	}
}
