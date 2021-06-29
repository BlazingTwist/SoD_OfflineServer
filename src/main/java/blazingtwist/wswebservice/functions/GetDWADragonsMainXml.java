package blazingtwist.wswebservice.functions;

import blazingtwist.crypto.TripleDes;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class GetDWADragonsMainXml extends WebServiceFunction {
	public static final boolean provideLocalUrls = false;

	@WebServiceFunctionConstructor
	public GetDWADragonsMainXml(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		InputStream xmlStream;
		if(provideLocalUrls){
			xmlStream = this.getClass().getClassLoader().getResourceAsStream("GameConfigLocal.xml");
		}else{
			xmlStream = this.getClass().getClassLoader().getResourceAsStream("GameConfig.xml");
		}
		if(xmlStream == null){
			logger.error("Failed to load GameConfig.xml");
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		try {
			String encryptedConfig = TripleDes.encryptAscii(new String(xmlStream.readAllBytes(), StandardCharsets.UTF_8));
			respond(exchange, 200, encryptedConfig);
		} catch (IOException e) {
			logger.error("Unexpected error in {}", this.getClass().getSimpleName(), e);
			respond(exchange, 500, INTERNAL_ERROR);
		}
	}
}
