package assegnamento1.messages;

import java.io.Serializable;

public class Message implements Serializable{
	private static final long serialVersionUID = 1L;
	private int sender;
	private int message;
	
	public Message(int sender, int message) {
		this.sender = sender;
		this.message = message;
	}
	
	public int getSender() {
		return sender;
	}

	public int getMessage() {
		return message;
	}
}
