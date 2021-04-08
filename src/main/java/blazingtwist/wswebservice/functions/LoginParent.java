package blazingtwist.wswebservice.functions;

import blazingtwist.sod.ParentLoginData;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
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
			respond(exchange, 200, "OK");
			System.out.println("done");
		} catch (JAXBException e) {
			e.printStackTrace();
			respond(exchange, 500, INTERNAL_ERROR);
		}
	}
}
