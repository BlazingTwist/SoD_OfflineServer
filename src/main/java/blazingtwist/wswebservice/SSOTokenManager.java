package blazingtwist.wswebservice;

import at.favre.lib.crypto.bcrypt.BCrypt;
import blazingtwist.database.MainDBAccessor;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Random;

public class SSOTokenManager {
	private static final Random random = new Random();

	public static String generateParentToken(String userName) throws SQLException {
		MainDBAccessor.invalidateExistingTokens(userName);

		byte[] salt = new byte[16];
		BCrypt.Hasher hasher = BCrypt.with(BCrypt.Version.VERSION_2A);
		Base64.Encoder encoder = Base64.getEncoder();
		String token;
		do {
			random.nextBytes(salt);
			token = encoder.encodeToString(hasher.hash(6, salt, userName.getBytes(StandardCharsets.UTF_16LE)));
		} while (!MainDBAccessor.isSSOParentTokenUnique(token));

		MainDBAccessor.addSSOParentToken(token, userName);
		return token;
	}
}
