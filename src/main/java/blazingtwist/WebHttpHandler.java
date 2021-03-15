package blazingtwist;

import blazingtwist.config.web.WebConfig;
import blazingtwist.wswebservice.WebServiceFunction;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

public class WebHttpHandler {
	private final HttpServer server;

	public WebHttpHandler(WebConfig webConfig, List<WebServiceFunction> webServiceFunctions) throws IOException {
		InetSocketAddress address = new InetSocketAddress(webConfig.getAddress(), webConfig.getPort());
		server = HttpServer.create(address, 0);

		if (webServiceFunctions != null) {
			for (WebServiceFunction webServiceFunction : webServiceFunctions) {
				webServiceFunction.register(server);
			}
		}

		server.setExecutor(Executors.newFixedThreadPool(webConfig.getThreadCount()));
		System.out.println("Starting Server on: " + server.getAddress());
		server.start();
	}
}
