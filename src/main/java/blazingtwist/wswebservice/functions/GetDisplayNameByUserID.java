package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class GetDisplayNameByUserID extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetDisplayNameByUserID(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		/*try {
			JAXBContext contextObject = JAXBContext.newInstance(String.class);
			Marshaller marshaller = contextObject.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			StringWriter writer = new StringWriter();

			JAXBElement<String> element = new JAXBElement<>(
					QName.valueOf("string"),
					String.class,
					"test");

			marshaller.marshal(element, writer);
			System.err.println("String: " + writer.toString());
		} catch (JAXBException e) {
			e.printStackTrace();
		}*/

		// TODO
	}
}
