package blazingtwist.wswebservice.functions;

import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import generated.MessageInfo;
import java.util.Map;

public class GetUserMessageQueue extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetUserMessageQueue(String contextName) {
		super(contextName);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// used for sending system messages, such as being kicked from a clan
		// will be displayed in chat

		MessageInfo message = new MessageInfo();
		message.setUserMessageQueueID(1); // TODO unique ID, basically a counter for how many messages have been sent
		message.setFromUserID(null); // TODO relevant for friend requests?
		message.setMessageID(1); // TODO not used except for null-checks, so just make it non-null (also used for daily quests?)

		/*
		* Can't they just use fucking enums for this?!
		* 4 = Level-Up Messages | TypeName: Rank
		* 9 = Achievement Messages | TypeName: AchievementTask
		* 28 = Clan-Messages | TypeName: Group
		*/
		message.setMessageTypeID(4); // TODO

		// TODO
	}
}
