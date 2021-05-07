package unipr.dia.sd.assegnamento3.statemachine;

public class TransitionBuilder {
	
	private State source;
	private Event event;
	private Guard guard;
	
	
	public TransitionBuilder(State source, Event event, Guard guard) {
		super();
		this.source = source;
		this.event = event;
		this.guard = guard;
	}


	public State to( State target ) {
		Transition t = new Transition(source, target, event, guard );
		source.getTransitions().add( t );
		return source;
	}
	
}
