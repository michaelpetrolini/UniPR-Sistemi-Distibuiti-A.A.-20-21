package assegnamento1.minions;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import assegnamento1.CommunicationNode;
import assegnamento1.messages.NodeStatistics;
import assegnamento1.messages.ResendRequest;
import assegnamento1.sync.Counter;

public class ResetterMinion extends Thread{
	private Socket sendClient;
	private Counter messagesSent;
	private int id;
	private int destinatario;
	
	public ResetterMinion(CommunicationNode node, int destinatario, Socket sendClient, Counter messagesSent) throws IOException {
		this.sendClient = sendClient;
		this.messagesSent = messagesSent;
		this.id = node.getNodeId();
		this.destinatario = destinatario;
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
				} else
					break;
			}
			//System.out.println("ResetterMinion " + id + " ha ricevuto il messaggio di completamento da " + destinatario);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
}
