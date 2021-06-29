package blazingtwist.crypto;

import blazingtwist.logback.LogbackLoggerProvider;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;

public class MD5 {
	private static final Logger logger = LogbackLoggerProvider.getLogger(MD5.class);

	private static byte[] getHash(String text, Charset charset) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("MD5");
			md5.update(text.getBytes(charset));
			return md5.digest();
		} catch (NoSuchAlgorithmException e) {
			logger.error("Unexpected MD5 exception", e);
			return null;
		}
	}

	private static String buildString(Object... objects) {
		return Arrays.stream(objects).map(Object::toString).collect(Collectors.joining());
	}

	public static byte[] getAsciiHash(String text) {
		return getHash(text, StandardCharsets.UTF_8);
	}

	public static byte[] getAsciiHash(Object... objects) {
		return getAsciiHash(buildString(objects));
	}

	public static String getAsciiHashHex(Object... objects) {
		return Hex.encodeHexString(getAsciiHash(buildString(objects)), true);
	}

	public static byte[] getUnicodeHash(String text) {
		return getHash(text, StandardCharsets.UTF_16LE);
	}

	public static byte[] getUnicodeHash(Object... objects) {
		return getUnicodeHash(buildString(objects));
	}

	public static String getUnicodeHashHex(Object... objects) {
		return Hex.encodeHexString(getUnicodeHash(buildString(objects)), true);
	}
}
