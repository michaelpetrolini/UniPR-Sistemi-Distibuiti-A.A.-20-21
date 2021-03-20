package assegnamento1.main.messages;

import java.io.Serializable;

public class RegisterRequest implements Serializable
{
	private static final long serialVersionUID = 1L;
	private int sender;
	private String address;
	private int port;
	
	public RegisterRequest(int sender, String address, int port) {
		this.sender = sender;
		this.address = address;
		this.port = port;
	}
	
	public int getSender() {
		return sender;
	}
	
	public String getAddress() {
		return address;
	}

	public int getPort() {
		return port;
	}
}
