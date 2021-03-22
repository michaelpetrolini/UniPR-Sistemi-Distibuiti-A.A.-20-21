package assegnamento1.main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import assegnamento1.main.messages.Counter;
import assegnamento1.main.messages.Message;
import assegnamento1.main.messages.NodeStatistics;

public class SenderMinion extends Thread{
	private Socket sendClient;
	private Counter messagesSent;
	private ResetterMinion resetter;
	private int id;
	private int dest;
	private NodeStatistics stats;
	private ConcurrentRandom random;
	
	private static final double LP = -1;
	private static final int M = 10000;
	
	public SenderMinion(int id, int dest, Socket sendClient, Counter messagesSent, ResetterMinion resetter, NodeStatistics stats, ConcurrentRandom random) throws IOException {
		this.sendClient = sendClient;
		this.messagesSent = messagesSent;
		this.resetter = resetter;
		this.id = id;
		this.dest = dest;
		this.stats = stats;
		this.random = random;
	}

	@Override
	public void run() {
		try {
			ObjectOutputStream sOs = new ObjectOutputStream(sendClient.getOutputStream());
			while(resetter.isAlive()) {
				if (messagesSent.getCounter() < M) {
					int message	= messagesSent.getIncrementedCounter();
					if (random.nextFloat() > LP) {
						sOs.writeObject(new Message(id, message));
						sOs.flush();
						sOs.reset();
						//System.out.println("ID " + id + " ha inviato il messaggio " + message + " a " + dest);
					} else {
						//System.out.println("(ID " + id + " non ha inviato il messaggio " + message + " a " + dest + ")");
					}
					stats.incrementNSend();
				}				
			}
			System.out.println("SenderMinion " + id + " ha terminato l'invio dei messaggi a " + dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
