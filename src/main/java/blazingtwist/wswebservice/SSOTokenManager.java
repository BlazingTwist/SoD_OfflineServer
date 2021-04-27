package blazingtwist.wswebservice;

import at.favre.lib.crypto.bcrypt.BCrypt;
import blazingtwist.database.MainDBAccessor;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Locale;
import java.util.Random;

public class SSOTokenManager {
	private static final Random random = new Random();

	private static String generateSSOToken(String userName) throws SQLException {
		byte[] salt = new byte[16];
		BCrypt.Hasher hasher = BCrypt.with(BCrypt.Version.VERSION_2A);
		Base64.Encoder encoder = Base64.getEncoder();
		String token;
		do {
			random.nextBytes(salt);
			token = encoder.encodeToString(hasher.hash(6, salt, userName.getBytes(StandardCharsets.UTF_16LE))).toLowerCase(Locale.ENGLISH);
		} while (!MainDBAccessor.isSSOTokenUnique(token));

		return token;
	}

	public static String generateParentToken(String userName) throws SQLException {
		MainDBAccessor.invalidateExistingParentTokens(userName);
		String token = generateSSOToken(userName);
		MainDBAccessor.addSSOParentToken(token, userName);
		return token;
	}

	public static String generateChildToken(String userName) throws SQLException {
		MainDBAccessor.invalidateExistingChildTokens(userName);
		String token = generateSSOToken(userName);
		MainDBAccessor.addSSOChildToken(token, userName);
		return token;
	}
}
