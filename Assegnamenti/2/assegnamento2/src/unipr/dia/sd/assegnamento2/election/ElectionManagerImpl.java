package unipr.dia.sd.assegnamento2.election;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import unipr.dia.sd.assegnamento2.Node;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public class ElectionManagerImpl extends UnicastRemoteObject implements ElectionManager{

	private static final long serialVersionUID = 1L;

	private static final long MAX_DELAY = 5000;
	
	private Node node;
	private int nNodes;
	private Registry registry;
	private List<ElectionMessage> receivedMessages;
	private Map<UUID, Boolean> nResponses;
	private int coordinator;
	private boolean coordinatorAlerted;
	
	public ElectionManagerImpl(Node node, int nNodes, Registry registry) throws RemoteException{
		this.node = node;
		this.nNodes = nNodes;
		this.registry = registry;
		this.nResponses = new HashMap<>();
		this.receivedMessages = new ArrayList<>();
	}

	@Override
	public void getElectionMessage(ElectionMessage message) throws RemoteException, NotBoundException, InterruptedException, StateMachineException {
		if (node.isRunning()) {
			if (coordinator == node.getId())
				coordinatorAlerted = true;
			System.out.println("Node " + node.getId() + " received an ElectionMessage by node " + message.getSender());
			receivedMessages.add(message);
			node.newElection();
		}
	}

	public void sendMessages()
			throws RemoteException, NotBoundException, AccessException, InterruptedException, StateMachineException {
		if (receivedMessages.isEmpty())
			receivedMessages.add(new ElectionMessage(0, 0));
		while (!receivedMessages.isEmpty() && !node.isDead()) {
			ElectionMessage message = receivedMessages.remove(0);
			if (message.getSender() > 0) {
				ElectionManager sender = (ElectionManager) registry.lookup("EM_" + message.getSender());
				sender.replyToElectionMessage(message);
			}
			
			nResponses.put(message.getUniqueID(), true);
			for (int receiverId = node.getId() + 1; receiverId <= nNodes; receiverId++) {
				ElectionMessage electionMessage = new ElectionMessage(receiverId, node.getId(), message.getUniqueID());
				ElectionManager receiver = (ElectionManager) registry.lookup("EM_" + receiverId);
				receiver.getElectionMessage(electionMessage);
			}
			Thread.yield();
		}
		
		Thread.sleep(MAX_DELAY);
		if (checkResults() && !node.isDead()) {
			reset();
			coordinator = node.getId();
		}
	}
	
	private boolean checkResults() {
		return nResponses.entrySet().stream().map(e -> e.getValue()).reduce(true, (a, b) -> a && b);
	}

	public void notifyAllCandidates() throws RemoteException, NotBoundException, AccessException, StateMachineException {
		for (int receiverId = 1; receiverId <= nNodes; receiverId++) {
			if (receiverId != node.getId()) {
				ElectionManager receiver = (ElectionManager) registry.lookup("EM_" + receiverId);
				receiver.getCoordinationMessage(node.getId());
			}
		}
	}

	@Override
	public void replyToElectionMessage(ElectionMessage message) throws RemoteException {
		System.out.println("Node " + node.getId() + " replied to an ElectionMessage sent by node " + message.getReceiver());
		nResponses.put(message.getUniqueID(), false);
	}
	
	@Override
	public void getCoordinationMessage(int id) throws StateMachineException {
		reset();
		coordinator = id;
		node.informSimpleNode();
	}

	public void reset() {
		receivedMessages.clear();
		nResponses.clear();
		coordinatorAlerted = false;
		coordinator = 0;
	}

	public int getCoordinator() {
		return coordinator;
	}

	public boolean isCoordinatorAlerted() {
		return coordinatorAlerted;
	}
}
