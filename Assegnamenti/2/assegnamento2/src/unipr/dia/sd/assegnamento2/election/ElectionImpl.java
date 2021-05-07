package unipr.dia.sd.assegnamento2.election;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;

import unipr.dia.sd.assegnamento2.Node;
import unipr.dia.sd.assegnamento2.mutualexclusion.MutualExclusion;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public class ElectionImpl extends UnicastRemoteObject implements Election{

	private static final long serialVersionUID = 1L;
	
	private Node node;
	private BlockingQueue<Election> electionList;
	private MutualExclusion coordinator;
	private boolean reply;

	public ElectionImpl(Node node, BlockingQueue<Election> electionList) throws RemoteException{
		this.node = node;
		this.electionList = electionList;
	}
	
	@Override
	public int getId() {
		return node.getId();
	}

	@Override
	public void getElectionMessage(Election message) {
		try {
			if (!node.isDead()) {
				electionList.put(message);
				node.newElection();
			}			
		} catch (InterruptedException | StateMachineException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void replyToElectionMessage() {
		reply = true;
	}
	
	@Override
	public void getCoordinationMessage(MutualExclusion message) {
		try {
			setCoordinator(message);
			node.informNode();
		} catch (StateMachineException e) {
			e.printStackTrace();
		}
	}

	public MutualExclusion getCoordinator() {
		return coordinator;
	}

	public boolean isResponse() {
		return reply;
	}

	public void reset() {
		reply = false;		
	}
	
	public boolean isCoordinator() {
		try {
			return coordinator.getId() == node.getId();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setCoordinator(MutualExclusion mutualExclusion) {
		coordinator = mutualExclusion;
	}
}
