package unipr.dia.sd.assegnamento2.election;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import unipr.dia.sd.assegnamento2.Node;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public class ElectionManagerImpl extends UnicastRemoteObject implements ElectionManager{

	private static final long serialVersionUID = 1L;

	private static final long DELAY = 1000;
	
	private Node node;
	private int nNodes;
	private Registry registry;
	private int nCurrentElections;
	private Map<UUID, Integer> nResponses;
	
	public ElectionManagerImpl(Node node, int nNodes, Registry registry) throws RemoteException{
		this.node = node;
		this.nNodes = nNodes;
		this.registry = registry;
		this.nCurrentElections = 0;
		this.nResponses = new HashMap<>();
	}

	@Override
	public void getElectionMessage(ElectionMessage message) throws RemoteException, NotBoundException, InterruptedException, StateMachineException {
		if (node.isRunning()) {
			node.toElecting();
			nCurrentElections++;
			System.out.println("Node " + node.getId() + " received an ElectionMessage by node " + message.getSender());
			ElectionManager sender = (ElectionManager) registry.lookup("EM_" + message.getSender());
			sender.replyToElectionMessage(message);
			
			if (!nResponses.containsKey(message.getUniqueID())) {
				sendMessages(message);	
			}
		}
	}

	private void sendMessages(ElectionMessage message)
			throws RemoteException, NotBoundException, AccessException, InterruptedException, StateMachineException {
		nResponses.put(message.getUniqueID(), 0);
		for (int receiverId = node.getId() + 1; receiverId <= nNodes; receiverId++) {
			ElectionMessage electionMessage = new ElectionMessage(receiverId, node.getId(), message.getUniqueID());
			ElectionManager receiver = (ElectionManager) registry.lookup("EM_" + receiverId);
			receiver.getElectionMessage(electionMessage);
		}
		Thread.sleep(DELAY);
		
		if (nResponses.get(message.getUniqueID()) == 0) {
			node.toCoordinator();
		}
		nResponses.remove(message.getUniqueID());
	}
	
	public void startElection() throws AccessException, RemoteException, NotBoundException, InterruptedException, StateMachineException {
		ElectionMessage message = new ElectionMessage(0, 0);
		sendMessages(message);
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
		Integer responses = nResponses.get(message.getUniqueID());
		responses++;
		nResponses.put(message.getUniqueID(), responses);
	}
	
	@Override
	public void getCoordinationMessage(int id) throws StateMachineException {
		nCurrentElections--;
		if (nCurrentElections <= 0) {
			node.toRequester();
		}
	}
}
