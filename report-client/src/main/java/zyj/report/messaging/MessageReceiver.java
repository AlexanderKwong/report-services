package zyj.report.messaging;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Component;
import zyj.report.service.ReportMsgService;


@Component
public class MessageReceiver {

	static final Logger LOG = LoggerFactory.getLogger(MessageReceiver.class);
	private static final String ORDER_RESPONSE_QUEUE = "reportRequest";

	@Autowired
	ReportMsgService reportMsgService;
	
	@JmsListener(destination = ORDER_RESPONSE_QUEUE)
	public void receiveMessage(final TextMessage message) throws JMSException {
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++");
//		MessageHeaders headers =  message.getHeaders();
//		LOG.info("Application : headers received : {}", headers);
		
		String msg = message.getText();
		LOG.info("Application :  received : {}",msg);
		try {
			reportMsgService.dispatchMsg(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LOG.info("+++++++++++++++++++++++++++++++++++++++++++++++++++++");
	}

}
