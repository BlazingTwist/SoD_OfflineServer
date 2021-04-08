package blazingtwist.wswebservice;

import blazingtwist.crypto.TripleDes;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

public class WebFunctionUtils {
	public static final Pattern paramPattern = Pattern.compile("^(.*?)=(.*)$");

	public static Map<String, String> readUrlMap(String string) {
		return Arrays.stream(string.split("&"))
				.map(paramPattern::matcher)
				.filter(matcher -> matcher.matches() && matcher.groupCount() == 2)
				.collect(Collectors.toMap(matcher -> matcher.group(1), matcher -> matcher.group(2)));
	}

	public static <K, V> boolean checkKeysPresent(Map<K, V> map, K... keys) {
		if (keys == null || keys.length == 0) {
			return true;
		}

		if (map == null) {
			return false;
		}

		for (K key : keys) {
			if (!map.containsKey(key)) {
				return false;
			}
		}
		return true;
	}

	public static <T> T unmarshalEncryptedXml(String encryptedString, Class<? extends T> clazz) throws JAXBException {
		return unmarshalXml(TripleDes.decrypt(encryptedString), clazz);
	}

	public static <T> T unmarshalXml(String xmlString, Class<? extends T> clazz) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return unmarshaller
				.unmarshal(new StreamSource(new StringReader(xmlString)), clazz)
				.getValue();
	}
}
