package it.unipr.dia.sd.assegnamento3.statemachine;

public class Transition {

	private State source;
	private State target;
	private Event event;
	private Guard guard;
	
	
	public Transition(State source, State target, Event event, Guard guard) {
		super();
		this.source = source;
		this.target = target;
		this.event = event;
		this.guard = guard;
	}

	public State getSource() {
		return source;
	}

	public State getTarget() {
		return target;
	}

	public Event getEvent() {
		return event;
	}

	public Guard getGuard() {
		return guard;
	}
	
	boolean allows( Event event ) {
		return this.event.getCode().equals( event.getCode() );
	}
	
	boolean canFire( Event event ) {
		
		if ( this.event == null || event == null || event.getCode() == null || guard == null ) {
			System.out.println( "null parameters" );
			return false;
		}
		return this.event.getCode().equals( event.getCode()) && 
			guard.isSatisfied();
	}

}
