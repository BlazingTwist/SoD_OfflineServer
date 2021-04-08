package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.MembershipUserStatus;
import generated.ParentLoginData;
import generated.ParentLoginInfo;
import generated.UserLoginInfo;
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

			ParentLoginInfo result = new ParentLoginInfo();
			result.setUserName(loginData.getUserName());
			result.setApiToken("TempToken"); // TODO
			result.setUserID("TempID"); // TODO
			result.setStatus(MembershipUserStatus.SUCCESS); // TODO
			result.setSendActivationReminder(false); // TODO
			result.setUnAuthorized(false); // TODO

			UserLoginInfo firstChild = new UserLoginInfo();
			firstChild.setUserName("FirstChildName"); // TODO
			firstChild.setApiToken("TempToken1"); // TODO
			firstChild.setUserID("TempID1"); // TODO
			result.getChildList().add(firstChild);

			UserLoginInfo secondChild = new UserLoginInfo();
			secondChild.setUserName("SecondChildName"); // TODO
			secondChild.setApiToken("TempToken2"); // TODO
			secondChild.setUserID("TempID2"); // TODO
			result.getChildList().add(secondChild);

			String resultString = WebFunctionUtils.marshalXml(result, "ParentLoginInfo", UserLoginInfo.class);
			respond(exchange, 200, resultString);
		} catch (JAXBException e) {
			e.printStackTrace();
			respond(exchange, 500, INTERNAL_ERROR);
		}
	}
}
