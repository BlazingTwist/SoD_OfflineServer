package blazingtwist.wswebservice.functions;

import blazingtwist.sod.AvatarData;
import blazingtwist.sod.AvatarDataPart;
import blazingtwist.sod.AvatarDisplayData;
import blazingtwist.sod.Gender;
import blazingtwist.wswebservice.WebServiceFunction;
import blazingtwist.wswebservice.WebServiceFunctionConstructor;
import com.sun.net.httpserver.HttpExchange;
import java.io.StringWriter;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

public class GetAvatarDisplayDataByUserID extends WebServiceFunction {
	@WebServiceFunctionConstructor
	public GetAvatarDisplayDataByUserID(String contextName) {
		super(contextName);
	}

	private JAXBElement<AvatarDisplayData> buildResponse(){
		AvatarData avatarData = new AvatarData();
		avatarData.setId(72199228);
		avatarData.setDisplayName("BlazingTwist");
		avatarData.setGender(Gender.unknown);
		AvatarDataPart avatarDataPart = new AvatarDataPart();
		avatarDataPart.setPartType("WristBand");

		avatarDataPart.getGeometries().add("PfDWAvWristbandFLGroncicle.unity3d/PfDWAvWristbandFLGroncicle");
		avatarDataPart.getGeometries().add("PfDWAvWristbandFRGroncicle.unity3d/PfDWAvWristbandFRGroncicle");

		avatarDataPart.getTextures().add("DWAvatarWristbandFLGroncicle01.unity3d/DWAvWristbandFGroncicleTex");
		avatarDataPart.getTextures().add("DWAvatarWristbandFLGroncicle01.unity3d/DWAvWristbandFGroncicleTex");

		avatarDataPart.setUserInventoryID(740462370);
		avatarData.getPart().add(avatarDataPart);
		avatarData.setUserNameToDisplayName(null);
		avatarData.setIsSuggestedAvatarName(null);

		AvatarDisplayData avatarDisplayData = new AvatarDisplayData();
		avatarDisplayData.setAvatarData(avatarData);

		return new JAXBElement<>(
				QName.valueOf("AvatarDisplayData"),
				AvatarDisplayData.class,
				avatarDisplayData);
	}

	@Override
	public void handle(HttpExchange exchange, Map<String, String> params, Map<String, String> body) {
		// TODO

		try {
			JAXBContext contextObject = JAXBContext.newInstance(AvatarDisplayData.class);
			Marshaller marshaller = contextObject.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

			StringWriter writer = new StringWriter();
			marshaller.marshal(buildResponse(), writer);
			super.respond(exchange, 200, writer.toString());
		} catch (JAXBException e) {
			e.printStackTrace();
			super.respond(exchange, 500, "Exception while handling the request.");
		}

	}
}
