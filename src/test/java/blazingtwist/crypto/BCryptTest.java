package blazingtwist.crypto;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.IllegalBCryptFormatException;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class BCryptTest {

	public static final String PASSWORD_PLAINTEXT = "BlazingTwist.TestPassword";
	public static final String PASSWORD_HASH = "$2a$12$daRF1I3tCB6yKsWABSuHpeuXFHXniJszQdqEkbf9zCP/wzdMzGVW6";

	public static final BCrypt.Version VERSION = BCrypt.Version.VERSION_2A;
	public static final int COST = 12;
	public static final byte[] SALT = new byte[]{0x7D, (byte) 0xC4, (byte) 0xC7, (byte) 0xDC, (byte) 0xAE, 0x6F, 0x10, 0x3F, 0x34, 0x32, (byte) 0xE6, 0x02, 0x0D, 0x4C, 0x09, (byte) 0xAE};

	/**
	 * The Idea here is to verify the login process on the Client Side
	 * <p>
	 * the client will be provided with Version, Cost and Salt
	 * it then determines the BCrypt hash using the plaintext password
	 * <p>
	 * Verify that the generated hash matches the stored one
	 */
	@Test
	public void testPasswordMatchingClientSide() throws IllegalBCryptFormatException {
		// Given
		BCrypt.Version version;
		int cost;
		byte[] salt;
		{
			BCrypt.HashData hashData = BCrypt.Version.VERSION_2A.parser.parse(PASSWORD_HASH.getBytes(StandardCharsets.UTF_8));
			version = hashData.version;
			cost = hashData.cost;
			salt = hashData.rawSalt;
		}

		// Client calculates Hash
		BCrypt.Hasher hasher = BCrypt.with(version);
		byte[] hash = hasher.hash(cost, salt, PASSWORD_PLAINTEXT.getBytes(StandardCharsets.UTF_16LE));

		// Server compares hashes
		assertEquals(PASSWORD_HASH, new String(hash, StandardCharsets.UTF_8), "Computed hash did not match stored one");
	}

	/**
	 * Verify Server-side login process
	 * * check that the extracted Version, Cost and Salt are correct
	 */
	@Test
	public void testPasswordMatchingServerSide() throws IllegalBCryptFormatException {
		BCrypt.HashData hashData = BCrypt.Version.VERSION_2A.parser.parse(PASSWORD_HASH.getBytes(StandardCharsets.UTF_8));
		assertEquals(VERSION, hashData.version, "Extracted incorrect Version");
		assertEquals(COST, hashData.cost, "Extracted incorrect Cost-Value");
		assertTrue(() -> {
			for (int i = 0; i < hashData.rawSalt.length; i++) {
				if (hashData.rawSalt[i] != SALT[i]) {
					return false;
				}
			}
			return true;
		}, "Extracted incorrect Salt");
	}
}
