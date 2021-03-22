package assegnamento1.main;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import assegnamento1.main.messages.Completed;
import assegnamento1.main.messages.Message;
import assegnamento1.main.messages.NodeStatistics;
import assegnamento1.main.messages.ResendRequest;

public class ReceiverMinion extends Thread{
	private Socket recClient;
	private int id;
	private int messagesReceived = 0;
	private NodeStatistics stats;
	private boolean lostBefore = false;
	
	private static final int M = 10000;

	
	public ReceiverMinion(int id, Socket recClient, NodeStatistics stats) {
		this.id = id;
		this.recClient = recClient;
		this.stats = stats;
	}
	
	@Override
	public void run() {
		try {
			ObjectOutputStream os = new ObjectOutputStream(recClient.getOutputStream());
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(recClient.getInputStream()));
			recClient.setSoTimeout(1000);
			while (messagesReceived < M) {
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
				if (messagesReceived == M)
					writeObject(os, new Completed());
			}
			System.out.println("ReceiverMinion " + id + " ha terminato");
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
