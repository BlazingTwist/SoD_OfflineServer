### Ignore-able Webfunctions:
* GetInventoryData
* SetInventoryData
* GetProductData
* SetProductData
* GetImageByProductGroup
* SetImagesData
* GetChildList
* GetConfigurationSetting
* GetContentByType
* GetValidatedUserID
* CreateAvatarContestEntry
* GetRankByUserID
* GetRankByUserIDs
* GetUserAchievementInfo
* SetUserAchievement
* GetTopAchievementPointUsers
* GetTopAchievementPointBuddies
* GetTopAchievementPointUsersByType
* GetTopAchievementPointBuddiesByType
* GetAchievementRewardsByAchievementTaskID
* SendMessage
* QueueMessage
* PostNewMessageToList
* GetItemsInStoreData
* IsValidToken
* GetXsollaToken
* CreatePurchaseOrderRequest
* SetHighScore
* GetDisplayNames
* GetDisplayNamesByCategory
* GetGameCurrency
* GetGameCurrencyByUserID
* CollectUserBonus
* GetItems
* GetGameDataByGameForDayRange
* GetCumulativePeriodicGameDataByUsers
* DeleteKeyValuePairByKeys
* GetAssetVersion
* GetAllPlatformAssetVersions
* GetAssetVersions
* GetAllAssetVersionsByUser
* GetNickname
* AddOneWayBuddy
* GetNeighborsByUserID
* SetNeighbor
* GetPartyByUserID
* GetPartiesByUserID
* GetActiveParties
* PurchaseParty
* GetUserTimedItems
* SetUserTimedItem
* GetUserStaff
* SetUserStaff
* GetStreamPost
* StreamPostLog
* GetActiveRaisedPetsByTypes
* GetSelectedRaisedPetByType
* GetUnselectedPetsByTypes
* CreateRaisedPet
* SubmitRating
* GetAllRatings
* ClearRating
* GetTopRank
* GetRank
* GetRatingForRatedEntity
* SubmitScore
* ClearScore
* GetTopScore
* GetTracksByUserID
* GetTracksByTrackIDs
* GetTrackElements
* SetTrack
* DeleteTrack
* RemoveUserWithoutTrack
* SetAchievementTaskByUserID
* GetLocaleData
* GetLocaleData
* GetGamePlayDataForDateRange
* GetUserAnswers
* GetConversationByMessageID
* GetMessageBoard
* GetStatusConversation
* AcceptTrade
* GetGameProgress
* GetGameProgressByUserID
* SetGameProgress
* LogOffParent
* RegisterAppInstall
* GetUsersWithPet
* CheckProductVersion
* CheckProductVersionV2
* GetProfileTagAll
* SendFriendInviteRegister
* PurchaseGift
* SetSpeedUpItem
* GetGroupsByUserID
* GetGroupsByGroupType
* ValidatePrizeCode

### Used but planned to remove:
* ProcessSteam
* RedeemReceipt
* SetRaisedPetInactive (TODO: still called from console)
* SubmitPrizeCode

### Other
* RegisterGuest (uses same end-point as RegisterParent)


### client-side TODOs
* implement new ItemData.ItemStats
* replace ItemData.PossibleStats.ItemStatsID with enum value, rename to ItemStatType