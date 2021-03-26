package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class GetPetAchievementsByUserID extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetPetAchievementsByUserID(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params) {
		// TODO
	}
}
