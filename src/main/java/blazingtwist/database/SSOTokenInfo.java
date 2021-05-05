package blazingtwist.database;

public interface SSOTokenInfo {
	String getToken();
	boolean getExpired();
	boolean getExpiredByLogin();
	UserInfo getUserInfo();
}
