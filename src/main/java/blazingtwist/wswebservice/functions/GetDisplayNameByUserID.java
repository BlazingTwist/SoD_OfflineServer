package blazingtwist.wswebservice.functions;

import blazingtwist.config.JsonDefaultConstructor;
import blazingtwist.wswebservice.WebServiceFunction;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class GetDisplayNameByUserID extends WebServiceFunction {
	@JsonDefaultConstructor
	public GetDisplayNameByUserID() {
		super(GetDisplayNameByUserID.class.getSimpleName());
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params) {
		// TODO
	}
}
