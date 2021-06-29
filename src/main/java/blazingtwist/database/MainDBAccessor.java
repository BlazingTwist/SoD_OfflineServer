package blazingtwist.database;

import blazingtwist.database.querydatatypes.tokeninfo.SSOChildTokenInfo;
import blazingtwist.database.querydatatypes.tokeninfo.SSOParentTokenInfo;
import blazingtwist.database.querydatatypes.tokeninfo.SSOTokenInfo;
import blazingtwist.database.querydatatypes.userinfo.ChildUserInfo;
import blazingtwist.database.querydatatypes.userinfo.ParentUserInfo;
import blazingtwist.logback.LogbackLoggerProvider;
import blazingtwist.config.sql.SQLConfig;
import blazingtwist.crypto.MD5;
import generated.BluePrint;
import generated.BluePrintSpecification;
import generated.Gender;
import generated.ItemData;
import generated.ItemDataAvatarModifier;
import generated.ItemDataCategory;
import generated.ItemDataDragonFood;
import generated.ItemDataDragonFoodModifier;
import generated.ItemDataRelationship;
import generated.ItemDataTexture;
import generated.ItemFishModifier;
import generated.ItemPossibleStatsMap;
import generated.ItemRarity;
import generated.ItemStatType;
import generated.ItemState;
import generated.ItemStateConsumable;
import generated.ItemStateExpiryInfo;
import generated.ItemStateSpeedUp;
import generated.ItemStoreStat;
import generated.ItemTier;
import generated.ItemsInStoreData;
import generated.RaisedPetStage;
import generated.STItemStatType;
import generated.Stat;
import generated.StatRangeMap;
import generated.StateTransition;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;

public class MainDBAccessor {
	private static final Logger logger = LogbackLoggerProvider.getLogger(MainDBAccessor.class);

	private static Connection mainDBConnection = null;
	private static long lastSSOParentTokensUpdate = 0;
	private static final long SSOTokenUpdateFrequency = 60_000L; // update token validity once every minute
	private static final long SSOTokenDuration = 300_000L; // expire tokens older than 5 minutes
	private static final long SSOTokenDurationExpired = 3_600_000L; // delete expired tokens older than 1 hour

	private static HashMap<Integer, ItemData> dbItemData = null;
	private static HashMap<Integer, ItemsInStoreData> dbStoreData = null;

	public static void initialize(SQLConfig sqlConfig) {
		try {
			mainDBConnection = DriverManager.getConnection("jdbc:sqlite:" + sqlConfig.getPath());
		} catch (SQLException throwables) {
			logger.error("Initialization of MainDBAccessor failed!", throwables);
		}
	}

	public static void disconnect() {
		if (mainDBConnection != null) {
			try {
				mainDBConnection.close();
			} catch (SQLException throwables) {
				logger.error("Failed to close mainDBConnection!", throwables);
			}
		}
	}

	public static String buildGUID(String text) {
		String hash = MD5.getUnicodeHashHex(text);
		return hash.substring(0, 8) + "-" + hash.substring(8, 12) + "-" + hash.substring(12, 16) + "-" + hash.substring(16, 20) + "-" + hash.substring(20);
	}

	public static void addParentUser(String userName, String password) throws SQLException {
		PreparedStatement statement = mainDBConnection.prepareStatement("insert into ParentUsers (UserName, UserID, Password) values(?, ?, ?)");
		statement.setString(1, userName);
		statement.setString(2, buildGUID(userName));
		statement.setString(3, password);
		statement.executeUpdate();
		statement.close();
	}

