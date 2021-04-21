package unipr.dia.sd.assegnamento2;

import static unipr.dia.sd.assegnamento2.statemachine.Event.event;
import static unipr.dia.sd.assegnamento2.statemachine.State.state;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

import unipr.dia.sd.assegnamento2.election.ElectionManagerImpl;
import unipr.dia.sd.assegnamento2.mutualexception.MutualExceptionManagerImpl;
import unipr.dia.sd.assegnamento2.statemachine.Action;
import unipr.dia.sd.assegnamento2.statemachine.Event;
import unipr.dia.sd.assegnamento2.statemachine.Guard;
import unipr.dia.sd.assegnamento2.statemachine.State;
import unipr.dia.sd.assegnamento2.statemachine.StateMachine;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;
import unipr.dia.sd.assegnamento2.threads.FailureEventToss;
import unipr.dia.sd.assegnamento2.threads.ResourceEventToss;
import unipr.dia.sd.assegnamento2.threads.WaitingTimeoutHandler;
import unipr.dia.sd.assegnamento2.threads.WakingEventToss;

public class Node extends StateMachine{

	private static final int TIMEOUT = 5000;

	private final State starting = state("STARTING");
	private final State candidate = state("CANDIDATE");
	private final State coordinator = state("COORDINATOR");
	private final State running = state("RUNNING");
	private final State waiting = state("WAITING");
	private final State holding = state("HOLDING_RESOURCE");
	private final State dead = state("DEAD");
	
	private final Event wakeUp = event("WAKE-UP");
	private final Event failed = event("FAILED");
	private final Event election = event("ELECTION");
	private final Event elected = event("ELECTED");
	private final Event informed = event("INFORMED");
	private final Event request = event("REQUEST");
	private final Event failureDetected = event("FAILURE_DETECTED");
	private final Event granted = event("ACCESS_GRANTED");
	private final Event completed = event("COMPLETED");
	
	private int id;
	private int nNodes;
	private Registry registry;
	private ElectionManagerImpl electionManager;
	private MutualExceptionManagerImpl mutualExceptionManager;
	
	public Node(int id, int nNodes) throws RemoteException {
		this.id = id;
		this.nNodes = nNodes;
		if (id == 1)
			registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		else
			registry = LocateRegistry.getRegistry();
		this.electionManager = new ElectionManagerImpl(this, nNodes, registry);
		this.registry.rebind("EM_" + this.id, electionManager);
		this.mutualExceptionManager = new MutualExceptionManagerImpl(this, registry);
		this.registry.rebind("MEM_" + this.id, mutualExceptionManager);
		setVerbose(true);
	}
	
	@Override
	protected State defineStateMachine() {
		starting
			.actions(onStart())
			.transition(isLast()).to(coordinator)
			.transition(informed).to(running);
		candidate
			.actions(sendElectionMessages())
			.transition(election).to(candidate)
			.transition(elected).to(coordinator)
			.transition(informed).to(running)
			.transition(failed).to(dead);
		coordinator
			.actions(notifyAllCandidates(), manageRequests())
			.transition(election).to(candidate)
			.transition(failed).to(dead);
		running
			.transition(election).to(candidate)
			.transition(request).to(waiting)
			.transition(failed).to(dead);
		waiting
			.actions(sendRequest())
			.transition(election).to(candidate)
			.transition(granted).to(holding)
			.transition(failureDetected).to(candidate)
			.transition(failed).to(dead);
		holding
			.actions(holdResource())
			.transition(completed()).to(running)
			.transition(election).to(candidate)
			.transition(failed).to(dead);
		dead
			.actions(handleFailure())
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

	private Guard isLast() {
		return () -> {
			if (id == nNodes)
				return true;
			return false;
		};
	}
	
	public void failed() throws StateMachineException {
		if (!current.equals(dead) && !current.equals(starting)) {
			fire(failed);
		}
	}

	public void wakeUp() throws StateMachineException {
		if (current.equals(dead)) {
			fire(wakeUp);
		}
	}
	
	public int getId() {
		return id;
	}
	
	public boolean isRunning() {
		return !current.equals(dead);
	}

	public void electNode() throws StateMachineException {
		fire(elected);
	}

	public void informSimpleNode() throws StateMachineException {
		fire(informed);
	}
	
	public void complete() throws StateMachineException {
		fire(completed);
	}
	
	private Action notifyAllCandidates() {
		return () -> {
			try {
				electionManager.notifyAllCandidates();
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}
	
	private Action onStart() {
		return () -> {
			FailureEventToss failure = new FailureEventToss(this);
			failure.start();
			WakingEventToss waking = new WakingEventToss(this);
			waking.start();
			ResourceEventToss resource = new ResourceEventToss(this);
			resource.start();
		};
	}

	public void toCandidate() throws StateMachineException {
		fire(election);
	}
	
	public Action sendElectionMessages() {
		return () -> {
			try {
				electionManager.sendElectionMessages();
			} catch (RemoteException | NotBoundException | InterruptedException e) {
				e.printStackTrace();
			}
		};
	}
	
	public boolean isCoordinator() {
		return current.equals(coordinator);
	}

	public boolean isWaiting() {
		return current.equals(waiting);
	}
	
	private Action handleFailure() {
		return () -> {
			electionManager.reset();
			mutualExceptionManager.reset();
		};
	}
	
	public void accessResource() throws StateMachineException {
		fire(request);
	}
	
	private Action sendRequest() {
		return () -> {
			try {
				mutualExceptionManager.requireResource(electionManager.getCoordinator());
				WaitingTimeoutHandler handler = new WaitingTimeoutHandler(this, mutualExceptionManager, TIMEOUT);
				handler.start();
			} catch (RemoteException | NotBoundException | InterruptedException e) {
				e.printStackTrace();
			}
		};
	}
	
	private Action manageRequests() {
		return () -> {
			try {
				while (true) {
					mutualExceptionManager.manageRequests();
					Thread.sleep(100);
				}
			} catch (RemoteException | NotBoundException | InterruptedException e) {
					e.printStackTrace();
			}
		};
	}
	
	private Action holdResource() {
		return () -> {
			try {
				mutualExceptionManager.useResource(electionManager.getCoordinator());
				mutualExceptionManager.returnAccess(electionManager.getCoordinator());
			} catch (InterruptedException | RemoteException | NotBoundException e) {
				e.printStackTrace();
			}
		};
	}
	
	public void failureDetected() throws StateMachineException {
		if (isWaiting())
			System.out.println("E' stato rilevato un problema failure del coordinatore");
		fire(failureDetected);
	}

	public void grantAccess() throws StateMachineException {
		fire(granted);
	}
	
	private Guard completed() {
		return () -> {
			if (!mutualExceptionManager.isUsingResource())
				return true;
			return false;
		};
	}
}
