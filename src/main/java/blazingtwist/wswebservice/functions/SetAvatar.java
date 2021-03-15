package blazingtwist.wswebservice.functions;

import blazingtwist.config.JsonDefaultConstructor;
import blazingtwist.wswebservice.WebServiceFunction;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class SetAvatar extends WebServiceFunction {
	@JsonDefaultConstructor
	public SetAvatar() {
		super(SetAvatar.class.getSimpleName());
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params) {
		// TODO
	}
}
