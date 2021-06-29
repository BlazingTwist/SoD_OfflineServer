package blazingtwist.crypto;

/*
 * Credits: https://gist.github.com/riversun/6e15306cd6e3b1b37687a0e5cec1cef1
 */

import blazingtwist.logback.LogbackLoggerProvider;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;

public class TripleDes {
	private static final Logger logger = LogbackLoggerProvider.getLogger(TripleDes.class);

	private static final String CRYPT_ALGORITHM = "DESede";
	private static final String PADDING = "DESede/ECB/PKCS5Padding";

	/**
	 * Secret key used for WsWebService
	 */
	public static final String KEY = "F5D573D9-CDB3-4142-9462-F2A5DEF0B7E8";

	/**
	 * Secret key used for the media-server DWADragonsMain.xml (defines Server URLs)
	 */
	public static final String KEY_ASCII = "C92EC1AA-54CD-4D0C-A8D5-403FCCF1C0BD";

	public static byte[] getKeyHash() throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(KEY.getBytes(StandardCharsets.UTF_16LE));
		byte[] hash = md5.digest();

		// repeat first 8 bytes at the end to get full 24 byte key length
		byte[] fullHash = Arrays.copyOf(hash, 24);
		for (int i = 0, i2 = 16; i < 8; i++, i2++) {
			fullHash[i2] = hash[i];
		}

		return fullHash;
	}

	public static String decrypt(String ciphertext) {
		if (ciphertext == null) {
			return null;
		}

		try {
			SecretKeySpec keySpec = new SecretKeySpec(getKeyHash(), CRYPT_ALGORITHM);
			Cipher cipher = Cipher.getInstance(PADDING);
			cipher.init(Cipher.DECRYPT_MODE, keySpec);
			byte[] decryptBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));

			return new String(decryptBytes, StandardCharsets.UTF_16LE);
		} catch (Exception e) {
			logger.error("Unexpected TripleDes exception", e);
			return null;
		}
	}

	public static String encrypt(String plaintext) {
		if (plaintext == null) {
			return null;
		}

		try {
			SecretKeySpec keySpec = new SecretKeySpec(getKeyHash(), CRYPT_ALGORITHM);
			Cipher cipher = Cipher.getInstance(PADDING);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);
			byte[] encryptBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_16LE));

			return new String(Base64.getEncoder().encode(encryptBytes));
		} catch (Exception e) {
			logger.error("Unexpected TripleDes exception", e);
			return null;
		}
	}

	private static byte[] getKeyHashAscii() throws NoSuchAlgorithmException {
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		md5.update(KEY_ASCII.getBytes(StandardCharsets.UTF_8));
		byte[] hash = md5.digest();

		// repeat first 8 bytes at the end to get full 24 byte key length
		byte[] fullHash = Arrays.copyOf(hash, 24);
		for (int i = 0, i2 = 16; i < 8; i++, i2++) {
			fullHash[i2] = hash[i];
		}

		return fullHash;
	}

	public static String decryptAscii(String ciphertext) {
		if (ciphertext == null) {
			return null;
		}

		try {
			SecretKeySpec keySpec = new SecretKeySpec(getKeyHashAscii(), CRYPT_ALGORITHM);
			Cipher cipher = Cipher.getInstance(PADDING);
			cipher.init(Cipher.DECRYPT_MODE, keySpec);
			byte[] decryptBytes = cipher.doFinal(Base64.getDecoder().decode(ciphertext));

			return new String(decryptBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			logger.error("Unexpected TripleDes exception", e);
			return null;
		}
	}

	public static String encryptAscii(String plaintext) {
		if (plaintext == null) {
			return null;
		}

		try {
			SecretKeySpec keySpec = new SecretKeySpec(getKeyHashAscii(), CRYPT_ALGORITHM);
			Cipher cipher = Cipher.getInstance(PADDING);
			cipher.init(Cipher.ENCRYPT_MODE, keySpec);
			byte[] encryptBytes = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

			return new String(Base64.getEncoder().encode(encryptBytes));
		} catch (Exception e) {
			logger.error("Unexpected TripleDes exception", e);
			return null;
		}
	}
}
