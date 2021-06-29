package blazingtwist.wswebservice;

import blazingtwist.logback.LogbackLoggerProvider;
import blazingtwist.crypto.TripleDes;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;

public class WebFunctionUtils {
	private static final Logger logger = LogbackLoggerProvider.getLogger(WebFunctionUtils.class);
	public static final Pattern paramPattern = Pattern.compile("^(.*?)=(.*)$");

	public static Map<String, String> readUrlMap(String string) {
		return Arrays.stream(string.split("&"))
				.map(paramPattern::matcher)
				.filter(matcher -> matcher.matches() && matcher.groupCount() == 2)
				.collect(Collectors.toMap(matcher -> matcher.group(1), matcher -> matcher.group(2)));
	}

	@SafeVarargs
	public static <K, V> boolean checkKeysPresent(Map<K, V> map, K... keys) {
		if (keys == null || keys.length == 0) {
			return true;
		}

		if (map == null) {
			return false;
		}

		for (K key : keys) {
			if (!map.containsKey(key)) {
				logger.info("missing key: {}", key);
				return false;
			}
		}
		return true;
	}

	public static <T> T unmarshalEncryptedXml(String encryptedString, Class<T> clazz) throws JAXBException {
		return unmarshalXml(TripleDes.decrypt(encryptedString), clazz);
	}

	public static <T> T unmarshalXml(String xmlString, Class<T> clazz) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return unmarshaller
				.unmarshal(new StreamSource(new StringReader(xmlString)), clazz)
				.getValue();
	}

	public static <T> String marshalEncryptedXml(T data, String rootName, Class<T> clazz, boolean fragment) throws JAXBException {
		return TripleDes.encrypt(marshalXml(data, rootName, clazz, fragment));
	}

	public static <T> String marshalXml(T data, String rootName, Class<T> clazz, boolean fragment) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, fragment);
		StringWriter writer = new StringWriter();

		JAXBElement<T> element = new JAXBElement<>(new QName(rootName), clazz, data);
		marshaller.marshal(element, writer);
		return writer.toString();
	}
}
