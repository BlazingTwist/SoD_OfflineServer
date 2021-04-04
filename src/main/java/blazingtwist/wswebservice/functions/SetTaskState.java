package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class SetTaskState extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public SetTaskState(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params) {
		// TODO
	}
}
