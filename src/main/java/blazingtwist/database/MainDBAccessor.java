package blazingtwist.database;

import blazingtwist.config.sql.SQLConfig;
import blazingtwist.wswebservice.SSOTokenInfo;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainDBAccessor {
	private static Connection mainDBConnection = null;
	private static long lastSSOParentTokensUpdate = 0;
	private static final long SSOParentTokenUpdateFrequency = 60_000L; // update token validity once every minute
	private static final long SSOParentTokenDuration = 300_000L; // expire tokens older than 5 minutes
	private static final long SSOParentTokenDurationExpired = 3_600_000L; // delete expired tokens older than 1 hour

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

	public static void addParentUser(String userName, String password) throws SQLException {
		PreparedStatement statement = mainDBConnection.prepareStatement("insert into ParentUsers (UserName, Password) values(?, ?)");
		statement.setString(1, userName);
		statement.setString(2, password);
		statement.executeUpdate();
		statement.close();
	}

	public static String getParentUserPassword(String userName) throws SQLException {
		String password = null;
		PreparedStatement statement = mainDBConnection.prepareStatement("select Password from ParentUsers where UserName = ?");
		statement.setString(1, userName);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			password = resultSet.getString("Password");
		}
		statement.close();
		return password;
	}

	private static void updateSSOParentTokens() {
		long now = System.currentTimeMillis();
		// Trigger update (at most) every 60 seconds
		if ((now - lastSSOParentTokensUpdate) >= SSOParentTokenUpdateFrequency) {
			lastSSOParentTokensUpdate = now;
			expireUnusedParentTokens(now);
			deleteExpiredParentTokens(now);
		}
	}

	private static void expireUnusedParentTokens(long now) {
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveParentTokens set LastRefresh = 0, Expired = 1, ExpiredSince = ?, ExpiredByLogin = 0 where Expired = 0 and LastRefresh < ?");
			statement.setLong(1, now);
			statement.setLong(2, now - SSOParentTokenDuration);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	private static void deleteExpiredParentTokens(long now) {
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("delete from ActiveParentTokens where Expired = 1 and ExpiredSince < ?");
			statement.setLong(1, now - SSOParentTokenDurationExpired); // delete expired tokens after one hour
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			throwables.printStackTrace();
		}
	}

	public static void invalidateExistingTokens(String userName) {
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

	public static boolean isSSOParentTokenUnique(String token) throws SQLException {
		PreparedStatement statement = mainDBConnection.prepareStatement("select * from ActiveParentTokens where Token = ?");
		statement.setString(1, token);
		ResultSet resultSet = statement.executeQuery();
		boolean result = !resultSet.next();
		statement.close();
		return result;
	}

	public static SSOTokenInfo getSSOParentTokenInfo(String token) throws SQLException {
		updateSSOParentTokens();

		PreparedStatement statement = mainDBConnection.prepareStatement("select Expired, ExpiredByLogin, UserName from ActiveParentTokens where Token = ?");
		statement.setString(1, token);
		ResultSet resultSet = statement.executeQuery();
		SSOTokenInfo result = null;
		if (resultSet.next()) {
			result = new SSOTokenInfo(token, resultSet.getBoolean(1), resultSet.getBoolean(2), resultSet.getString(3));
		}
		statement.close();
		return result;
	}

	public static void addSSOParentToken(String token, String userName) throws SQLException {
		updateSSOParentTokens();

		PreparedStatement statement = mainDBConnection.prepareStatement("insert into ActiveParentTokens (Token, UserName, LastRefresh, Expired, ExpiredSince, ExpiredByLogin) values(?, ?, ?, 0, 0, 0)");
		statement.setString(1, token);
		statement.setString(2, userName);
		statement.setLong(3, System.currentTimeMillis());
		statement.executeUpdate();
		statement.close();
	}

	public static boolean refreshSSOParentToken(String token) throws SQLException {
		updateSSOParentTokens();

		PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveParentTokens set LastRefresh = ? where Token = ? and Expired = 0");
		statement.setLong(1, System.currentTimeMillis());
		statement.setString(2, token);
		int updatedRows = statement.executeUpdate();
		statement.close();
		return updatedRows == 1;
	}
}
