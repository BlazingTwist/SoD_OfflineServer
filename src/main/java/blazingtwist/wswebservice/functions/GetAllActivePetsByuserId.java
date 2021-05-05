package blazingtwist.wswebservice.functions;

import blazingtwist.database.ChildUserInfo;
import blazingtwist.database.MainDBAccessor;
import blazingtwist.database.ParentUserInfo;
import blazingtwist.database.SSOTokenInfo;
import blazingtwist.wswebservice.WebDataUtils;
import blazingtwist.wswebservice.WebFunctionUtils;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.Gender;
import generated.ListOfRaisedPetData;
import generated.RaisedPetAttribute;
import generated.RaisedPetColor;
import generated.RaisedPetData;
import generated.RaisedPetGrowthState;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

public class GetAllActivePetsByuserId extends WebServiceFunction {
	public static final String PARAM_API_KEY = "apiKey";
	public static final String PARAM_API_TOKEN = "apiToken";
	public static final String PARAM_USER_ID = "userId";

	@WebServiceFunctionConstructor
	public GetAllActivePetsByuserId(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		if(!WebFunctionUtils.checkKeysPresent(body, PARAM_API_KEY, PARAM_API_TOKEN, PARAM_USER_ID)){
			respond(exchange, 400, INVALID_BODY);
			return;
		}

		SSOTokenInfo tokenInfo;
		try {
			tokenInfo = MainDBAccessor.getSSOTokenInfo(body.get(PARAM_API_TOKEN));
		} catch (SQLException throwables) {
			throwables.printStackTrace();
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		if(tokenInfo == null || tokenInfo.getExpired()){
			respond(exchange, 401, "Invalid token!");
			return;
		}

		ChildUserInfo userInfo = MainDBAccessor.getChildUserInfo(body.get(PARAM_USER_ID));
		if(userInfo == null){
			respond(exchange, 400, "UserID not found!");
			return;
		}

		ParentUserInfo tokenParentUserInfo = tokenInfo.getUserInfo().getParentUserInfo();
		ParentUserInfo childParentUserInfo = userInfo.parentUserInfo;
		if(!tokenParentUserInfo.isSameParent(childParentUserInfo)){
			System.err.println("Token user: " + tokenParentUserInfo.userName + " called method for child of parent: " + childParentUserInfo.userName);
			respond(exchange, 500, INTERNAL_ERROR);
			return;
		}

		/*
		* Notes: RaisedPetData
		*   IsPetCreated - unused
		*   validationmessage - unused
		*   IsReleased - unused
		*
		*   RaisedPetID - TODO
		*   EntityID - TODO
		*   UserID - TODO
		*   Name - TODO
		*   PetTypeID - TODO
		*   GrowthState - TODO
		*   ImagePosition - TODO
		*   Geometry - TODO
		*   Texture - TODO
		*   Gender - TODO
		*   Accessories - TODO
		*   Attributes - TODO
		*   Colors - TODO
		*   Skills - TODO
		*   States - TODO
		*   IsSelected - TODO
		*   CreateDate - TODO
		* */

		ListOfRaisedPetData petList = new ListOfRaisedPetData();

		// TODO actually query this from DB
		RaisedPetData pet = new RaisedPetData();
		pet.setRaisedPetID(65647148);
		pet.setEntityID("7ea5788d-f4fe-4516-a382-6f318660d8f8");
		pet.setUserID(userInfo.userID);
		pet.setName("TestPetName");
		pet.setPetTypeID(58);
		RaisedPetGrowthState growthState = new RaisedPetGrowthState();
		growthState.setName("Adult");
		pet.setGrowthState(growthState);
		pet.setImagePosition(0);
		pet.setGeometry("RS_SHARED/DWPrickleboggleDO.unity3d/PfDWPrickleboggle");
		pet.setTexture(null);
		pet.setGender(Gender.male);
		{
			RaisedPetAttribute attribute = new RaisedPetAttribute();
			attribute.setKey("PetStage");
			attribute.setValue("5");
			pet.getAttributes().add(attribute);
		}
		{
			RaisedPetColor color = new RaisedPetColor();
			color.setOrder(0);
			color.setRed(0.160784319f);
			color.setGreen(0.333333343f);
			color.setBlue(0.160784319f);
			pet.getColors().add(color);
		}
		{
			RaisedPetColor color = new RaisedPetColor();
			color.setOrder(1);
			color.setRed(0.5137255f);
			color.setGreen(0.219607845f);
			color.setBlue(0.09411765f);
			pet.getColors().add(color);
		}
		{
			RaisedPetColor color = new RaisedPetColor();
			color.setOrder(2);
			color.setRed(0.596078455f);
			color.setGreen(0.5137255f);
			color.setBlue(0.305882365f);
			pet.getColors().add(color);
		}
		pet.setIsSelected(true);
		pet.setCreateDate(WebDataUtils.getXmlCalendar(Instant.now()));

		petList.getRaisedPetData().add(pet);
		respondXml(exchange, 200, petList, "ArrayOfRaisedPetData", false);
	}
}
