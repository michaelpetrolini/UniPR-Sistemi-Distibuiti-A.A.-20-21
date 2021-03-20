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
	
	private static final int M = 1000;

	
	public ReceiverMinion(int id, Socket recClient, NodeStatistics stats) {
		this.id = id;
		this.recClient = recClient;
		this.stats = stats;
	}
	
	@Override
	public void run() {
		try {
			ObjectOutputStream rOs = new ObjectOutputStream(recClient.getOutputStream());
			ObjectInputStream rIs = new ObjectInputStream(new BufferedInputStream(recClient.getInputStream()));
			while (messagesReceived < M) {
				Object rObj = rIs.readObject();
				if (rObj instanceof Message) {
					stats.incrementNReceived();
					Message m = (Message) rObj;
					if (m.getMessage() == messagesReceived + 1){
						//System.out.println("ID " + id + " ha ricevuto il messaggio n " + m.getMessage());
						messagesReceived++;
					} else if (m.getMessage() > messagesReceived + 1) {
						rOs.writeObject(new ResendRequest(messagesReceived));
						rOs.flush();
						//System.out.println("ID " + id + " non ha ricevuto il messaggio n " + (messagesReceived + 1));
					}
				}
				if (messagesReceived == M) {
					rOs.writeObject(new Completed());
					rOs.flush();
				}
			}
			System.out.println("ReceiverMinion " + id + " ha terminato");
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
