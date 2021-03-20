package assegnamento1.main.messages;

import java.io.Serializable;

public class NodeStatistics implements Serializable{
	private static final long serialVersionUID = 1L;
	private int id;
	private long sendTime;
	private long nSend = 0;
	private long nResend = 0;
	private long nReceived = 0;
	
	public NodeStatistics(int id) {
		this.id = id;
	}
	
	public long getSendTime() {
		return sendTime;
	}
	
	public void setSendTime(long sendTime) {
		this.sendTime = sendTime;
	}

	public long getnSend() {
		return nSend;
	}

	public synchronized void incrementNSend() {
		this.nSend++;
	}

	public long getnResend() {
		return nResend;
	}

	public synchronized void incrementNResend() {
		this.nResend++;
	}

	public long getnReceived() {
		return nReceived;
	}

	public synchronized void incrementNReceived() {
		this.nReceived++;
	}
	
	@Override
	public String toString() {
		return "NodeStatistics[ID: " + id + ", sendTime: " + sendTime + ", nSend: " + nSend + ", nResend: " + nResend + ", nReceived: " + nReceived + "]";
	}
}
