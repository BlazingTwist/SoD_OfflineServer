package blazingtwist.database;

import generated.Gender;

public class ChildUserInfo {
	public String userName;
	public int gameCurrency;
	public int cashCurrency;
	public Gender gender;
	public boolean isMultiplayerEnabled;
	public String parentUserName;

	public ChildUserInfo(String userName, int gameCurrency, int cashCurrency, Gender gender, boolean isMultiplayerEnabled, String parentUserName) {
		this.userName = userName;
		this.gameCurrency = gameCurrency;
		this.cashCurrency = cashCurrency;
		this.gender = gender;
		this.isMultiplayerEnabled = isMultiplayerEnabled;
		this.parentUserName = parentUserName;
	}
}
