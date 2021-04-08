package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.SubscriptionInfo;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.GregorianCalendar;
import java.util.Map;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

public class GetSubscriptionInfo extends WebServiceFunction {
	public static final String PARAM_API_TOKEN = "apiToken";
	public static final String PARAM_API_KEY = "apiKey";

	@WebServiceFunctionConstructor
	public GetSubscriptionInfo(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// TODO static response, remove serverside and fill in on clientside instead

		if(!WebFunctionUtils.checkKeysPresent(body, PARAM_API_TOKEN, PARAM_API_KEY)){
			respond(exchange, 500, INVALID_BODY);
			return;
		}

		SubscriptionInfo result = new SubscriptionInfo();
		result.setRecurring(true); // recurring subscriptions aren't checked for expiration
		result.setStatus("Member"); // only used for checking Trial-Users ('Trial')

		// set expiry to be one year from now
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(Date.from(Instant.now().plus(365, ChronoUnit.DAYS)));
		try {
			result.setSubscriptionEndDate(DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar));
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}

		result.setSubscriptionTypeID(1);

		respondXml(exchange, 200, result, "SubscriptionInfo", false);
	}
}
