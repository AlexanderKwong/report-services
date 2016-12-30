package zyj.report.messaging;

import javax.jms.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;


@Component
public class MessageSender {

	@Autowired
	JmsTemplate jmsTemplate;
	@Autowired
	Destination destination;

	public void sendMessage(final String msg) {

		jmsTemplate.send(destination, new MessageCreator(){
				@Override
				public Message createMessage(Session session) throws JMSException{
					TextMessage textMessage = session.createTextMessage(msg);
					return textMessage;
				}
			});
	}



}
