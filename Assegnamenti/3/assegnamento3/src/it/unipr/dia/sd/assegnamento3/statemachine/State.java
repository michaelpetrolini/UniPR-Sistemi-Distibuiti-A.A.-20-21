package it.unipr.dia.sd.assegnamento3.statemachine;

import static it.unipr.dia.sd.assegnamento3.statemachine.Event.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class State {

	private static final Guard NO_GUARD = new Guard() {

		public boolean isSatisfied() {
			return true;
		}
		
	};
	
	private String code;
	private List<Action> actions;
	private List<Transition> transitions;


	public State(String code) {
		super();
		this.code = code;
		actions = new ArrayList<>();
		transitions = new ArrayList<>();
	}

	public static State state( String code ) {
		return new State( code );
	}

	List<Action> getActions() {
		return actions;
	}
	
	List<Transition> getTransitions() {
		return transitions;
	}

	public String getCode() {
		return code;
	}
	
	public State actions( Action... actions ) {
		this.actions.addAll( Arrays.asList( actions ));
		return this;
	}

	
	public TransitionBuilder transition() {
		return transition( ANY, NO_GUARD );
	}
	public TransitionBuilder transition( Event event ) {
		return transition( event, NO_GUARD );
	}
	public TransitionBuilder transition( Guard guard ) {
		return transition( ANY, guard );
	}
	public TransitionBuilder transition( Event event, Guard guard ) {
		return new TransitionBuilder( this, event, guard);
	}
	
	public void executeActions() throws StateMachineException {
		for ( Action action : actions ) {
			action.execute();
		}
	}
	
	public boolean allows( Event event ) {
		for ( Transition transition : transitions ) {
			if ( transition.allows( event )) {
				return true;
			}
		}
		return false;
	}
	
	public State fire( Event event, boolean verbose ) throws StateMachineException {
		try {
			for ( Transition transition : transitions ) {
				if ( transition.canFire( event ) ) {
					if ( verbose ) {
						System.out.println( "transition to " + transition.getTarget().getCode());
					}
					transition.getTarget().executeActions();
					return transition.getTarget();
				}
			}
			
			return this;
		} catch (Exception e) {
			throw new StateMachineException( e );
		}
	}
}
