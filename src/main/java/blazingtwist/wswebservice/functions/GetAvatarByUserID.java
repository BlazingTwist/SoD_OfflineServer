package blazingtwist.wswebservice.functions;

import blazingtwist.config.JsonDefaultConstructor;
import blazingtwist.wswebservice.WebServiceFunction;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class GetAvatarByUserID extends WebServiceFunction {
	@JsonDefaultConstructor
	public GetAvatarByUserID() {
		super(GetAvatarByUserID.class.getSimpleName());
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params) {
		// TODO
	}
}
