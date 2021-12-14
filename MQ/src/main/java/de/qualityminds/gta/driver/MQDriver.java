package de.qualityminds.gta.driver;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import javax.jms.*;

import com.ibm.msg.client.jms.JmsConstants;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.jms.JmsQueueConnectionFactory;
import com.ibm.msg.client.wmq.common.CommonConstants;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.qualityminds.gta.config.ConnectionSettings;

public class MQDriver {
	private static final Logger logger = LoggerFactory.getLogger(MQDriver.class);


	public static void sendTextMsgToQueue(ConnectionSettings conSettings, String msg, Map<String, Object> msgProperties) throws JMSException {

		QueueConnection queueConnection = getQueueConnection(conSettings);
		QueueSession session = getQueueSession(queueConnection, conSettings);
		Queue queue = session.createQueue(conSettings.getQueueName());

		try (QueueSender sender = session.createSender(queue)) {

			TextMessage textMsg = session.createTextMessage();
			textMsg.setText(msg);

			if (msgProperties != null) {
				for (Map.Entry<String, Object> msgProperty : msgProperties.entrySet()) {
					textMsg.setObjectProperty(msgProperty.getKey(), msgProperty.getValue());
				}
			}

			sender.send(textMsg);
		} finally {
			session.close();
			queueConnection.close();
		}
	}


	public static List<Object> browseMessages(ConnectionSettings conSettings, String messageSelector) throws JMSException {
		QueueConnection queueConnection = getQueueConnection(conSettings);
		QueueSession session = getQueueSession(queueConnection, conSettings);
		Queue queue = session.createQueue(conSettings.getQueueName());

		ArrayList<Object> allObjects = new ArrayList<>();

		try (QueueBrowser browser =
					 StringUtils.isNotBlank(messageSelector) ? session.createBrowser(queue, messageSelector) : session.createBrowser(queue)) {
			Enumeration<?> messageEnumerator = browser.getEnumeration();


			while (messageEnumerator.hasMoreElements()) {
				allObjects.add(messageEnumerator.nextElement());
			}

		} finally {
			session.close();
			queueConnection.close();
		}

		return allObjects;
	}

	private static QueueConnection getQueueConnection(ConnectionSettings conSettings) throws JMSException {
		JmsFactoryFactory ff = JmsFactoryFactory.getInstance(JmsConstants.WMQ_PROVIDER);
		JmsQueueConnectionFactory qcf = ff.createQueueConnectionFactory();

		qcf.setStringProperty(CommonConstants.WMQ_HOST_NAME, conSettings.getHost());
		qcf.setIntProperty(CommonConstants.WMQ_PORT, conSettings.getPort());
		qcf.setIntProperty(CommonConstants.WMQ_CONNECTION_MODE, CommonConstants.WMQ_CM_CLIENT);

		qcf.setStringProperty(CommonConstants.WMQ_CHANNEL, conSettings.getChannelName());
		qcf.setStringProperty(CommonConstants.WMQ_QUEUE_MANAGER, conSettings.getQueueManagerName());

		if (StringUtils.isNotBlank(conSettings.getApplicationName())) {
			qcf.setStringProperty(CommonConstants.WMQ_APPLICATIONNAME, conSettings.getApplicationName());
		}

		/* Needs to be tested how we should set the credentials
		qcf.setStringProperty(JmsConstants.USERID, conSettings.getUserId());
		qcf.setBooleanProperty(JmsConstants.USER_AUTHENTICATION_MQCSP, conSettings.getUserPassword() != null);

		qcf.setStringProperty(JmsConstants.PASSWORD, conSettings.getUserPassword());
		*/

		return qcf.createQueueConnection(conSettings.getUserId(), conSettings.getUserPassword());
	}

	private static QueueSession getQueueSession(QueueConnection queueConnection, ConnectionSettings conSettings) throws JMSException {
		queueConnection.start();
		return queueConnection.createQueueSession(conSettings.isTransacted(), Session.AUTO_ACKNOWLEDGE);
	}
}
