package assegnamento1.main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import assegnamento1.main.messages.Counter;
import assegnamento1.main.messages.Message;
import assegnamento1.main.messages.NodeStatistics;

public class SenderMinion extends Thread{
	private Socket sendClient;
	private Counter messagesSent;
	private ResetterMinion resetter;
	private Random r = new Random();
	private int id;
	private int dest;
	private NodeStatistics stats;
	
	private static final double LP = 0.05;
	private static final int M = 1000;
	
	public SenderMinion(int id, int dest, Socket sendClient, Counter messagesSent, ResetterMinion resetter, NodeStatistics stats) throws IOException {
		this.sendClient = sendClient;
		this.messagesSent = messagesSent;
		this.resetter = resetter;
		this.id = id;
		this.dest = dest;
		this.stats = stats;
	}

	@Override
	public void run() {
		try {
			ObjectOutputStream sOs = new ObjectOutputStream(sendClient.getOutputStream());
			while(resetter.isAlive()) {
				int message	= (messagesSent.getCounter() < M)? messagesSent.getIncrementedCounter(): messagesSent.getIncrementedCounter();
				float random = r.nextFloat();
				if (random > LP) {
					sOs.writeObject(new Message(id, message));
					sOs.flush();
				}
				stats.incrementNSend();
				
			}
			System.out.println("SenderMinion " + id + " ha terminato l'invio dei messaggi a " + dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
