package blazingtwist.database;

import generated.Gender;

public class ChildUserInfo implements UserInfo {
	public String userName;
	public String userID;
	public int gameCurrency;
	public int cashCurrency;
	public Gender gender;
	public boolean isMultiplayerEnabled;
	public ParentUserInfo parentUserInfo;

	public ChildUserInfo(String userName, String userID, int gameCurrency, int cashCurrency, Gender gender, boolean isMultiplayerEnabled, String parentUserName) {
		this.userName = userName;
		this.userID = userID;
		this.gameCurrency = gameCurrency;
		this.cashCurrency = cashCurrency;
		this.gender = gender;
		this.isMultiplayerEnabled = isMultiplayerEnabled;
		this.parentUserInfo = MainDBAccessor.getParentUserInfo(parentUserName);
	}

	@Override
	public String getUserName() {
		return userName;
	}

	@Override
	public String getUserID() {
		return userID;
	}

	@Override
	public ParentUserInfo getParentUserInfo() {
		return parentUserInfo;
	}
}
