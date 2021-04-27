package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GetAllRanks extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetAllRanks(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// TODO why does this exist?!

		/*
		* TODO unused client side garbage
		*   UserRank:
		*     RankID (value from server is ignored and immediate overwritten)
		*     Name (never used)
		*     Description (never used)
		*     Image (used, but always null)
		*     Audio (never used AND always null)
		*     IsMember (never used, always false)
		* */

		/*
		* UserRank remaining relevant data:
		*   RankID (kind of, just assign 0 for now)
		*   Value (used a lot)
		*   GlobalRankID (used... for something?)
		* */

		InputStream xmlStream = this.getClass().getClassLoader().getResourceAsStream("AllRankData.xml");
		if(xmlStream == null){
			System.err.println("Failed to load AllRankData.xml");
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		try{
			respond(exchange, 200, new String(xmlStream.readAllBytes(), StandardCharsets.UTF_8));
		} catch (IOException e) {
			e.printStackTrace();
			respond(exchange, 500, INTERNAL_ERROR);
		}
	}
}
