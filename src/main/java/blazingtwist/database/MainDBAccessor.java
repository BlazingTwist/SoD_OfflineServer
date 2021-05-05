package blazingtwist.database;

import blazingtwist.config.sql.SQLConfig;
import blazingtwist.crypto.MD5;
import generated.Gender;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MainDBAccessor {
	private static Connection mainDBConnection = null;
	private static long lastSSOParentTokensUpdate = 0;
	private static final long SSOTokenUpdateFrequency = 60_000L; // update token validity once every minute
	private static final long SSOTokenDuration = 300_000L; // expire tokens older than 5 minutes
	private static final long SSOTokenDurationExpired = 3_600_000L; // delete expired tokens older than 1 hour

	public static void initialize(SQLConfig sqlConfig) {
		try {
			mainDBConnection = DriverManager.getConnection("jdbc:sqlite:" + sqlConfig.getPath());
		} catch (SQLException throwables) {
			System.err.println("Initialization of MainDBAccessor failed!");
			throwables.printStackTrace();
		}
	}

	public static void disconnect() {
		if (mainDBConnection != null) {
			try {
				mainDBConnection.close();
			} catch (SQLException throwables) {
				System.err.println("Failed to close mainDBConnection!");
				throwables.printStackTrace();
			}
		}
	}

	public static String buildGUID(String text) {
		String hash = MD5.getUnicodeHashHex(text);
		return hash.substring(0, 8) + "-" + hash.substring(8, 12) + "-" + hash.substring(12, 16) + "-" + hash.substring(16, 20) + "-" + hash.substring(20);
	}

	public static void addParentUser(String userName, String password) throws SQLException {
		PreparedStatement statement = mainDBConnection.prepareStatement("insert into ParentUsers (UserName, UserID, Password) values(?, ?, ?)");
		statement.setString(1, userName);
		statement.setString(2, buildGUID(userName));
		statement.setString(3, password);
		statement.executeUpdate();
		statement.close();
	}

	/**
	 * @param key either ParentUserName or ParentUserID
	 */
	public static ParentUserInfo getParentUserInfo(String key) {
		ParentUserInfo userInfo = null;
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("select UserName, UserID, Password from ParentUsers where (UserName = ?) or (UserID = ?)");
			statement.setString(1, key);
			statement.setString(2, key);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				userInfo = new ParentUserInfo(
						resultSet.getString(1),
						resultSet.getString(2),
						resultSet.getString(3)
				);
			}
			statement.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		return userInfo;
	}

	public static List<ChildUserInfo> getChildUserInfoForParent(String parentName) throws SQLException {
		PreparedStatement statement = mainDBConnection.prepareStatement("select UserName, UserID, CashCurrency, GameCurrency, Gender, MultiplayerEnabled from ChildUsers where ParentUserName = ?");
		statement.setString(1, parentName);
		ResultSet resultSet = statement.executeQuery();
		List<ChildUserInfo> children = new ArrayList<>();
		while (resultSet.next()) {
			children.add(new ChildUserInfo(
					resultSet.getString(1),
					resultSet.getString(2),
					resultSet.getInt(4),
					resultSet.getInt(3),
					Gender.fromValue(resultSet.getInt(5)),
					resultSet.getBoolean(6),
					parentName
			));
		}
		return children;
	}

	public static ChildUserInfo getChildUserInfo(String key) {
		ChildUserInfo userInfo = null;
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("select UserName, UserID, CashCurrency, GameCurrency, Gender, MultiplayerEnabled, ParentUserName from ChildUsers where (UserName = ?) or (UserID = ?)");
			statement.setString(1, key);
			statement.setString(2, key);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				userInfo = new ChildUserInfo(
						resultSet.getString(1),
						resultSet.getString(2),
						resultSet.getInt(4), // CashCurrency
						resultSet.getInt(3), // GameCurrency
						Gender.fromValue(resultSet.getInt(5)), // GenderID
						resultSet.getBoolean(6), // MultiplayEnabled
						resultSet.getString(7) // ParentUserName
				);
			}
			statement.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		return userInfo;
	}

	private static void updateSSOTokens() {
		long now = System.currentTimeMillis();
		// Trigger update (at most) every 60 seconds
		if ((now - lastSSOParentTokensUpdate) >= SSOTokenUpdateFrequency) {
			lastSSOParentTokensUpdate = now;
			expiredUnusedSSOTokens(now);
			deleteExpiredTokens(now);
		}
	}

	private static void expiredUnusedSSOTokens(long now) {
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveParentTokens set LastRefresh = 0, Expired = 1, ExpiredSince = ?, ExpiredByLogin = 0 where Expired = 0 and LastRefresh < ?");
			statement.setLong(1, now);
			statement.setLong(2, now - SSOTokenDuration);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveChildTokens set LastRefresh = 0, Expired = 1, ExpiredSince = ?, ExpiredByLogin = 0 where Expired = 0 and LastRefresh < ?");
			statement.setLong(1, now);
			statement.setLong(2, now - SSOTokenDuration);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	private static void deleteExpiredTokens(long now) {
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("delete from ActiveParentTokens where Expired = 1 and ExpiredSince < ?");
			statement.setLong(1, now - SSOTokenDurationExpired); // delete expired tokens after one hour
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("delete from ActiveChildTokens where Expired = 1 and ExpiredSince < ?");
			statement.setLong(1, now - SSOTokenDurationExpired); // delete expired tokens after one hour
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	public static void invalidateExistingParentTokens(String userName) {
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveParentTokens set LastRefresh = 0, Expired = 1, ExpiredSince = ?, ExpiredByLogin = 1 where UserName = ? and Expired = 0");
			statement.setLong(1, System.currentTimeMillis());
			statement.setString(2, userName);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	public static void invalidateExistingChildTokens(String userName) {
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveChildTokens set LastRefresh = 0, Expired = 1, ExpiredSince = ?, ExpiredByLogin = 1 where UserName = ? and Expired = 0");
			statement.setLong(1, System.currentTimeMillis());
			statement.setString(2, userName);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	public static boolean isSSOTokenUnique(String token) throws SQLException {
		PreparedStatement statement = mainDBConnection.prepareStatement("select * from ActiveParentTokens where Token = ?");
		statement.setString(1, token);
		ResultSet resultSet = statement.executeQuery();
		boolean isUnique = !resultSet.next();
		statement.close();

		if (isUnique) {
			statement = mainDBConnection.prepareStatement("select * from ActiveChildTokens where Token = ?");
			statement.setString(1, token);
			resultSet = statement.executeQuery();
			isUnique = !resultSet.next();
			statement.close();
		}
		return isUnique;
	}

	public static SSOParentTokenInfo getSSOParentTokenInfo(String token) throws SQLException {
		updateSSOTokens();
		SSOParentTokenInfo parentTokenInfo = null;
		PreparedStatement statement = mainDBConnection.prepareStatement("select Expired, ExpiredByLogin, UserName from ActiveParentTokens where Token = ?");
		statement.setString(1, token);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			parentTokenInfo = new SSOParentTokenInfo(
					token,
					resultSet.getBoolean(1),
					resultSet.getBoolean(2),
					resultSet.getString(3)
			);
		}
		return parentTokenInfo;
	}

	public static SSOChildTokenInfo getSSOChildTokenInfo(String token) throws SQLException {
		updateSSOTokens();
		SSOChildTokenInfo childTokenInfo = null;
		PreparedStatement statement = mainDBConnection.prepareStatement("select Expired, ExpiredByLogin, UserName from ActiveChildTokens where Token = ?");
		statement.setString(1, token);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			childTokenInfo = new SSOChildTokenInfo(
					token,
					resultSet.getBoolean(1),
					resultSet.getBoolean(2),
					resultSet.getString(3)
			);
		}
		return childTokenInfo;
	}

	public static SSOTokenInfo getSSOTokenInfo(String token) throws SQLException {
		SSOTokenInfo tokenInfo = getSSOChildTokenInfo(token);
		if(tokenInfo != null){
			return tokenInfo;
		}
		return getSSOParentTokenInfo(token);
	}

	public static void addSSOParentToken(String token, String userName) throws SQLException {
		updateSSOTokens();
		PreparedStatement statement = mainDBConnection.prepareStatement("insert into ActiveParentTokens (Token, UserName, LastRefresh, Expired, ExpiredSince, ExpiredByLogin) values(?, ?, ?, 0, 0, 0)");
		addSSOToken(token, userName, statement);
	}

	public static void addSSOChildToken(String token, String userName) throws SQLException {
		updateSSOTokens();
		PreparedStatement statement = mainDBConnection.prepareStatement("insert into ActiveChildTokens (Token, UserName, LastRefresh, Expired, ExpiredSince, ExpiredByLogin) values(?, ?, ?, 0, 0, 0)");
		addSSOToken(token, userName, statement);
	}

	public static void addSSOToken(String token, String userName, PreparedStatement statement) throws SQLException {
		statement.setString(1, token);
		statement.setString(2, userName);
		statement.setLong(3, System.currentTimeMillis());
		statement.executeUpdate();
		statement.close();
	}

	public static boolean refreshSSOParentToken(String token) throws SQLException {
		updateSSOTokens();

		PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveParentTokens set LastRefresh = ? where Token = ? and Expired = 0");
		statement.setLong(1, System.currentTimeMillis());
		statement.setString(2, token);
		int updatedRows = statement.executeUpdate();
		statement.close();
		return updatedRows == 1;
	}
}
