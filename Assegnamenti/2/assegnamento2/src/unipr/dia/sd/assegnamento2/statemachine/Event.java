package unipr.dia.sd.assegnamento2.statemachine;

public class Event {

	public static final Event ANY = event( "ANY" );
	
	private String code;

	public Event(String code) {
		super();
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static Event event( String code ) {
		return new Event( code );
	}
}