	/**
	 * @param key either ParentUserName or ParentUserID
	 */
	public static ParentUserInfo getParentUserInfo(String key) {
		ParentUserInfo userInfo = null;
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("select UserName, UserID, Password from ParentUsers where (UserName = ?) or (UserID = ?)");
			statement.setString(1, key);
			statement.setString(2, key);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				userInfo = new ParentUserInfo(
						resultSet.getString(1),
						resultSet.getString(2),
						resultSet.getString(3)
				);
			}
			statement.close();
		} catch (SQLException throwables) {
			logger.warn("Error during getParentUserInfo", throwables);
		}
		return userInfo;
	}

	public static List<ChildUserInfo> getChildUserInfoForParent(String parentName) throws SQLException {
		PreparedStatement statement = mainDBConnection.prepareStatement("select UserName, UserID, CashCurrency, GameCurrency, Gender, MultiplayerEnabled from ChildUsers where ParentUserName = ?");
		statement.setString(1, parentName);
		ResultSet resultSet = statement.executeQuery();
		List<ChildUserInfo> children = new ArrayList<>();
		while (resultSet.next()) {
			children.add(new ChildUserInfo(
					resultSet.getString(1),
					resultSet.getString(2),
					resultSet.getInt(4),
					resultSet.getInt(3),
					Gender.fromValue(resultSet.getInt(5)),
					resultSet.getBoolean(6),
					parentName
			));
		}
		return children;
	}

	public static ChildUserInfo getChildUserInfo(String key) {
		ChildUserInfo userInfo = null;
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("select UserName, UserID, CashCurrency, GameCurrency, Gender, MultiplayerEnabled, ParentUserName from ChildUsers where (UserName = ?) or (UserID = ?)");
			statement.setString(1, key);
			statement.setString(2, key);
			ResultSet resultSet = statement.executeQuery();
			if (resultSet.next()) {
				userInfo = new ChildUserInfo(
						resultSet.getString(1),
						resultSet.getString(2),
						resultSet.getInt(4), // CashCurrency
						resultSet.getInt(3), // GameCurrency
						Gender.fromValue(resultSet.getInt(5)), // GenderID
						resultSet.getBoolean(6), // MultiplayEnabled
						resultSet.getString(7) // ParentUserName
				);
			}
			statement.close();
		} catch (SQLException throwables) {
			logger.warn("Error during getChildUserInfo", throwables);
		}
		return userInfo;
	}

	private static void updateSSOTokens() {
		long now = System.currentTimeMillis();
		// Trigger update (at most) every 60 seconds
		if ((now - lastSSOParentTokensUpdate) >= SSOTokenUpdateFrequency) {
			lastSSOParentTokensUpdate = now;
			expireUnusedSSOTokens(now);
			deleteExpiredTokens(now);
		}
	}

	private static void expireUnusedSSOTokens(long now) {
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveParentTokens set LastRefresh = 0, Expired = 1, ExpiredSince = ?, ExpiredByLogin = 0 where Expired = 0 and LastRefresh < ?");
			statement.setLong(1, now);
			statement.setLong(2, now - SSOTokenDuration);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			logger.warn("Error during expireUnusedSSOTokens, while updating ParentTokens", throwables);
		}
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveChildTokens set LastRefresh = 0, Expired = 1, ExpiredSince = ?, ExpiredByLogin = 0 where Expired = 0 and LastRefresh < ?");
			statement.setLong(1, now);
			statement.setLong(2, now - SSOTokenDuration);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			logger.warn("Error during expireUnusedSSOTokens, while updating ChildTokens", throwables);
		}
	}

	private static void deleteExpiredTokens(long now) {
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("delete from ActiveParentTokens where Expired = 1 and ExpiredSince < ?");
			statement.setLong(1, now - SSOTokenDurationExpired); // delete expired tokens after one hour
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			logger.warn("Error during deleteExpiredTokens, while deleting ParentTokens", throwables);
		}
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("delete from ActiveChildTokens where Expired = 1 and ExpiredSince < ?");
			statement.setLong(1, now - SSOTokenDurationExpired); // delete expired tokens after one hour
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			logger.warn("Error during deleteExpiredTokens, while deleting ChildTokens", throwables);
		}
	}

	public static void invalidateExistingParentTokens(String userName) {
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveParentTokens set LastRefresh = 0, Expired = 1, ExpiredSince = ?, ExpiredByLogin = 1 where UserName = ? and Expired = 0");
			statement.setLong(1, System.currentTimeMillis());
			statement.setString(2, userName);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			logger.warn("Error during invalidateExistingParentTokens", throwables);
		}
	}

	public static void invalidateExistingChildTokens(String userName) {
		try {
			PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveChildTokens set LastRefresh = 0, Expired = 1, ExpiredSince = ?, ExpiredByLogin = 1 where UserName = ? and Expired = 0");
			statement.setLong(1, System.currentTimeMillis());
			statement.setString(2, userName);
			statement.executeUpdate();
			statement.close();
		} catch (SQLException throwables) {
			logger.warn("Error during invalidateExistingChildTokens", throwables);
		}
	}

	public static boolean isSSOTokenUnique(String token) throws SQLException {
		PreparedStatement statement = mainDBConnection.prepareStatement("select * from ActiveParentTokens where Token = ?");
		statement.setString(1, token);
		ResultSet resultSet = statement.executeQuery();
		boolean isUnique = !resultSet.next();
		statement.close();

		if (isUnique) {
			statement = mainDBConnection.prepareStatement("select * from ActiveChildTokens where Token = ?");
			statement.setString(1, token);
			resultSet = statement.executeQuery();
			isUnique = !resultSet.next();
			statement.close();
		}
		return isUnique;
	}

	public static SSOParentTokenInfo getSSOParentTokenInfo(String token) throws SQLException {
		updateSSOTokens();
		SSOParentTokenInfo parentTokenInfo = null;
		PreparedStatement statement = mainDBConnection.prepareStatement("select Expired, ExpiredByLogin, UserName from ActiveParentTokens where Token = ?");
		statement.setString(1, token);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			parentTokenInfo = new SSOParentTokenInfo(
					token,
					resultSet.getBoolean(1),
					resultSet.getBoolean(2),
					resultSet.getString(3)
			);
		}
		return parentTokenInfo;
	}

	public static SSOChildTokenInfo getSSOChildTokenInfo(String token) throws SQLException {
		updateSSOTokens();
		SSOChildTokenInfo childTokenInfo = null;
		PreparedStatement statement = mainDBConnection.prepareStatement("select Expired, ExpiredByLogin, UserName from ActiveChildTokens where Token = ?");
		statement.setString(1, token);
		ResultSet resultSet = statement.executeQuery();
		if (resultSet.next()) {
			childTokenInfo = new SSOChildTokenInfo(
					token,
					resultSet.getBoolean(1),
					resultSet.getBoolean(2),
					resultSet.getString(3)
			);
		}
		return childTokenInfo;
	}

	public static SSOTokenInfo getSSOTokenInfo(String token) throws SQLException {
		SSOTokenInfo tokenInfo = getSSOChildTokenInfo(token);
		if (tokenInfo != null) {
			return tokenInfo;
		}
		return getSSOParentTokenInfo(token);
	}

	public static void addSSOParentToken(String token, String userName) throws SQLException {
		updateSSOTokens();
		PreparedStatement statement = mainDBConnection.prepareStatement("insert into ActiveParentTokens (Token, UserName, LastRefresh, Expired, ExpiredSince, ExpiredByLogin) values(?, ?, ?, 0, 0, 0)");
		addSSOToken(token, userName, statement);
	}

	public static void addSSOChildToken(String token, String userName) throws SQLException {
		updateSSOTokens();
		PreparedStatement statement = mainDBConnection.prepareStatement("insert into ActiveChildTokens (Token, UserName, LastRefresh, Expired, ExpiredSince, ExpiredByLogin) values(?, ?, ?, 0, 0, 0)");
		addSSOToken(token, userName, statement);
	}

	public static void addSSOToken(String token, String userName, PreparedStatement statement) throws SQLException {
		statement.setString(1, token);
		statement.setString(2, userName);
		statement.setLong(3, System.currentTimeMillis());
		statement.executeUpdate();
		statement.close();
	}

	public static boolean refreshSSOParentToken(String token) throws SQLException {
		updateSSOTokens();

		PreparedStatement statement = mainDBConnection.prepareStatement("update ActiveParentTokens set LastRefresh = ? where Token = ? and Expired = 0");
		statement.setLong(1, System.currentTimeMillis());
		statement.setString(2, token);
		int updatedRows = statement.executeUpdate();
		statement.close();
		return updatedRows == 1;
	}

	private static ItemData readMainItemData(ResultSet resultSet, int itemID) throws SQLException {
		ItemData result = new ItemData();
		result.setItemID(itemID);
		int il_itemRarity = resultSet.getInt("il_ItemRarity");
		if (!resultSet.wasNull()) {
			result.setItemRarity(ItemRarity.fromValue(il_itemRarity));
		}
		result.setItemName(resultSet.getString("il_ItemName"));
		result.setItemNamePlural(resultSet.getString("il_ItemNamePlural"));
		result.setDescription(resultSet.getString("il_Description"));
		result.setCost(resultSet.getInt("il_Cost"));
		result.setCashCost(resultSet.getInt("il_CashCost"));
		result.setSaleFactor(resultSet.getInt("il_SaleFactor"));
		result.setSaleShardCount(resultSet.getInt("il_SaleShardCount"));
		int il_storeItemTier = resultSet.getInt("il_StoreItemTier");
		if (!resultSet.wasNull()) {
			result.setStoreItemTier(ItemTier.fromValue(il_storeItemTier));
		}
		result.setInventoryMax(resultSet.getInt("il_InventoryMax"));
		result.setIsParentItem(resultSet.getInt("il_IsParentItem") == 1);
		result.setUses(resultSet.getInt("il_Uses"));
		result.setCreativePoints(resultSet.getInt("il_CreativePoints"));
		result.setRewardTypeID(resultSet.getInt("il_RewardTypeID"));
		result.setPoints(resultSet.getInt("il_Points"));
		result.setRankID(resultSet.getInt("il_RankID"));
		result.setIconName(resultSet.getString("il_IconName"));
		result.setAssetName(resultSet.getString("il_AssetName"));
		result.setActionDB(resultSet.getString("il_ActionDB"));
		result.setActionDBImage(resultSet.getString("il_ActionDBImage"));
		result.setWeaponName(resultSet.getString("il_WeaponName"));
		result.setMovie(resultSet.getString("il_Movie"));
		result.setGeometry2(resultSet.getString("il_Geometry2"));
		result.setHasToggleWings(resultSet.getInt("il_HasToggleWings") == 1);
		result.setPartBone2(resultSet.getString("il_PartBone2"));
		result.setGender(Gender.fromValue(resultSet.getInt("il_Gender")));
		result.setPetTypeID(resultSet.getInt("il_PetTypeID"));
		String il_petStage = resultSet.getString("il_PetStage");
		if (!resultSet.wasNull()) {
			result.setPetStage(RaisedPetStage.fromValue(il_petStage));
		}
		result.setPetToyType(resultSet.getString("il_PetToyType"));
		result.setIsNew(resultSet.getInt("il_IsNew") == 1);
		result.setIs2D(resultSet.getInt("il_Is2D") == 1);
		result.setStorePreviewImage(resultSet.getString("il_StorePreviewImage"));
		result.setStorePreviewScale(resultSet.getString("il_StorePreviewScale"));
		result.setGlowColor(resultSet.getString("il_GlowColor"));
		result.setEffectDuration(resultSet.getFloat("il_EffectDuration"));
		result.setStableNestCount(resultSet.getInt("il_StableNestCount"));
		result.setHappinessDecreaseModifier(resultSet.getFloat("il_HappinessDecreaseModifier"));
		return result;
	}

	private static void addItemTexture(ResultSet resultSet, ItemData itemData) throws SQLException {
		String textureType = resultSet.getString("tex_TextureType");
		if (textureType == null) {
			return;
		}

		ItemDataTexture texture = new ItemDataTexture();
		texture.setTextureTypeName(textureType);
		texture.setTextureName(resultSet.getString("tex_TexturePath"));
		itemData.getTexture().add(texture);
	}

	private static void addItemRelationship(ResultSet resultSet, ItemData itemData) throws SQLException {
		String relationshipType = resultSet.getString("rs_RelationshipType");
		if (relationshipType == null) {
			return;
		}

		ItemDataRelationship relationship = new ItemDataRelationship();
		relationship.setType(relationshipType);
		relationship.setItemID(resultSet.getInt("rs_RelationshipItemID"));
		relationship.setQuantity(resultSet.getInt("rs_RelationshipQuantity"));
		relationship.setWeight(resultSet.getInt("rs_RelationshipWeight"));
		itemData.getRelationship().add(relationship);
	}

	private static boolean addItemDragonFood(ResultSet resultSet, ItemData itemData) throws SQLException {
		int energy = resultSet.getInt("df_Energy");
		if (resultSet.wasNull()) {
			return false;
		}

		ItemDataDragonFood dragonFood = new ItemDataDragonFood();
		itemData.setDragonFood(dragonFood);
		dragonFood.setEnergy(energy);
		dragonFood.setHappiness(resultSet.getInt("df_Happiness"));

		// gather modifier data that has been mapped to the same row first
		int modifierPetTypeID = resultSet.getInt("dfm_PetTypeID");
		if (!resultSet.wasNull()) {
			dragonFood.getModifiers().add(readItemDragonFoodModifier(resultSet, modifierPetTypeID));

			// then check for additional modifier data in the following rows
			while (resultSet.next()) {

				int modifierItemID = resultSet.getInt("si_ItemID");
				if (itemData.getItemID() != modifierItemID) {
					// advanced to the next item, signal that the main loop doesn't have to advance
					return true;
				}

				modifierPetTypeID = resultSet.getInt("dfm_PetTypeID");
				if (resultSet.wasNull()) {
					// reached end of modifier data, signal that the main loop doesn't have to advance
					return true;
				}

				// found modifier data
				dragonFood.getModifiers().add(readItemDragonFoodModifier(resultSet, modifierPetTypeID));
			}
		}
		return false;
	}

	private static ItemDataDragonFoodModifier readItemDragonFoodModifier(ResultSet resultSet, int modifierPetTypeID) throws SQLException {
		ItemDataDragonFoodModifier modifier = new ItemDataDragonFoodModifier();
		modifier.setPetTypeID(modifierPetTypeID);
		modifier.setEnergy(resultSet.getInt("dfm_Energy"));
		modifier.setHappiness(resultSet.getInt("dfm_Happiness"));
		return modifier;
	}

	private static void addItemFishModifier(ResultSet resultSet, ItemData itemData) throws SQLException {
		String modifierFishName = resultSet.getString("fm_FishName");
		if (modifierFishName == null) {
			return;
		}

		ItemFishModifier fishModifier = new ItemFishModifier();
		fishModifier.setFishName(modifierFishName);
		fishModifier.setSpawnChanceModifier(resultSet.getInt("fm_SpawnChanceModifier"));
		itemData.getFishModifiers().add(fishModifier);
	}

	private static void addItemCategory(ResultSet resultSet, ItemData itemData) throws SQLException {
		int categoryID = resultSet.getInt("cat_CategoryID");
		if (resultSet.wasNull()) {
			return;
		}

		ItemDataCategory category = new ItemDataCategory();
		category.setCategoryID(categoryID);
		category.setCategoryName(resultSet.getString("cat_CategoryName"));
		itemData.getCategory().add(category);
	}

	private static boolean addItemBlueprint(ResultSet resultSet, ItemData itemData) throws SQLException {
		int coinCost = resultSet.getInt("bp_CoinCost");
		if (resultSet.wasNull()) {
			return false;
		}

		BluePrint bluePrint = new BluePrint();
		itemData.setBluePrint(bluePrint);
		bluePrint.setCoinCost(coinCost);
		bluePrint.setShardCost(resultSet.getInt("bp_ShardCost"));
		bluePrint.setResultItemID(resultSet.getInt("bp_ResultItemID"));
		bluePrint.setResultItemTier(ItemTier.fromValue(resultSet.getInt("bp_ResultItemTier")));

		// gather ingredient that has been mapped to the same row
		int blueprintSpecID = resultSet.getInt("bpi_IngredientID");
		if (!resultSet.wasNull()) {
			bluePrint.getIngredients().add(readItemBlueprintIngredient(resultSet, blueprintSpecID));

			// then check for additional ingredients in the following rows
			while (resultSet.next()) {

				int blueprintItemID = resultSet.getInt("si_ItemID");
				if (itemData.getItemID() != blueprintItemID) {
					// advanced to the next item, signal that the main loop doesn't have to advance
					return true;
				}

				blueprintSpecID = resultSet.getInt("bpi_IngredientID");
				if (resultSet.wasNull()) {
					// reached end of ingredients, signal that the main loop doesn't have to advance
					return true;
				}

				// found ingredient
				bluePrint.getIngredients().add(readItemBlueprintIngredient(resultSet, blueprintSpecID));
			}
		}

		return false;
	}

	private static BluePrintSpecification readItemBlueprintIngredient(ResultSet resultSet, int blueprintSpecID) throws SQLException {
		BluePrintSpecification ingredient = new BluePrintSpecification();
		ingredient.setBluePrintSpecID(blueprintSpecID);
		ingredient.setItemID(resultSet.getInt("bpi_InItemID"));
		ingredient.setCategoryID(resultSet.getInt("bpi_InCategoryID"));
		ingredient.setItemRarity(ItemRarity.fromValue(resultSet.getInt("bpi_InItemRarity")));
		ingredient.setTier(ItemTier.fromValue(resultSet.getInt("bpi_InItemTier")));
		ingredient.setQuantity(resultSet.getInt("bpi_InQuantity"));
		return ingredient;
	}

	private static void addItemAvatarModifier(ResultSet resultSet, ItemData itemData) throws SQLException {
		String modifierType = resultSet.getString("am_AvatarModifierType");
		if (modifierType == null) {
			return;
		}

		ItemDataAvatarModifier modifier = new ItemDataAvatarModifier();
		modifier.setAvatarModifierType(modifierType);
		modifier.setModifierValue(resultSet.getFloat("am_ModifierValue"));
		itemData.getAvatarModifiers().add(modifier);
	}

	private static void addItemStoreStat(ResultSet resultSet, ItemData itemData) throws SQLException {
		int statTypeInt = resultSet.getInt("ss_StatType");
		if (resultSet.wasNull()) {
			return;
		}

		ItemStoreStat storeStat = new ItemStoreStat();
		storeStat.setStatType(ItemStatType.fromValue(statTypeInt));
		storeStat.setStatValue(resultSet.getInt("ss_StatValue"));
		itemData.getItemStoreStats().add(storeStat);
	}

	private static boolean addItemPossibleStat(ResultSet resultSet, ItemData itemData) throws SQLException {
		int statTypeInt = resultSet.getInt("ps_StatType");
		if (resultSet.wasNull()) {
			return false;
		}

		// get possibleStatsMap, create one of none is present
		ItemPossibleStatsMap possibleStatsMap = itemData.getPossibleStatsMap();
		if (possibleStatsMap == null) {
			possibleStatsMap = new ItemPossibleStatsMap();
			itemData.setPossibleStatsMap(possibleStatsMap);
		}

		Stat stat = new Stat();
		possibleStatsMap.getStats().add(stat);
		stat.setItemStatType(STItemStatType.fromValue(statTypeInt));

		// read statRangeMap that has been mapped to this row first
		StatRangeMap rangeMap = readItemStatRangeMap(resultSet);
		if (rangeMap != null) {
			stat.getItemStatsRangeMaps().add(rangeMap);

			// then check for additional statRangeMaps in the following rows
			while (resultSet.next()) {

				int statItemID = resultSet.getInt("si_ItemID");
				if (itemData.getItemID() != statItemID) {
					// advanced to the next item, signal that the main loop doesn't have to advance
					return true;
				}

				int curStatTypeInt = resultSet.getInt("ps_StatType");
				if (resultSet.wasNull()) {
					// reached end of possibleStats, signal that the main loop doesn't have to advance
					return true;
				}

				if (curStatTypeInt != statTypeInt) {
					// reached next statType (for same item), add new stat to statsMap
					stat = new Stat();
					possibleStatsMap.getStats().add(stat);
					stat.setItemStatType(STItemStatType.fromValue(curStatTypeInt));
					statTypeInt = curStatTypeInt;
				}

				rangeMap = readItemStatRangeMap(resultSet);
				if (rangeMap != null) {
					stat.getItemStatsRangeMaps().add(rangeMap);
				} else {
					logger.warn("StatsRangeMap for item {} was null unexpectedly! Database structure may have changed!" +
							"(server will continue uninterruped)", statItemID);
				}
			}
		}

		return false;
	}

	private static StatRangeMap readItemStatRangeMap(ResultSet resultSet) throws SQLException {
		int itemTierID = resultSet.getInt("ps_Tier");
		if (resultSet.wasNull()) {
			return null;
		}

		StatRangeMap rangeMap = new StatRangeMap();
		rangeMap.setItemTierID(itemTierID);
		rangeMap.setStartRange(resultSet.getInt("ps_StartRange"));
		rangeMap.setEndRange(resultSet.getInt("ps_EndRange"));
		return rangeMap;
	}

	private static void addItemFarmState(ResultSet resultSet, ItemData itemData) throws SQLException {
		int farmStateID = resultSet.getInt("fs_ItemStateID");
		if (resultSet.wasNull()) {
			return;
		}

		ItemState itemState = new ItemState();
		itemState.setItemStateID(farmStateID);
		itemState.setOrder(resultSet.getInt("fs_Order"));
		itemState.setStateCompletionTransition(StateTransition.fromValue(resultSet.getInt("fs_Transition")));
		itemState.setStateCompletionMinAge(resultSet.getInt("fs_MinAge"));

		int consumableItemID = resultSet.getInt("fs_ConsumableItemID");
		if (!resultSet.wasNull()) {
			ItemStateConsumable consumable = new ItemStateConsumable();
			consumable.setConsumableItemID(consumableItemID);
			consumable.setConsumableItemAmount(resultSet.getInt("fs_ConsumableAmount"));
			itemState.setConsumableItem(consumable);
		}

		int speedUpItemID = resultSet.getInt("fs_SpeedUpItemID");
		if (!resultSet.wasNull()) {
			ItemStateSpeedUp speedUp = new ItemStateSpeedUp();
			speedUp.setSpeedUpItemID(speedUpItemID);
			speedUp.setSpeedUpResultStateIndex(resultSet.getInt("fs_SpeedUpResultStateIndex"));
			itemState.setSpeedUpInfo(speedUp);
		}

		int expireDuration = resultSet.getInt("fs_ExpireDuration");
		if (!resultSet.wasNull()) {
			ItemStateExpiryInfo expiryInfo = new ItemStateExpiryInfo();
			expiryInfo.setExpireDuration(expireDuration);
			expiryInfo.setExpiredStateIndex(resultSet.getInt("fs_ExpiredStateIndex"));
			itemState.setExpireInfo(expiryInfo);
		}

		itemData.getItemStates().add(itemState);
	}

	private static void ensureItemDataLoaded() throws SQLException, IOException {
		if (dbItemData != null) {
			return;
		}

		InputStream queryStream = MainDBAccessor.class.getClassLoader().getResourceAsStream("sql/ItemData_Query.sql");
		if (queryStream == null) {
			throw new IOException("sql/ItemData_Query.sql couldn't be found.");
		}
		PreparedStatement statement = mainDBConnection.prepareStatement(
				new String(queryStream.readAllBytes(), StandardCharsets.UTF_8));
		ResultSet resultSet = statement.executeQuery();

		if (!resultSet.next()) {
			logger.error("Found no ItemData in Database!");
			return;
		}

		dbItemData = new HashMap<>();
		ItemData currentItemData = null;
		while (true) {
			int itemID = resultSet.getInt("si_ItemID");
			if (currentItemData == null || currentItemData.getItemID() != itemID) {
				currentItemData = readMainItemData(resultSet, itemID);
				dbItemData.put(itemID, currentItemData);
			}

			addItemTexture(resultSet, currentItemData);
			addItemRelationship(resultSet, currentItemData);
			if (addItemDragonFood(resultSet, currentItemData)) {
				continue;
			}
			addItemFishModifier(resultSet, currentItemData);
			addItemCategory(resultSet, currentItemData);
			if (addItemBlueprint(resultSet, currentItemData)) {
				continue;
			}
			addItemAvatarModifier(resultSet, currentItemData);
			addItemStoreStat(resultSet, currentItemData);
			if (addItemPossibleStat(resultSet, currentItemData)) {
				continue;
			}
			addItemFarmState(resultSet, currentItemData);

			if (!resultSet.next()) {
				break;
			}
		}
	}

	private static void addStoreItem(ResultSet resultSet, ItemsInStoreData storeData) throws SQLException {
		int itemID = resultSet.getInt("si_ItemID");
		if (resultSet.wasNull()) {
			return;
		}

		ItemData itemData = dbItemData.get(itemID);
		if (itemData != null) {
			storeData.getItems().add(itemData);
		} else {
			logger.error("dbItemData is missing ItemData for id: {}", itemID);
		}
	}

	private static void ensureStoreDataLoaded() throws SQLException, IOException {
		ensureItemDataLoaded();
		if (dbStoreData != null) {
			return;
		}

		InputStream queryStream = MainDBAccessor.class.getClassLoader().getResourceAsStream("sql/StoreData_Query.sql");
		if (queryStream == null) {
			throw new IOException("sql/StoreData_Query.sql couldn't be found.");
		}
		PreparedStatement statement = mainDBConnection.prepareStatement(
				new String(queryStream.readAllBytes(), StandardCharsets.UTF_8));
		ResultSet resultSet = statement.executeQuery();

		if (!resultSet.isBeforeFirst()) {
			logger.error("Found no StoreData in Database!");
			return;
		}

		dbStoreData = new HashMap<>();
		ItemsInStoreData currentStoreData = null;
		while (resultSet.next()) {
			int storeID = resultSet.getInt("sl_StoreID");
			if (currentStoreData == null || currentStoreData.getID() != storeID) {
				currentStoreData = new ItemsInStoreData();
				currentStoreData.setID(storeID);
				dbStoreData.put(storeID, currentStoreData);
			}

			addStoreItem(resultSet, currentStoreData);
		}
	}

	public static List<ItemsInStoreData> getStoreData(List<Integer> storeIDs) throws SQLException, IOException {
		ensureStoreDataLoaded();
		return storeIDs.stream()
				.filter(storeID -> dbStoreData.containsKey(storeID))
				.map(storeID -> dbStoreData.get(storeID))
				.collect(Collectors.toList());
	}
}
