package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.ArrayOfUserAchievementInfo;
import generated.UserAchievementInfo;
import java.util.Map;

public class GetPetAchievementsByUserID extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetPetAchievementsByUserID(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// TODO why is this a thing an not attached to the pet data?!

		ArrayOfUserAchievementInfo infoArray = new ArrayOfUserAchievementInfo();
		{
			UserAchievementInfo info = new UserAchievementInfo();
			info.setUserID("7ea5788d-f4fe-4516-a382-6f318660d8f8"); // pet entity id
			info.setAchievementPointTotal(53600);
			info.setRankID(50);
			info.setPointTypeID(8);
			infoArray.getUserAchievementInfo().add(info);
		}
		{
			UserAchievementInfo info = new UserAchievementInfo();
			info.setUserID("7ea5788d-f4fe-4516-a382-6f318660d8f8"); // pet entity id
			info.setAchievementPointTotal(0);
			info.setRankID(1);
			info.setPointTypeID(1);
			infoArray.getUserAchievementInfo().add(info);
		}
		respondXml(exchange, 200, infoArray, "ArrayOfUserAchievementInfo", false);
	}
}
