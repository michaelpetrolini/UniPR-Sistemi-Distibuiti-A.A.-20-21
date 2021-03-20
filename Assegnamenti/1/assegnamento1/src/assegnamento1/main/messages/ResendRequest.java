package assegnamento1.main.messages;

import java.io.Serializable;

public class ResendRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private int firstLost;
	
	public ResendRequest(int firstLost) {
		this.firstLost = firstLost;
	}

	public int getFirstLost() {
		return firstLost;
	}
}
