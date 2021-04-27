package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.CommonInventoryData;
import java.util.Map;

public class GetCommonInventoryData extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetCommonInventoryData(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// two different calls
		// * apiToken | apiKey | ContainerId
		// * apiToken | apiKey | getCommonInventoryRequestXml

		// TODO
		CommonInventoryData inventoryData = new CommonInventoryData();
		respondXml(exchange, 200, inventoryData, "CI", false);
	}
}
