package blazingtwist.database.querydatatypes.tokeninfo;

import blazingtwist.database.querydatatypes.userinfo.UserInfo;

public interface SSOTokenInfo {
	String getToken();
	boolean getExpired();
	boolean getExpiredByLogin();
	UserInfo getUserInfo();
}
