package blazingtwist.wswebservice;

import blazingtwist.logback.LogbackLoggerProvider;
import generated.SubscriptionInfo;
import generated.UserSubscriptionInfo;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.slf4j.Logger;

public class WebDataUtils {
	private static final Logger logger = LogbackLoggerProvider.getLogger(WebDataUtils.class);

	private static XMLGregorianCalendar getNextYearDate() {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(Date.from(Instant.now().plus(365, ChronoUnit.DAYS)));
		try {
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			logger.error("Error during getNextYearDate", e);
			return null;
		}
	}

	// TODO remove this data from the client so we don't have to generate and send it
	public static SubscriptionInfo getSubscriptionInfo() {
		SubscriptionInfo result = new SubscriptionInfo();
		result.setRecurring(true); // recurring subscriptions aren't checked for expiration
		result.setStatus("Member"); // only used for checking Trial-Users ('Trial')
		result.setSubscriptionTypeID(1);
		result.setSubscriptionEndDate(getNextYearDate());
		return result;
	}

	// TODO remove this data from the client so we don't have to generate and send it
	public static UserSubscriptionInfo getUserSubscriptionInfo() {
		UserSubscriptionInfo result = new UserSubscriptionInfo();
		result.setRecurring(true); // recurring subscriptions aren't checked for expiration
		result.setStatus("Member"); // only used for checking Trial-Users ('Trial')
		result.setSubscriptionTypeID(1);
		result.setSubscriptionEndDate(getNextYearDate());
		return result;
	}

	public static XMLGregorianCalendar getXmlCalendar(Instant instant){
		try{
			GregorianCalendar calendar = new GregorianCalendar();
			calendar.setTime(Date.from(instant));
			return DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		} catch (DatatypeConfigurationException e) {
			logger.error("Error during getXmlCalendar", e);
			return null;
		}
	}
}
