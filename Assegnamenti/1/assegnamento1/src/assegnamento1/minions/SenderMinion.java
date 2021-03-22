package assegnamento1.minions;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.Socket;

import assegnamento1.CommunicationNode;
import assegnamento1.messages.Message;
import assegnamento1.messages.NodeStatistics;
import assegnamento1.sync.ConcurrentRandom;
import assegnamento1.sync.Counter;

public class SenderMinion extends Thread{
	private Socket sendClient;
	private Counter messagesSent;
	private ResetterMinion resetter;
	private int id;
	private int dest;
	private NodeStatistics stats;
	private ConcurrentRandom random;
	private int nMessages;
	
	private static final double LP = 0.05;
	
	public SenderMinion(CommunicationNode node, int dest, Socket sendClient, Counter messagesSent, ResetterMinion resetter) throws IOException {
		this.sendClient = sendClient;
		this.messagesSent = messagesSent;
		this.resetter = resetter;
		this.id = node.getNodeId();
		this.dest = dest;
		this.stats = node.getStats();
		this.random = node.getRandom();
		this.nMessages = node.getNMessages();
	}

	@Override
	public void run() {
		try {
			ObjectOutputStream sOs = new ObjectOutputStream(sendClient.getOutputStream());
			while(resetter.isAlive()) {
				if (messagesSent.getCounter() < nMessages) {
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
			//System.out.println("SenderMinion " + id + " ha terminato l'invio dei messaggi a " + dest);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
