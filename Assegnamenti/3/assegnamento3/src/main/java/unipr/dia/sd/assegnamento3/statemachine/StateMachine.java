package unipr.dia.sd.assegnamento3.statemachine;

import static unipr.dia.sd.assegnamento3.statemachine.Event.*;

public abstract class StateMachine {

	protected State current;
	protected boolean verbose;
	
	public StateMachine() {
	}
	
	public void init() throws StateMachineException {
		current = defineStateMachine();
		current.executeActions();
		fire( ANY );
	}

	public void init( String initialStateCode ) throws StateMachineException {
		defineStateMachine();
		State state = getStateByCode( initialStateCode );
		
		if ( state == null ) {
			throw new StateMachineException( "State with code '" + initialStateCode + "' not found" );
		}
		current = state;
	}
	
	public boolean isVerbose() {
		return verbose;
	}
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}


	protected abstract State defineStateMachine();
	
	public String getCurrentStateCode() {
		if ( current == null ) {
			return null;
		} else {
			return current.getCode();
		}
	}

	protected abstract State getStateByCode( String code );

	
	protected void fire( Event event ) throws StateMachineException {
		Event evt  = event;
		State prev = null;
		State cur = current;
		
		while ( prev != cur ) {
			prev = cur;
			cur = cur.fire( evt, verbose );
			evt = ANY;
		}
		
		current = cur;
	}
	
}
