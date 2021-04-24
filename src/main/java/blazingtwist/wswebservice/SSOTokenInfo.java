package blazingtwist.wswebservice;

public class SSOTokenInfo {
	public String token;
	public boolean isExpired;
	public boolean isExpiredByLogin;
	public String userName;

	public SSOTokenInfo(String token, boolean isExpired, boolean isExpiredByLogin, String userName) {
		this.token = token;
		this.isExpired = isExpired;
		this.isExpiredByLogin = isExpiredByLogin;
		this.userName = userName;
	}
}
