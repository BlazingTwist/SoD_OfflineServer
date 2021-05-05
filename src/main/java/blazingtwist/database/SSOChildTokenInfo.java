package blazingtwist.database;

public class SSOChildTokenInfo implements SSOTokenInfo {
	public String token;
	public boolean isExpired;
	public boolean isExpiredByLogin;
	public ChildUserInfo childUserInfo;

	/**
	 * @param childUserKey either childUserName or childUserID
	 */
	public SSOChildTokenInfo(String token, boolean isExpired, boolean isExpiredByLogin, String childUserKey) {
		this.token = token;
		this.isExpired = isExpired;
		this.isExpiredByLogin = isExpiredByLogin;
		this.childUserInfo = MainDBAccessor.getChildUserInfo(childUserKey);
	}

	@Override
	public String getToken() {
		return token;
	}

	@Override
	public boolean getExpired() {
		return isExpired;
	}

	@Override
	public boolean getExpiredByLogin() {
		return isExpiredByLogin;
	}

	@Override
	public UserInfo getUserInfo() {
		return childUserInfo;
	}
}
