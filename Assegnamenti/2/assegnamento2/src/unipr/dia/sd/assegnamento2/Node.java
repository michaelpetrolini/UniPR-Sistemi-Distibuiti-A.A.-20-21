package unipr.dia.sd.assegnamento2;

import static unipr.dia.sd.assegnamento2.statemachine.Event.event;
import static unipr.dia.sd.assegnamento2.statemachine.State.state;

import unipr.dia.sd.assegnamento2.statemachine.Event;
import unipr.dia.sd.assegnamento2.statemachine.Guard;
import unipr.dia.sd.assegnamento2.statemachine.State;
import unipr.dia.sd.assegnamento2.statemachine.StateMachine;

public class Node extends StateMachine{
	
	private final State starting = state("STARTING");
	private final State idle = state("IDLE");
	private final State candidate = state("CANDIDATE");
	private final State coordinator = state("COORDINATOR");
	private final State requester = state("REQUESTER");
	private final State waiter = state("WAITER");
	private final State dead = state("DEAD");
	
	private final Event wakeUp = event("WAKE-UP");
	
	private int id;

	private static final int N = 5;
	
	public Node(int id) {
		this.id = id;
	}
	
	@Override
	protected State defineStateMachine() {
		starting
			.transition(isLast()).to(coordinator)
			.transition().to(idle);
		return starting;
	}

	@Override
	protected State getStateByCode(String code) {
		// TODO Auto-generated method stub
		return null;
	}

	private Guard isLast() {
		return () -> {
			if (id == N)
				return true;
			return false;
		};
	}
}
