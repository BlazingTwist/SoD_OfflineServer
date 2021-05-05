package blazingtwist.database;

public class SSOParentTokenInfo implements SSOTokenInfo {
	public String token;
	public boolean isExpired;
	public boolean isExpiredByLogin;
	public ParentUserInfo parentUserInfo;

	/**
	 * @param parentUserKey either parentUserName or parentUserID
	 */
	public SSOParentTokenInfo(String token, boolean isExpired, boolean isExpiredByLogin, String parentUserKey) {
		this.token = token;
		this.isExpired = isExpired;
		this.isExpiredByLogin = isExpiredByLogin;
		this.parentUserInfo = MainDBAccessor.getParentUserInfo(parentUserKey);
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
		return parentUserInfo;
	}
}
