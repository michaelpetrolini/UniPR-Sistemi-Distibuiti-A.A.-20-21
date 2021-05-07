package unipr.dia.sd.assegnamento2;

import static unipr.dia.sd.assegnamento2.statemachine.Event.event;
import static unipr.dia.sd.assegnamento2.statemachine.State.state;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import unipr.dia.sd.assegnamento2.election.Election;
import unipr.dia.sd.assegnamento2.election.ElectionImpl;
import unipr.dia.sd.assegnamento2.mutualexclusion.MutualExclusion;
import unipr.dia.sd.assegnamento2.mutualexclusion.MutualExclusionImpl;
import unipr.dia.sd.assegnamento2.statemachine.Action;
import unipr.dia.sd.assegnamento2.statemachine.Event;
import unipr.dia.sd.assegnamento2.statemachine.Guard;
import unipr.dia.sd.assegnamento2.statemachine.State;
import unipr.dia.sd.assegnamento2.statemachine.StateMachine;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;
import unipr.dia.sd.assegnamento2.threads.FailureEventToss;
import unipr.dia.sd.assegnamento2.threads.ResourceEventToss;
import unipr.dia.sd.assegnamento2.threads.WakingEventToss;

public class Node extends StateMachine{

	private static final int MINTIME = 100;
	private static final int MAXTIME = 500;
	private static final int MAXATTEMPTS = 30;	

	private final State starting = state("STARTING");
	private final State candidate = state("CANDIDATE");
	private final State coordinator = state("COORDINATOR");
	private final State running = state("RUNNING");
	private final State waiting = state("WAITING");
	private final State holding = state("HOLDING_RESOURCE");
	private final State dead = state("DEAD");
	
	private final Event wakeUp = event("WAKE-UP");
	private final Event failure = event("FAILURE");
	private final Event election = event("ELECTION");
	private final Event informed = event("INFORMED");
	private final Event request = event("REQUEST");
	
	private int id;
	private int nNodes;
	private Registry registry;
	private BlockingQueue<Election> electionList;
	private BlockingQueue<MutualExclusion> requestList;
	private ElectionImpl electionManager;
	private MutualExclusionImpl mutualExclusion;	
	private boolean failed;
	
	public Node(int id, int nNodes) throws RemoteException {
		this.id = id;
		this.nNodes = nNodes;
		if (id == 1)
			registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		else
			registry = LocateRegistry.getRegistry();
		this.electionList = new LinkedBlockingDeque<>(nNodes);
		this.requestList = new LinkedBlockingDeque<>(nNodes);
		setVerbose(true);
	}
	
	@Override
	protected State defineStateMachine() {
		starting
			.actions(onStart())
			.transition(isLast()).to(coordinator)
			.transition(informed).to(running);
		candidate
			.actions(sendMessages(), replyToMessages(), waitForReply())
			.transition(checkIfDead()).to(dead)
			.transition(failure).to(dead)
			.transition(isElected()).to(coordinator)
			.transition(informed).to(running);
		coordinator
			.actions(notifyNodes(), manageRequests())
			.transition(checkIfDead()).to(dead)
			.transition(coordinatorChanged()).to(running)
			.transition(election()).to(candidate);
		running
			.transition(election).to(candidate)
			.transition(request).to(waiting)
			.transition(failure).to(dead);
		waiting
			.actions(sendRequest())
			.transition(checkIfDead()).to(dead)
			.transition(election()).to(candidate)
			.transition(grantedAccess()).to(holding)
			.transition(failureDetected()).to(candidate);
		holding
			.actions(holdResource())
			.transition(checkIfDead()).to(dead)
			.transition(completed()).to(running)
			.transition(election()).to(candidate);
		dead
			.actions(reset())
			.transition(wakeUp).to(candidate);
		return starting;
	}

	@Override
	protected State getStateByCode(String code) {
        if (code == null) {
            return null;
        }

        for (State state : Arrays.asList(starting, candidate, coordinator, running, waiting, holding, dead)) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }

