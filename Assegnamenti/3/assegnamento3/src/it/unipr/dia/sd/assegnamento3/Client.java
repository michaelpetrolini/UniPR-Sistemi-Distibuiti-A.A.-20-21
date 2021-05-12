package it.unipr.dia.sd.assegnamento3;

import static it.unipr.dia.sd.assegnamento3.statemachine.Event.event;
import static it.unipr.dia.sd.assegnamento3.statemachine.State.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueReceiver;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;

import it.unipr.dia.sd.assegnamento3.statemachine.Action;
import it.unipr.dia.sd.assegnamento3.statemachine.Event;
import it.unipr.dia.sd.assegnamento3.statemachine.Guard;
import it.unipr.dia.sd.assegnamento3.statemachine.State;
import it.unipr.dia.sd.assegnamento3.statemachine.StateMachine;
import it.unipr.dia.sd.assegnamento3.statemachine.StateMachineException;
import it.unipr.dia.sd.assegnamento3.threads.ActionToss;

public class Client extends StateMachine {
	
	private static final long SLEEP_TIME = 1000;
	private static final long WRITE_TIME = 5000;
	private static final long READ_TIME = 1000;
		
	private final State free = state("FREE");
	private final State requesting = state("REQUESTING");
	private final State reading = state("READING");
	private final State writing = state("WRITING");
	
	private final Event action = event("ACTION");
	
	private String id;
	private int nCoordinators;
	private List<String> voters = new ArrayList<>();
	private ActiveMQConnection connection;
	private ActionType currentAction;
	private QueueReceiver receiver;
	private boolean accessGranted;

	public Client(String id, int nCoordinators) throws JMSException {
		this.id = id;
		this.nCoordinators = nCoordinators;
		setVerbose(true);
		
		ActiveMQConnectionFactory cf = new ActiveMQConnectionFactory("tcp://localhost:61616");

		connection = (ActiveMQConnection) cf.createConnection();
		connection.start();
		
		ActionToss at = new ActionToss(this);
		at.start();
		
		QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = queueSession.createQueue("client_" + id);
		receiver = queueSession.createReceiver(queue);
	}
	
	@Override
	protected State defineStateMachine() {
		free
			.actions(freeCoordinators())
			.transition(action).to(requesting);
		requesting
			.actions(publish(), waitForVoters())
			.transition(canWrite()).to(writing)
			.transition(canRead()).to(reading)
			.transition().to(free);
		writing
			.actions(write(), freeCoordinators())
			.transition().to(free);
		reading
			.actions(read(), freeCoordinators())
			.transition().to(free);
		return free;
	}

	@Override
	protected State getStateByCode(String code) {
		if (code == null) {
            return null;
        }

        for (State state : Arrays.asList(free, requesting, reading, writing)) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }

        return null;
	}
	
	private Action publish() {
		return () -> {			
			try {
				System.out.println("Need to do a " + currentAction + " operation");
				TopicSession session = connection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
				Topic topic = session.createTopic("topic_" + id);
				TopicPublisher publisher = session.createPublisher(topic);
				TextMessage message = session.createTextMessage();
			
				message.setText(id);
				publisher.publish(message);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

	private Action waitForVoters() {
		return () -> {
			try {
				long start = System.currentTimeMillis();
				while (System.currentTimeMillis() - start < SLEEP_TIME) {
					Message m =  receiver.receive(SLEEP_TIME);
					if (m != null) {
						TextMessage message = (TextMessage) m;
						String coordinatorId = message.getText();
						if ((currentAction.equals(ActionType.READ) && voters.size() < 1) || 
								(currentAction.equals(ActionType.WRITE) && voters.size() < nCoordinators)) {
							System.out.println("Received vote from coordinator " + coordinatorId);
							voters.add(coordinatorId);
							if ((currentAction.equals(ActionType.READ) && voters.size() == 1) || 
									(currentAction.equals(ActionType.WRITE) && voters.size() == nCoordinators)) {
								accessGranted = true;
							}
						} else {
							freeCoordinator(coordinatorId);
						}
					}
				}
			} catch (JMSException e) {
				e.printStackTrace();
			}
		};
	}

	private void freeCoordinator(String coordinatorId) throws JMSException {
		QueueSession queueSession = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue = queueSession.createQueue("coordinator_" + coordinatorId);
		QueueSender sender = queueSession.createSender(queue);
		TextMessage reply = queueSession.createTextMessage();
		
		reply.setText(id);
		sender.send(reply);
	}
	
	public void action(ActionType actionType) throws StateMachineException {
		currentAction = actionType;
		fire(action);
	}
	
	public enum ActionType {
		READ,
		WRITE
	}
	
	private Action write() {
		return () -> {
			try {
				Thread.sleep(WRITE_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
	}
	
	private Action read() {
		return () -> {
			try {
				Thread.sleep(READ_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		};
	}
	
	private Action freeCoordinators() {
		return () -> {
			accessGranted = false;
			voters.stream().forEach(coordinatorId -> {
				try {
					freeCoordinator(coordinatorId);
				} catch (JMSException e) {
					e.printStackTrace();
				}
			});
			voters.clear();
		};
	}
	
	private Guard canRead() {
		return () -> {
			return currentAction.equals(ActionType.READ) && accessGranted;
		};
	}
	
	private Guard canWrite() {
		return () -> {
			return currentAction.equals(ActionType.WRITE) && accessGranted;
		};
	}
}
