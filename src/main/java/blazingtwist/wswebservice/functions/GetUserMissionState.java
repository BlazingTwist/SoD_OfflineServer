package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class GetUserMissionState extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetUserMissionState(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// two different calls
		// * string (userId) | MissionRequestFilter (filter)
		// * string (userId) | MissionRequestFilterV2 (filter)

		// TODO
	}
}
