package blazingtwist.wswebservice.functions;

import blazingtwist.config.JsonDefaultConstructor;
import blazingtwist.wswebservice.WebServiceFunction;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class GetAvatarData extends WebServiceFunction {
	@JsonDefaultConstructor
	public GetAvatarData() {
		super(GetAvatarData.class.getSimpleName());
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params) {
		// TODO
	}
}
