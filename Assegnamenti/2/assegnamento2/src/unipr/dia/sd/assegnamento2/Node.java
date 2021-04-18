package unipr.dia.sd.assegnamento2;

import static unipr.dia.sd.assegnamento2.statemachine.Event.event;
import static unipr.dia.sd.assegnamento2.statemachine.State.state;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Arrays;

import unipr.dia.sd.assegnamento2.election.ElectionManagerImpl;
import unipr.dia.sd.assegnamento2.failures.FailureEventToss;
import unipr.dia.sd.assegnamento2.failures.WakingEventToss;
import unipr.dia.sd.assegnamento2.statemachine.Action;
import unipr.dia.sd.assegnamento2.statemachine.Event;
import unipr.dia.sd.assegnamento2.statemachine.Guard;
import unipr.dia.sd.assegnamento2.statemachine.State;
import unipr.dia.sd.assegnamento2.statemachine.StateMachine;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public class Node extends StateMachine{

	private static final int N = 5;

	private final State starting = state("STARTING");
	private final State candidate = state("CANDIDATE");
	private final State electing = state("ELECTING");
	private final State coordinator = state("COORDINATOR");
	private final State requester = state("REQUESTER");
	private final State waiter = state("WAITER");
	private final State dead = state("DEAD");
	
	private final Event wakeUp = event("WAKE-UP");
	private final Event failed = event("FAILED");
	private final Event election = event("ELECTION");
	private final Event elected = event("ELECTED");
	private final Event informed = event("INFORMED");
	
	private int id;
	private FailureEventToss failure;
	private WakingEventToss waking;
	private Registry registry;
	private ElectionManagerImpl electionManager;
	
	public Node(int id) throws RemoteException {
		this.id = id;
		if (id == 1)
			registry = LocateRegistry.createRegistry(Registry.REGISTRY_PORT);
		else
			registry = LocateRegistry.getRegistry();
		this.electionManager = new ElectionManagerImpl(this, N, registry);
		this.registry.rebind("EM_" + this.id, electionManager);
		
	}
	
	@Override
	protected State defineStateMachine() {
		starting
			.actions(onStart(), onChange())
			.transition(isLast()).to(coordinator)
			.transition(informed).to(requester);
		candidate
			.actions(onChange(), startElection())
			.transition(elected).to(coordinator)
			.transition(informed).to(requester)
			.transition(failed).to(dead);
		electing
			.actions(onChange())
			.transition(elected).to(coordinator)
			.transition(informed).to(requester)
			.transition(failed).to(dead);
		coordinator
			.actions(onChange(), notifyAllCandidates())
			.transition(election).to(electing)
			.transition(failed).to(dead);
		requester
			.actions(onChange())
			.transition(election).to(electing)
			.transition(failed).to(dead);
		waiter
			.actions(onChange())
			.transition(election).to(electing)
			.transition(failed).to(dead);
		dead
			.actions(onChange())
			.transition(wakeUp).to(candidate);
		return starting;
	}

	@Override
	protected State getStateByCode(String code) {
        if (code == null) {
            return null;
        }

        for (State state : Arrays.asList(starting, candidate, electing, coordinator, requester, waiter, dead)) {
            if (code.equals(state.getCode())) {
                return state;
            }
        }

        return null;
    }

	private Guard isLast() {
		return () -> {
			if (id == N)
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

	public void toCoordinator() throws StateMachineException {
		if (current.equals(candidate)) {
			fire(elected);
		}
	}

	public void toRequester() throws StateMachineException {
		if (current.equals(candidate) || current.equals(starting)) {
				fire(informed);
		}	
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
			failure = new FailureEventToss(this);
			failure.start();
			waking = new WakingEventToss(this);
			waking.start();
		};
	}

	public void toElecting() throws StateMachineException {
		fire(election);
	}
	
	public Action onChange() {
		return () -> {
			System.out.println("I'm in state " + current.getCode());
		};
	}
	
	public Action startElection() {
		return () -> {
			try {
				electionManager.startElection();
			} catch (RemoteException | NotBoundException | InterruptedException e) {
				e.printStackTrace();
			}
		};
	}
}
