package blazingtwist.wswebservice.functions;

import blazingtwist.database.MainDBAccessor;
import blazingtwist.database.querydatatypes.userinfo.ParentUserInfo;
import blazingtwist.wswebservice.SSOTokenManager;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.MembershipUserStatus;
import generated.ParentLoginData;
import generated.ParentLoginInfo;
import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBException;

public class LoginParent extends WebServiceFunction {
	public static final String PARAM_API_KEY = "apiKey";
	public static final String PARAM_PARENT_LOGIN_DATA = "parentLoginData";

	@WebServiceFunctionConstructor
	public LoginParent(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if (!WebFunctionUtils.checkKeysPresent(body, PARAM_API_KEY, PARAM_PARENT_LOGIN_DATA)) {
			respond(exchange, 500, INVALID_BODY);
			return;
		}

		try {
			ParentLoginData loginData = WebFunctionUtils.unmarshalEncryptedXml(body.get(PARAM_PARENT_LOGIN_DATA), ParentLoginData.class);

			/*
			 * Request-Notes:
			 *   ChildUserID is always ""
			 *   FaceBookUserId is always null
			 *   FacebookAccessToken is always ""
			 *   LoginDuration is never set
			 *   ExternalAuthProvider is always null
			 *   ExternalUserID is always null
			 *   ExternalAuthData is always null
			 *   ClientIP is always null
			 *   LoginHash is always null
			 *
			 *   UserPolicy is either null, or one where PrivacyPolicy and TermsAndConditions are accepted, i.e. it's useless
			 *   Locale is hard-coded to en-US (wow guys, gg)
			 *
			 *   Conclusion: the only relevant data sent is the UserName and Password
			 * */

			ParentUserInfo parentUserInfo = MainDBAccessor.getParentUserInfo(loginData.getUserName());
			if (parentUserInfo == null) {
				ParentLoginInfo invalidUserNameResult = new ParentLoginInfo();
				invalidUserNameResult.setUserName(loginData.getUserName());
				invalidUserNameResult.setStatus(MembershipUserStatus.INVALID_USER_NAME);
				respondXml(exchange, 200, invalidUserNameResult, "ParentLoginInfo", true);
				return;
			}

			if (!parentUserInfo.password.equals(loginData.getPassword())) {
				ParentLoginInfo invalidPasswordResult = new ParentLoginInfo();
				invalidPasswordResult.setUserName(loginData.getUserName());
				invalidPasswordResult.setStatus(MembershipUserStatus.INVALID_PASSWORD);
				respondXml(exchange, 200, invalidPasswordResult, "ParentLoginInfo", true);
				return;
			}

			/*
			 * Response-Notes:
			 *   UserID is only used for IAP (to be removed) and PlayFab (TODO research)
			 *   ChildList is only used for a single GuestLogin-check (TODO remove)
			 *   Unauthorized and SendActivationReminder can be left null
			 *
			 *   relevant:
			 *     UserName (nullable)
			 *     ApiToken (nullable)
			 *     Status
			 * */

			String ssoToken = SSOTokenManager.generateParentToken(parentUserInfo.userName);

			ParentLoginInfo result = new ParentLoginInfo();
			result.setUserName(parentUserInfo.userName);
			result.setApiToken(ssoToken);
			result.setUserID(parentUserInfo.userID);
			result.setStatus(MembershipUserStatus.SUCCESS);

			respondXml(exchange, 200, result, "ParentLoginInfo", true);
		} catch (JAXBException | SQLException e) {
			logger.error("Unexpected error in {}", this.getClass().getSimpleName(), e);
			respond(exchange, 500, INTERNAL_ERROR);
		}
	}
}
