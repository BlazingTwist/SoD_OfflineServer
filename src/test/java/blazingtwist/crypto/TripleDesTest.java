package blazingtwist.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class TripleDesTest {
	public static final String CIPHER_TEXT = "RjCCPGkzRQjfrQeeDHXUVkcfY/ScjPU+3zmqIvPqfwdvM3Ij8KmCncGqb1a3gPv+GA4mHGbBYn+g/kjFnn51ucUOv+1zRXHSddjIhJGc8YY=";
	public static final String PLAIN_TEXT = "59ba125d-2e2e-4b8c-b5b1-9a148f8d827f";

	@Test
	public void testEncryption() {
		String decryptResult = TripleDes.decrypt(CIPHER_TEXT);
		assertEquals(PLAIN_TEXT, decryptResult);
	}

	@Test
	public void testDecryption() {
		String encryptResult = TripleDes.encrypt(PLAIN_TEXT);
		assertEquals(CIPHER_TEXT, encryptResult);
	}
}
