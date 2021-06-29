package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GetRankAttributeData extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetRankAttributeData(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// TODO why does this exist?!

		InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream("GetRankAttributeData.xml");
		if(xmlStream == null){
			logger.error("Failed to load GetRankAttributeData.xml");
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		try{
			respond(exchange, 200, new String(xmlStream.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			logger.error("Unexpected error in {}", this.getClass().getSimpleName(), e);
			respond(exchange, 500, INTERNAL_ERROR);
		}
	}
}
