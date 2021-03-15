package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.util.Map;

public class SetInventoryData extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public SetInventoryData(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params) {
		/*try {
			JAXBContext contextObject = JAXBContext.newInstance(Boolean.class);
			Marshaller marshaller = contextObject.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			StringWriter writer = new StringWriter();

			JAXBElement<String> element = new JAXBElement<>(
					QName.valueOf("bool"),
					Boolean.class,
					true);

			marshaller.marshal(element, writer);
			System.err.println("String: " + writer.toString());
		} catch (JAXBException e) {
			e.printStackTrace();
		}*/

		// TODO
	}
}
