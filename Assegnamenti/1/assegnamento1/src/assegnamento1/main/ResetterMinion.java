package assegnamento1.main;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import assegnamento1.main.messages.Counter;
import assegnamento1.main.messages.NodeStatistics;
import assegnamento1.main.messages.ResendRequest;

public class ResetterMinion extends Thread{
	private Socket sendClient;
	private Counter messagesSent;
	private int id;
	private int destinatario;
	private NodeStatistics stats;
	
	public ResetterMinion(int id, int destinatario, Socket sendClient, Counter messagesSent, NodeStatistics stats) throws IOException {
		this.sendClient = sendClient;
		this.messagesSent = messagesSent;
		this.id = id;
		this.destinatario = destinatario;
		this.stats = stats;
	}
	
	@Override
	public void run() {
		try {
			ObjectInputStream sIs = new ObjectInputStream(new BufferedInputStream(sendClient.getInputStream()));
			while (true) {
				Object sObj = sIs.readObject();
				if (sObj instanceof ResendRequest) {
					ResendRequest rr = (ResendRequest) sObj;
					//System.out.println("ID " + id + " riceve da " + destinatario + " la richiesta di rinvio del messaggio n " + (rr.getFirstLost() + 1));
					messagesSent.setCounter(rr.getFirstLost());
					stats.incrementNResend();
				} else
					break;
			}
			System.out.println("ResetterMinion " + id + " ha ricevuto il messaggio di completamento da " + destinatario);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
}
