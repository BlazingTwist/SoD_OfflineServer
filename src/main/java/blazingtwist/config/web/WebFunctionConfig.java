package blazingtwist.config.web;

import blazingtwist.config.JsonDefaultConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

public class WebFunctionConfig {
	@JsonProperty("enabled")
	private boolean enabled;

	@JsonProperty("contextName")
	private String contextName;

	@JsonDefaultConstructor
	private WebFunctionConfig() {
	}

	public boolean isEnabled() {
		return enabled;
	}

	public String getContextName() {
		return contextName;
	}
}
