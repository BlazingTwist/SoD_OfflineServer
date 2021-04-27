package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.util.Calendar;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;

public class GetAuthoritativeTime extends WebServiceFunction {
	public static final String PARAM_API_KEY = "apiKey";
	public static final String PARAM_API_TOKEN = "apiToken";

	@WebServiceFunctionConstructor
	public GetAuthoritativeTime(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if(!WebFunctionUtils.checkKeysPresent(body, PARAM_API_KEY, PARAM_API_TOKEN)){
			respond(exchange, 500, INVALID_BODY);
			return;
		}

		String dateTime = DatatypeConverter.printDateTime(Calendar.getInstance());
		respond(exchange, 200, "<dateTime>" + dateTime + "</dateTime>");
	}
}
