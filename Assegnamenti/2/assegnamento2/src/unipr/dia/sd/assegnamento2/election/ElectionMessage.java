package unipr.dia.sd.assegnamento2.election;

import java.io.Serializable;
import java.util.UUID;

public class ElectionMessage implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	private UUID uniqueID;
	private int receiver;
	private int sender;
	
	public ElectionMessage(int receiver, int sender) {
		this.uniqueID = UUID.randomUUID();
		this.receiver = receiver;
		this.sender = sender;
	}
	
	public ElectionMessage(int receiver, int sender, UUID uniqueID) {
		this.uniqueID = uniqueID;
		this.receiver = receiver;
		this.sender = sender;
	}

	public int getReceiver() {
		return receiver;
	}

	public UUID getUniqueID() {
		return uniqueID;
	}

	public int getSender() {
		return sender;
	}
}
