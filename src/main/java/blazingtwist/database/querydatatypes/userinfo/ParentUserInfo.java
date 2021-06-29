package blazingtwist.database.querydatatypes.userinfo;

public class ParentUserInfo implements UserInfo {
	public String userName;
	public String userID;
	public String password;

	public ParentUserInfo(String userName, String userID, String password) {
		this.userName = userName;
		this.userID = userID;
		this.password = password;
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
		return this;
	}

	public boolean isSameParent(ParentUserInfo other){
		return this.userName.equals(other.userName) && this.userID.equals(other.userID);
	}
}
