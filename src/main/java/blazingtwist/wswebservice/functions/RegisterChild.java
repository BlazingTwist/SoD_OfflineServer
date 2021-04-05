package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class RegisterChild extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public RegisterChild(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params) {
		// if ChildRegistrationData.IsSuggestAvatarName is set, then return RegistrationResult
		// otherwise return MembershipUserStatus

		// TODO
	}
}
