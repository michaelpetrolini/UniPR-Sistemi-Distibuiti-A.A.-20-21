package assegnamento1.messages;

import java.io.Serializable;

public class NodeStatistics implements Serializable{
	private static final long serialVersionUID = 1L;
	private int id;
	private long sendTime;
	private long nSend = 0;
	private long nLost = 0;
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

	public long getnLost() {
		return nLost;
	}

	public synchronized void incrementNLost() {
		this.nLost++;
	}

	public long getnReceived() {
		return nReceived;
	}

	public synchronized void incrementNReceived() {
		this.nReceived++;
	}
	
	@Override
	public String toString() {
		long total = nSend + nLost + nReceived;
		return "NodeStatistics[ID: " + id + ", sendTime: " + sendTime + ", nSend: " + nSend + "= " + (float) nSend/total*100 + "%, nLost: " + nLost + "= " + (float) nLost/total*100 + "%, nReceived: " + nReceived + "= " + (float) nReceived/total*100 + "%]";
	}
}
