package blazingtwist.wswebservice.functions;

import blazingtwist.database.querydatatypes.userinfo.ChildUserInfo;
import blazingtwist.database.MainDBAccessor;
import blazingtwist.database.querydatatypes.userinfo.ParentUserInfo;
import blazingtwist.database.querydatatypes.tokeninfo.SSOParentTokenInfo;
import blazingtwist.wswebservice.WebDataUtils;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.AvatarData;
import generated.AvatarDataPart;
import generated.AvatarDataPartOffset;
import generated.AvatarDisplayData;
import generated.ListOfUserProfileData;
import generated.UserAchievementInfo;
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
		result.setID(dbInfo.userID);
		result.setMythieCount(0); // TODO presence is required but value unused
		result.setAchievementCount(0); // TODO presence is required but value unused
		result.setAnswerData(null);
		return result;
	}

	private static class AvatarDataPartBuilder {
		private AvatarDataPart dataPart = new AvatarDataPart();

		public AvatarDataPartBuilder(String partType) {
			dataPart.setPartType(partType);
		}

		public AvatarDataPartBuilder withOffset(float x, float y, float z) {
			AvatarDataPartOffset offset = new AvatarDataPartOffset();
			offset.setX(x);
			offset.setY(y);
			offset.setZ(z);
			dataPart.getOffsets().add(offset);
			return this;
		}

		public AvatarDataPartBuilder withGeometries(String... geometries) {
			for (String geometry : geometries) {
				dataPart.getGeometries().add(geometry);
			}
			return this;
		}

		public AvatarDataPartBuilder withTextures(String... textures) {
			for (String texture : textures) {
				dataPart.getTextures().add(texture);
			}
			return this;
		}

		public AvatarDataPart build(int uiid) {
			dataPart.setUserInventoryID(uiid);
			return dataPart;
		}

		public AvatarDataPart build() {
			return dataPart;
		}
	}

	private static AvatarData getAvatarData(ChildUserInfo dbInfo) {
		AvatarData avatarData = new AvatarData();
		avatarData.setIsSuggestedAvatarName(false);
		avatarData.setDisplayName(dbInfo.userName);
		avatarData.setGender(dbInfo.gender);
		avatarData.setIsHatVisible(true);
		avatarData.setIsWingVisible(false);
		avatarData.setLastEquippedFlightSuit(16192);

		// TODO generate part-list
		avatarData.getPart().add(new AvatarDataPartBuilder("Eyes").withGeometries("__EMPTY__").withTextures("__EMPTY__").build(-1));
		avatarData.getPart().add(new AvatarDataPartBuilder("Feet")
				.withGeometries("PfDWAvShoeFLArmorArcticLegendary.unity3d/PfDWAvShoeFLArmorArcticLegendary",
						"PfDWAvShoeFRArmorArcticLegendary.unity3d/PfDWAvShoeFRArmorArcticLegendary")
				.withTextures("DWAvatarShoeFLArmorArcticLegendary01.unity3d/DWAvShoesFArcticArmorLegendaryTex",
						"DWAvatarShoeFLArmorArcticLegendary01.unity3d/DWAvShoesFArcticArmorLegendaryTex")
				.build(782790392));
		avatarData.getPart().add(new AvatarDataPartBuilder("Hair")
				.withGeometries("PfDWAvHairFPonyLong.unity3d/PfDWAvHairFPonyLong")
				.withTextures("DWAvatarHairFPonyLong01.unity3d/DWAvHairFPonyLongTex",
						"DWAvatarHairFPonyLong01.unity3d/DWAvHairFPonyLong_MaskTex",
						"DWAvatarHairFPonyLong01.unity3d/DWAvHairFPonyLong_HighlightTex")
				.build());
		avatarData.getPart().add(new AvatarDataPartBuilder("Hand")
				.withGeometries("PfDWAvHandFLSkin.unity3d/PfDWAvHandFLSkin",
						"PfDWAvHandFRSkin.unity3d/PfDWAvHandFRSkin")
				.withTextures("DWAvatarHandFLSkin01.unity3d/DwAvatarHandFemaleDetailTex",
						"DWAvatarHandFLSkin01.unity3d/DwAvatarHandFemaleDetailTex")
				.build(-1));
		avatarData.getPart().add(new AvatarDataPartBuilder("Hat")
				.withGeometries("PfDWAvHatFPffyHrnPffyHlmt.unity3d/PfDWAvHatFPffyHrnPffyHlmt")
				.withTextures("DWAvatarHatFPffyHrnPffyHlmt01.unity3d/DWAvHatPffyHrnPffyHlmtArcticTex")
				.build(896674313));
		avatarData.getPart().add(new AvatarDataPartBuilder("Head")
				.withOffset(0.784313738f, 0.5411765f, 0.3764706f)
				.withOffset(0.2901961f, 0.105882354f, 0.05490196f)
				.withOffset(0.105882354f, 0.5882353f, 0.09019608f)
				.withOffset(0.7019608f, 0.101960786f, 0.101960786f)
				.withGeometries("PfDWAvHeadFBasic.unity3d/PfDWAvHeadFBasic")
				.withTextures("DWAvatarGirlFace.unity3d/AvatarGirlFaceTex",
						"DWAvatarGirlFace.unity3d/AvatarGirlFaceMask",
						"DWAvatarEyesF01.unity3d/DWAvEyesFNormalTex",
						"DWAvatarEyesF01.unity3d/DWAvEyesFNormal_MaskTex",
						"DWAvatarFacialDecals01.unity3d/Decal_Blank",
						"DWAvatarFacialWarPaints01.unity3d/Decal_Fill")
				.build());
		avatarData.getPart().add(new AvatarDataPartBuilder("Hidden")
				.withGeometries("__EMPTY__")
				.withTextures("__EMPTY__")
				.build());
		avatarData.getPart().add(new AvatarDataPartBuilder("Legs")
				.withGeometries("PfDWAvLegsFArmorArcticLegendary.unity3d/PfDWAvLegsFArmorArcticLegendary")
				.withTextures("DWAvatarLegsFArmorArcticLegendary01.unity3d/DWAvLegsFArmorArcticLegendaryTex")
				.build(784311356));
		avatarData.getPart().add(new AvatarDataPartBuilder("Mouth")
				.withGeometries("__EMPTY__")
				.withTextures("DWAvatarHeadFBasic01.unity3d/DWAvHeadFBasicTex",
						"DWAvatarHeadFBasic01.unity3d/DWAvHeadFBasicTex")
				.build());
		avatarData.getPart().add(new AvatarDataPartBuilder("Shield")
				.withGeometries("pfdwshieldbonestormerlegendary/PfDWShieldBonestormerLegendary")
				.withTextures("__EMPTY__")
				.build(876813955));
		avatarData.getPart().add(new AvatarDataPartBuilder("ShoulderPad")
				.withGeometries("PfDWAvShoulderLGroncicle.unity3d/PfDWAvShoulderLGroncicle",
						"PfDWAvShoulderRGroncicle.unity3d/PfDWAvShoulderRGroncicle")
				.withTextures("DWAvatarShoulderGroncicle01.unity3d/DWAvShoulderGroncicleTex",
						"DWAvatarShoulderGroncicle01.unity3d/DWAvShoulderGroncicleTex")
				.build(740462368));
		avatarData.getPart().add(new AvatarDataPartBuilder("Skin")
				.withGeometries("__EMPTY__")
				.withTextures("DWAvatarHeadFBasic01.unity3d/DWAvHeadFBasicTex")
				.build());
		avatarData.getPart().add(new AvatarDataPartBuilder("Torso")
				.withGeometries("PfDWAvTorsoFArmorArcticLegendary.unity3d/PfDWAvTorsoFArmorArcticLegendary")
				.withTextures("DWAvatarTorsoFArmorArcticLegendary01.unity3d/DWAvTorsoFArcticArmorLegendaryTex")
				.build(802279705));
		avatarData.getPart().add(new AvatarDataPartBuilder("Version")
				.withOffset(6f, 1f, 0f)
				.build());
		avatarData.getPart().add(new AvatarDataPartBuilder("Weapon")
				.withGeometries("pfdwcrossbowbonestormer/PfDWCrossbowBonestormer")
				.withTextures("__EMPTY__")
				.build(876811703));
		avatarData.getPart().add(new AvatarDataPartBuilder("Wing")
				.withGeometries("NULL")
				.withTextures("__EMPTY__")
				.build(896677911));
		avatarData.getPart().add(new AvatarDataPartBuilder("WristBand")
				.withGeometries("PfDWAvWristbandFLGroncicle.unity3d/PfDWAvWristbandFLGroncicle",
						"PfDWAvWristbandFRGroncicle.unity3d/PfDWAvWristbandFRGroncicle")
				.withTextures("DWAvatarWristbandFLGroncicle01.unity3d/DWAvWristbandFGroncicleTex",
						"DWAvatarWristbandFLGroncicle01.unity3d/DWAvWristbandFGroncicleTex")
				.build(740462370));

		return avatarData;
	}

	private static UserInfo getUserInfo(ParentUserInfo parentUserInfo, ChildUserInfo dbInfo) {
		UserInfo result = new UserInfo();
		result.setUserID(dbInfo.userID);
		result.setParentUserID(parentUserInfo.userID);
		result.setUsername(dbInfo.userName);
		result.setOpenChatEnabled(true);
		result.setGenderID(dbInfo.gender);
		result.setBirthDate(null); // null for child accounts
		result.setAge(null); // null for child accounts
		result.setCreationDate(null); // TODO do we care about this?
		result.setMultiplayerEnabled(dbInfo.isMultiplayerEnabled);
		return result;
	}

	private static AvatarDisplayData getAvatarDisplayData(ParentUserInfo parentUserInfo, ChildUserInfo dbInfo) {
		// TODO Achievements and RewardMultipliers
		AvatarDisplayData result = new AvatarDisplayData();
		result.setAvatarData(getAvatarData(dbInfo));
		result.setUserSubscriptionInfo(WebDataUtils.getUserSubscriptionInfo());
		result.setUserInfo(getUserInfo(parentUserInfo, dbInfo));

		// TODO Achievements refer to rank data (such as AvatarXP / UDT_XP / FishingXP / FarmingXP / Trophies)
		UserAchievementInfo udtRank = new UserAchievementInfo();
		udtRank.setUserID(dbInfo.userID);
		udtRank.setAchievementPointTotal(400000);
		udtRank.setRankID(5);
		udtRank.setPointTypeID(12);
		result.getAchievements().add(udtRank);

		UserAchievementInfo xpRank = new UserAchievementInfo();
		xpRank.setUserID(dbInfo.userID);
		xpRank.setAchievementPointTotal(45600);
		xpRank.setRankID(50);
		xpRank.setPointTypeID(1);
		result.getAchievements().add(xpRank);

		UserAchievementInfo fishingRank = new UserAchievementInfo();
		fishingRank.setUserID(dbInfo.userID);
		fishingRank.setAchievementPointTotal(24600);
		fishingRank.setRankID(30);
		fishingRank.setPointTypeID(10);
		result.getAchievements().add(fishingRank);

		UserAchievementInfo farmingRank = new UserAchievementInfo();
		farmingRank.setUserID(dbInfo.userID);
		farmingRank.setAchievementPointTotal(100000);
		farmingRank.setRankID(30);
		farmingRank.setPointTypeID(9);
		result.getAchievements().add(farmingRank);

		UserAchievementInfo trophies = new UserAchievementInfo();
		trophies.setUserID(dbInfo.userID);
		trophies.setAchievementPointTotal(5302);
		trophies.setRankID(0);
		trophies.setPointTypeID(11);
		result.getAchievements().add(trophies);

		return result;
	}

	private static UserProfileData getUserProfileData(ParentUserInfo parentUserInfo, ChildUserInfo dbInfo) {
		UserProfileData result = createBaseProfileData(dbInfo);
		result.setCashCurrency(dbInfo.cashCurrency);
		result.setGameCurrency(dbInfo.gameCurrency);
		result.setAvatarInfo(getAvatarDisplayData(parentUserInfo, dbInfo));
		return result;
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if (!WebFunctionUtils.checkKeysPresent(body, PARAM_API_KEY, PARAM_PARENT_API_TOKEN)) {
			respond(exchange, 400, INVALID_BODY);
			return;
		}

		SSOParentTokenInfo parentTokenInfo;
		try {
			parentTokenInfo = MainDBAccessor.getSSOParentTokenInfo(body.get(PARAM_PARENT_API_TOKEN));
		} catch (SQLException throwables) {
			logger.error("getSSOParentTokenInfo threw exception", throwables);
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		ListOfUserProfileData childList = new ListOfUserProfileData();
		if (parentTokenInfo == null || parentTokenInfo.isExpired || parentTokenInfo.parentUserInfo == null) {
			respondXml(exchange, 200, childList, "ArrayOfUserProfileDisplayData", false);
			return;
		}

		List<ChildUserInfo> dbChildren;
		try {
			dbChildren = MainDBAccessor.getChildUserInfoForParent(parentTokenInfo.parentUserInfo.userName);
		} catch (SQLException throwables) {
			logger.error("getChildUserInfoForParent threw exception", throwables);
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		logger.trace("checking child list...");
		for (ChildUserInfo dbChild : dbChildren) {
			logger.trace("found child: {}", dbChild.userName);
			childList.getUserProfileDisplayData().add(getUserProfileData(parentTokenInfo.parentUserInfo, dbChild));
		}

		respondXml(exchange, 200, childList, "ArrayOfUserProfileDisplayData", false);
	}
}
