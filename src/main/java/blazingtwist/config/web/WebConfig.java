package blazingtwist.config.web;

import blazingtwist.config.JsonDefaultConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;

public class WebConfig {
	@JsonProperty("address")
	private String address;

	@JsonProperty("port")
	private int port;

	@JsonProperty("threadCount")
	private int threadCount;

	@JsonProperty("functions")
	private HashMap<String, WebFunctionConfig> webServiceFunctions;

	@JsonDefaultConstructor
	private WebConfig() {
	}

	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}

	public int getThreadCount() {
		return threadCount;
	}

	public HashMap<String, WebFunctionConfig> getWebServiceFunctions() {
		return webServiceFunctions;
	}
}
