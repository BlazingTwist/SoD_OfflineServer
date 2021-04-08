package blazingtwist;

import blazingtwist.config.util.ConfigUtils;
import blazingtwist.config.web.WebConfig;
import blazingtwist.config.web.WebFunctionConfig;
import blazingtwist.wswebservice.WebServiceFunction;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import org.reflections.Reflections;

public class Main {
	private static WebHttpHandler webHandler;

	public static WebHttpHandler getWebHandler() {
		return webHandler;
	}

	private static Set<Class<? extends WebServiceFunction>> findWebFunctions() {
		Reflections reflections = new Reflections("blazingtwist.wswebservice.functions");
		return reflections.getSubTypesOf(WebServiceFunction.class);
	}

	public static void main(String[] args) {
		WebConfig webConfig = ConfigUtils.loadConfig(WebConfig.class, "WebConfig.conf");
		HashMap<String, WebFunctionConfig> webFunctionsConfig = webConfig.getWebServiceFunctions();
		ArrayList<WebServiceFunction> webFunctions = new ArrayList<>();

		Set<Class<? extends WebServiceFunction>> webFunctionTypes = findWebFunctions();
		for (Class<? extends WebServiceFunction> webFunctionType : webFunctionTypes) {

			String functionName = webFunctionType.getSimpleName();
			if (!webFunctionsConfig.containsKey(functionName)) {
				continue;
			}

			WebFunctionConfig functionConfig = webFunctionsConfig.get(functionName);
			if (!functionConfig.isEnabled()) {
				continue;
			}

			try {
				webFunctions.add(webFunctionType.getDeclaredConstructor(String.class).newInstance(functionConfig.getContextName()));
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				System.err.println("Unable to load WebFunction: " + webFunctionType.getSimpleName());
				e.printStackTrace();
			}
		}

		try {
			webHandler = new WebHttpHandler(webConfig, webFunctions);
		} catch (IOException e) {
			System.err.println("Unable to start Server, exception: " + e);
			System.exit(-1);
		}
	}
}
