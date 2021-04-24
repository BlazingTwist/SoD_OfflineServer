package blazingtwist.config.sql;

import blazingtwist.config.JsonDefaultConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SQLConfig {
	@JsonProperty("path")
	private String path;

	@JsonDefaultConstructor
	private SQLConfig() {
	}

	public String getPath() {
		return path;
	}
}