        return null;
    }
	
	private Action onStart() {
		return () -> {
			try {
				mutualExclusion = new MutualExclusionImpl(id, requestList);
				registry.rebind("MEM_" + id, mutualExclusion);
				
				electionManager = new ElectionImpl(this, electionList);
				registry.rebind("EM_" + id, electionManager);
								
				FailureEventToss failure = new FailureEventToss(this);
				failure.start();
				
				WakingEventToss waking = new WakingEventToss(this);
				waking.start();
				
				ResourceEventToss resource = new ResourceEventToss(this);
				resource.start();	
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}
	
	private Action sendMessages() {
		return () -> {
			try {
				electionManager.reset();
				for (int receiverId = id + 1; receiverId <= nNodes; receiverId++) {
					System.out.println("Invio al nodo " + receiverId + " un messaggio");
					Election receiver = (Election) registry.lookup("EM_" + receiverId);
					receiver.getElectionMessage(electionManager);
				}	
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

	private Action replyToMessages() {
		return () -> {
			try {
				while (!electionList.isEmpty() && !isDead()) {
					Election message = electionList.poll();
					System.out.println("Ho ricevuto un messaggio da " + message.getId() + ", gli rispondo");
					Election sender = (Election) registry.lookup("EM_" + message.getId());
					sender.replyToElectionMessage();
				}	
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}
	
	private Action waitForReply() {
		return () -> {
				waitOnConditions(!electionManager.isResponse());
		};
	}
	
	private Action notifyNodes() {
		return () -> {
			try {
				for (int receiverId = 1; receiverId <= nNodes; receiverId++) {
					if (receiverId != id) {
						Election receiver = (Election) registry.lookup("EM_" + receiverId);
						receiver.getCoordinationMessage(mutualExclusion);
					}
				}
				electionManager.setCoordinator(mutualExclusion);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}
	
	private Action manageRequests() {
		return () -> {
			try {
				while (!isDead() && electionList.isEmpty() && electionManager.isCoordinator()) {
					if (!requestList.isEmpty()) {
						MutualExclusion next = requestList.poll();
						System.out.println("Do il controllo della risorsa a " + next.getId());
						next.accessGranted(mutualExclusion);
										
						waitOnConditions(mutualExclusion.isResourceHolded());
						
						if (mutualExclusion.isResourceHolded())
							System.out.println("Sembra che il nodo " + next.getId() + " sia fallito mentre controllava la risorsa, gliela ritiro");
							mutualExclusion.setResourceHolded(false);
					}
				}	
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}
	
	private Action sendRequest() {
		return () -> {
			try {
				electionManager.getCoordinator().requireAccess(mutualExclusion);
				
				waitOnConditions(!isDead());
				
				if (!mutualExclusion.isUsingResource())
					System.out.println("Sembra che il coordinatore non risponda, indico nuove elezioni");
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		};
	}
	
	private Action holdResource() {
		return () -> {
			System.out.println("Ho ricevuto il controllo della risorsa");
			int waiting = new Random().nextInt(MAXTIME - MINTIME) + MINTIME;

			long start = System.currentTimeMillis();
			long end = System.currentTimeMillis();
			while (end - start <= waiting && !isDead())
				end = System.currentTimeMillis();
			
			if (!isDead())
				mutualExclusion.setUsingResource(false);
		};
	}
	
	private Action reset() {
		return () -> {
			electionList.clear();
			requestList.clear();
			electionManager.reset();
			mutualExclusion.reset();
		};
	}
	
	private Guard isLast() {
		return () -> {
			return id == nNodes;
		};
	}
			
	private Guard isElected() {
		return () -> {
			return !electionManager.isResponse();
		};
	}
	
	private Guard failureDetected() {
		return () -> {
			return !mutualExclusion.isUsingResource();
		};
	}

	private Guard grantedAccess() {
		return () -> {
			return mutualExclusion.isUsingResource();
		};
	}
	
	private Guard completed() {
		return () -> {
			return !mutualExclusion.isUsingResource();
		};
	}

	private Guard election() {
		return () -> {
			return !electionList.isEmpty();
		};
	}
	
	private Guard checkIfDead() {
		return () -> {
			return isDead();
		};
	}
	
	private Guard coordinatorChanged() {
		return () -> {
			return !electionManager.isCoordinator();
		};
	}
	
	public void failed() throws StateMachineException {
		failed = true;
		fire(failure);
	}

	public void wakeUp() throws StateMachineException {
		failed = false;
		fire(wakeUp);
	}
	
	public int getId() {
		return id;
	}

	public void informNode() throws StateMachineException {
		fire(informed);
	}

	public void newElection() throws StateMachineException {
		fire(election);
	}
	
	public void accessResource() throws StateMachineException {
		fire(request);
	}
	
	public boolean isDead() {
		return failed;
	}
	
	private void waitOnConditions(boolean condition) {
		try {
			int counter = 0;
			while (counter++ < MAXATTEMPTS && condition)
				Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
}
}
