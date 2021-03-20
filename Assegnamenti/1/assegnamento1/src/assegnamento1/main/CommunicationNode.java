package assegnamento1.main;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import assegnamento1.main.messages.Counter;
import assegnamento1.main.messages.NodeStatistics;
import assegnamento1.main.messages.RegisterRequest;

public class CommunicationNode extends Thread{
	private int id;
	private String mAddress;
	private int mPort;
	private ServerSocket server;
	private List<RegisterRequest> nodesMap;
	private List<SenderMinion> sMinionsList = new ArrayList<>();
	private List<ReceiverMinion> cMinionsList = new ArrayList<>();
	
	public CommunicationNode(int id, String sAddress, int sPort) {
		try {
			this.id = id;
			this.mAddress = sAddress;
			this.mPort = sPort;
			this.server = new ServerSocket(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			registerNode();
			System.out.println("Hi! I'm client " + id + " waiting to start exhanging messages.");
			Socket client = server.accept();
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
			Object response = is.readObject();
			if (response instanceof List) {
				nodesMap = (List<RegisterRequest>) response;
				System.out.println("Client " + id + " has started!");
				NodeStatistics stats = new NodeStatistics(id);
				long startTime = System.currentTimeMillis();
				nodesMap.stream().forEach((rr) -> {
					if(rr.getSender() != id) {
						try {
							Socket sendClient = new Socket(rr.getAddress(), rr.getPort());
							Socket recClient = server.accept();
							Counter sendCounter = new Counter();
							ReceiverMinion cMinion = new ReceiverMinion(id, recClient, stats);
							cMinionsList.add(cMinion);
							ResetterMinion rMinion = new ResetterMinion(id, rr.getSender(), sendClient, sendCounter, stats);
							SenderMinion sMinion = new SenderMinion(id, rr.getSender(), sendClient, sendCounter, rMinion, stats);
							sMinionsList.add(sMinion);
							cMinion.start();
							rMinion.start();
							sMinion.start();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				for (SenderMinion minion: sMinionsList)
					minion.join();
				long sendTime = System.currentTimeMillis() - startTime;
				for (ReceiverMinion minion: cMinionsList)
					minion.join();
				sendStatistics(stats, sendTime);
			}
			System.out.println("Client " + id + " finished.");
		} catch (IOException | ClassNotFoundException | InterruptedException e) {
			e.printStackTrace();
		}
	}

	private void sendStatistics(NodeStatistics stats, long sendTime) throws UnknownHostException, IOException {
		stats.setSendTime(sendTime);
		Socket mClient = new Socket(mAddress, mPort);
		ObjectOutputStream os = new ObjectOutputStream(mClient.getOutputStream());
		os.writeObject(stats);
		os.flush();
		mClient.close();
	}

	private void registerNode() throws UnknownHostException, IOException {
		Socket mClient = new Socket(mAddress, mPort);
		ObjectOutputStream os = new ObjectOutputStream(mClient.getOutputStream());
		os.writeObject(new RegisterRequest(id, server.getInetAddress().getHostName(), server.getLocalPort()));
		os.flush();
		mClient.close();
	}
}
