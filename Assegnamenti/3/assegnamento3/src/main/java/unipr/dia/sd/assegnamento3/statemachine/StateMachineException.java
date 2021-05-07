package unipr.dia.sd.assegnamento3.statemachine;

public class StateMachineException extends Exception {

	private static final long serialVersionUID = 1L;

	public StateMachineException() {
		super();
	}

	public StateMachineException(String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public StateMachineException(String message, Throwable cause) {
		super(message, cause);
	}

	public StateMachineException(String message) {
		super(message);
	}

	public StateMachineException(Throwable cause) {
		super(cause);
	}

	
}