package it.unipr.dia.sd.assegnamento3;

import static it.unipr.dia.sd.assegnamento3.statemachine.State.state;
import static it.unipr.dia.sd.assegnamento3.statemachine.Event.event;

import java.util.Arrays;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSession;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import it.unipr.dia.sd.assegnamento3.statemachine.Action;
import it.unipr.dia.sd.assegnamento3.statemachine.Event;
import it.unipr.dia.sd.assegnamento3.statemachine.State;
import it.unipr.dia.sd.assegnamento3.statemachine.StateMachine;
import it.unipr.dia.sd.assegnamento3.statemachine.StateMachineException;

public class Coordinator extends StateMachine implements MessageListener {

	private final State free = state("FREE");
	private final State waiting = state("WAITING");
	
	private final Event request = event("REQUEST");
	
	private String id;
	private Integer clientId;
	private ActiveMQConnection connection;
	private QueueReceiver receiver;
	
	public Coordinator(String id, int nClients) throws JMSException {
		this.id = id;
		setVerbose(true);
		
		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");

		connection = (ActiveMQConnection) cf.createConnection();
		connection.start();
		
		TopicSession topicSession = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
		
		for (int i = 1; i <= nClients; i++) {
			Topic topic = topicSession.createTopic("topic_" + i);
			MessageConsumer consumer = topicSession.createConsumer(topic);
			consumer.setMessageListener(this);
		}
		
		QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = queueSession.createQueue("coordinator_" + id);
						
		receiver = queueSession.createReceiver(queue);
	}
	
	@Override
	protected State defineStateMachine() {
		free
			.actions(receive())
			.transition(request).to(waiting);
		waiting
			.actions(sendVote(), waitForRelease())
			.transition().to(free);
		return free;
	}

	@Override
	protected State getStateByCode(String code) {
		if (code == null) {
            return null;
        }

        for (State state : Arrays.asList(free, waiting)) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }

        return null;
	}

	private Action receive() {
		return () -> {
			
		};
	}
	
	private Action sendVote() {
		return () -> {			
			try {
				System.out.println("Sending vote to client " + clientId);
				QueueSession session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
				Queue queue = session.createQueue("client_" + clientId);
				QueueSender sender = session.createSender(queue);
				TextMessage message = session.createTextMessage();
				
				message.setText(id);
				sender.send(message);
			
			} catch (Exception e) {
				e.printStackTrace();
			} 
		};
	}
	
	private Action waitForRelease() {
		return () -> {
			try {								  
				while (true) {
					TextMessage message = (TextMessage) receiver.receive();
				    if (Integer.valueOf(message.getText()) == clientId){
				    	System.out.println("Received release from client " + clientId);
				    	break;
				    }
				}
			      
			} catch (JMSException e) {
				e.printStackTrace();
			}
		};
	}

	@Override
	public void onMessage(Message m) {
		try {
			if (current.equals(free)) {
				TextMessage message = (TextMessage) m;
				System.out.println("Request from client " + message.getText());
				clientId = Integer.valueOf(message.getText());
				fire(request);
			}
		} catch (JMSException | StateMachineException e) {
			e.printStackTrace();
		}
	}
}
