package assegnamento1.minions;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import assegnamento1.CommunicationNode;
import assegnamento1.messages.Completed;
import assegnamento1.messages.Message;
import assegnamento1.messages.NodeStatistics;
import assegnamento1.messages.ResendRequest;

public class ReceiverMinion extends Thread{
	private Socket recClient;
	private int id;
	private int messagesReceived = 0;
	private NodeStatistics stats;
	private boolean lostBefore = false;
	private int nMessages;
	
	private static final int SOCKET_TIMEOUT = 5000;
	
	public ReceiverMinion(CommunicationNode node, Socket recClient) {
		this.id = node.getNodeId();
		this.recClient = recClient;
		this.stats = node.getStats();
		this.nMessages = node.getNMessages();
	}
	
	@Override
	public void run() {
		try {
			ObjectOutputStream os = new ObjectOutputStream(recClient.getOutputStream());
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(recClient.getInputStream()));
			recClient.setSoTimeout(SOCKET_TIMEOUT);
			while (messagesReceived < nMessages) {
				Object rObj = getSocketObject(is);
				if (rObj instanceof Message) {
					stats.incrementNReceived();
					Message m = (Message) rObj;
					if (m.getMessage() == messagesReceived + 1){
						lostBefore = false;
						//System.out.println("ID " + id + " ha ricevuto il messaggio n " + m.getMessage());
						messagesReceived++;
					} else if (m.getMessage() > messagesReceived + 1 && !lostBefore) {
						lostBefore = true;
						writeObject(os, new ResendRequest(messagesReceived));
						//System.out.println("ID " + id + " non ha ricevuto il messaggio n " + (messagesReceived + 1) + ", invece ha ricevuto " + m.getMessage());
					}
				} else {
					writeObject(os, new ResendRequest(messagesReceived));
					//System.out.println("ID " + id + " non ha ricevuto risposte da più di 1 secondo, manda quindi un promemoria");
				}
				if (messagesReceived == nMessages)
					writeObject(os, new Completed());
			}
			//System.out.println("ReceiverMinion " + id + " ha terminato");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Object getSocketObject(ObjectInputStream rIs) {
		try {
			Object rObj = rIs.readObject();
			return rObj;
		} catch (ClassNotFoundException | IOException e) {
			return new Object();
		}
	}
	
	private void writeObject(ObjectOutputStream os, Object obj) throws IOException {
		os.writeObject(obj);
		os.flush();
		os.reset();
	}
}
