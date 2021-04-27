package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.ArrayOfAchievementTaskInfo;
import java.util.Map;

public class GetAchievementTaskInfo extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetAchievementTaskInfo(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// TODO
		ArrayOfAchievementTaskInfo taskInfo = new ArrayOfAchievementTaskInfo();
		respondXml(exchange, 200, taskInfo, "ArrayOfAchievementTaskInfo", false);
	}
}
