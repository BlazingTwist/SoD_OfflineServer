package blazingtwist.wswebservice.functions;

import blazingtwist.database.ChildUserInfo;
import blazingtwist.database.MainDBAccessor;
import blazingtwist.database.SSOTokenInfo;
import blazingtwist.wswebservice.WebDataUtils;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.AvatarData;
import generated.AvatarDisplayData;
import generated.ListOfUserProfileData;
import generated.UserInfo;
import generated.UserProfileData;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class GetDetailedChildList extends WebServiceFunction {
	public static final String PARAM_API_KEY = "apiKey";
	public static final String PARAM_PARENT_API_TOKEN = "parentApiToken";

	@WebServiceFunctionConstructor
	public GetDetailedChildList(String contextName) {
		super(contextName);
	}

	private static UserProfileData createBaseProfileData(ChildUserInfo dbInfo) {
		UserProfileData result = new UserProfileData();
		result.setID(dbInfo.userName);
		result.setMythieCount(0); // TODO presence is required but value unused
		result.setAchievementCount(0); // TODO presence is required but value unused
		result.setAnswerData(null);
		return result;
	}

	private static AvatarData getAvatarData(ChildUserInfo dbInfo) {
		AvatarData avatarData = new AvatarData();
		avatarData.setIsSuggestedAvatarName(false);
		avatarData.setDisplayName(dbInfo.userName);
		avatarData.setGender(dbInfo.gender);
		// TODO generate part-list
		return avatarData;
	}

	private static UserInfo getUserInfo(String parentUserName, ChildUserInfo dbInfo) {
		UserInfo result = new UserInfo();
		result.setUserID(dbInfo.userName);
		result.setParentUserID(parentUserName);
		result.setUsername(dbInfo.userName);
		result.setOpenChatEnabled(true);
		result.setGenderID(dbInfo.gender);
		result.setBirthDate(null); // null for child accounts
		result.setAge(null); // null for child accounts
		result.setCreationDate(null); // TODO do we care about this?
		result.setMultiplayerEnabled(dbInfo.isMultiplayerEnabled);
		return result;
	}

	private static AvatarDisplayData getAvatarDisplayData(String parentUserName, ChildUserInfo dbInfo) {
		// TODO Achievements and RewardMultipliers
		AvatarDisplayData result = new AvatarDisplayData();
		result.setAvatarData(getAvatarData(dbInfo));
		result.setUserSubscriptionInfo(WebDataUtils.getUserSubscriptionInfo());
		result.setUserInfo(getUserInfo(parentUserName, dbInfo));
		return result;
	}

	private static UserProfileData getUserProfileData(String parentUserName, ChildUserInfo dbInfo) {
		UserProfileData result = createBaseProfileData(dbInfo);
		result.setCashCurrency(dbInfo.cashCurrency);
		result.setGameCurrency(dbInfo.gameCurrency);
		result.setAvatarInfo(getAvatarDisplayData(parentUserName, dbInfo));
		return result;
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if (!WebFunctionUtils.checkKeysPresent(body, PARAM_API_KEY, PARAM_PARENT_API_TOKEN)) {
			respond(exchange, 400, INVALID_BODY);
			return;
		}

		SSOTokenInfo parentTokenInfo;
		try {
			parentTokenInfo = MainDBAccessor.getSSOParentTokenInfo(body.get(PARAM_PARENT_API_TOKEN));
		} catch (SQLException throwables) {
			throwables.printStackTrace();
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		ListOfUserProfileData childList = new ListOfUserProfileData();
		if (parentTokenInfo == null || parentTokenInfo.isExpired || parentTokenInfo.userName == null) {
			respondXml(exchange, 200, childList, "ArrayOfUserProfileDisplayData", false);
			return;
		}

		List<ChildUserInfo> dbChildren;
		try {
			dbChildren = MainDBAccessor.getChildUserInfoForParent(parentTokenInfo.userName);
		} catch (SQLException throwables) {
			throwables.printStackTrace();
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		System.out.println("checking child list...");
		for (ChildUserInfo dbChild : dbChildren) {
			System.out.println("found child: " + dbChild.userName);
			childList.getUserProfileDisplayData().add(getUserProfileData(parentTokenInfo.userName, dbChild));
		}

		respondXml(exchange, 200, childList, "ArrayOfUserProfileDisplayData", false);
	}
}
