package blazingtwist.wswebservice.functions;

import blazingtwist.database.MainDBAccessor;
import blazingtwist.database.SSOTokenInfo;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.GetStoreRequest;
import generated.GetStoreResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Map;
import javax.xml.bind.JAXBException;

public class GetStore extends WebServiceFunction {
	public static final String PARAM_API_KEY = "apiKey";
	public static final String PARAM_API_TOKEN = "apiToken";
	public static final String PARAM_GET_STORE_REQUEST = "getStoreRequest";

	@WebServiceFunctionConstructor
	public GetStore(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if (!WebFunctionUtils.checkKeysPresent(body, PARAM_API_KEY, PARAM_API_TOKEN, PARAM_GET_STORE_REQUEST)) {
			respond(exchange, 400, INVALID_BODY);
			return;
		}

		SSOTokenInfo tokenInfo;
		try {
			tokenInfo = MainDBAccessor.getSSOTokenInfo(body.get(PARAM_API_TOKEN));
		} catch (SQLException throwables) {
			throwables.printStackTrace();
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		if (tokenInfo == null || tokenInfo.getExpired()) {
			respond(exchange, 401, "Token is expired!");
			return;
		}

		GetStoreRequest request;
		try {
			request = WebFunctionUtils.unmarshalXml(body.get(PARAM_GET_STORE_REQUEST), GetStoreRequest.class);
		} catch (JAXBException e) {
			e.printStackTrace();
			respond(exchange, 400, "Cannot parse getStoreRequest");
			return;
		}

		/*
		* Notes:
		*   useless:
		*     Store::StoreName
		*     Store::Description
		*     ItemsInStoreDataSale::RankId
		*     ItemData::stackable
		*     ItemData::allowStacking
		*     ItemState::AchievementID
		*     ItemState::Rewards
		* */

		GetStoreResponse response = new GetStoreResponse();
		if (request != null && request.getStoreIDs().size() > 0) {
			// TODO handle store request
		}

		//respondXml(exchange, 200, response, "GetStoreResponse", false);

		InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream("TestStoreResponse.xml");
		if (xmlStream == null) {
			System.err.println("Failed to load TestStoreResponse.xml!");
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		try {
			respond(exchange, 200, new String(xmlStream.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
			respond(exchange, 500, INTERNAL_ERROR);
		}
	}
}
