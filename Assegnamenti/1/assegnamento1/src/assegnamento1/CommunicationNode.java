package assegnamento1;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import assegnamento1.messages.NodeStatistics;
import assegnamento1.messages.RegisterRequest;
import assegnamento1.minions.ReceiverMinion;
import assegnamento1.minions.ResetterMinion;
import assegnamento1.minions.SenderMinion;
import assegnamento1.sync.ConcurrentRandom;
import assegnamento1.sync.Counter;

public class CommunicationNode extends Thread{
	private int id;
	private String mAddress;
	private int mPort;
	private ServerSocket server;
	private List<RegisterRequest> nodesMap;
	private List<SenderMinion> sMinionsList = new ArrayList<>();
	private List<ReceiverMinion> cMinionsList = new ArrayList<>();
	private List<Socket> recClients = new ArrayList<>();
	private List<Socket> sendClients = new ArrayList<>();
	private ConcurrentRandom random;
	private NodeStatistics stats;

	
	private static final int M = 10000;
	
	public CommunicationNode(int id, String sAddress, int sPort) {
		try {
			this.id = id;
			this.mAddress = sAddress;
			this.mPort = sPort;
			this.server = new ServerSocket(0);
			this.random = new ConcurrentRandom(id);
			this.stats = new NodeStatistics(id);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getNodeId() {
		return this.id;
	}
	
	public int getNMessages() {
		return M;
	}
	
	public ConcurrentRandom getRandom() {
		return this.random;
	}
	
	public NodeStatistics getStats() {
		return this.stats;
	}
	
	@Override
	public void run() {
		try {
			Socket mClient = new Socket(mAddress, mPort);
			registerNode(mClient);
			System.out.println("Hi! I'm client " + id + " waiting to start exhanging messages.");
			ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(mClient.getInputStream()));
			Object response = is.readObject();
			mClient.close();
			if (response instanceof List) {
				nodesMap = (List<RegisterRequest>) response;
				System.out.println("Client " + id + " has started!");
				long startTime = System.currentTimeMillis();
				nodesMap.stream().forEach((rr) -> {
					if(rr.getSender() != id) {
						try {
							Socket sendClient = new Socket(rr.getAddress(), rr.getPort());
							sendClients.add(sendClient);
							Socket recClient = server.accept();
							recClients.add(recClient);
							Counter sendCounter = new Counter();
							ReceiverMinion cMinion = new ReceiverMinion(this, recClient);
							cMinionsList.add(cMinion);
							ResetterMinion rMinion = new ResetterMinion(this, rr.getSender(), sendClient, sendCounter);
							SenderMinion sMinion = new SenderMinion(this, rr.getSender(), sendClient, sendCounter, rMinion);
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
				for (int i = 0; i < sendClients.size(); i++) {
					sendClients.get(i).close();
					recClients.get(i).close();
				}
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

	private void registerNode(Socket mClient) throws UnknownHostException, IOException {
		ObjectOutputStream os = new ObjectOutputStream(mClient.getOutputStream());
		os.writeObject(new RegisterRequest(id, server.getInetAddress().getHostName(), server.getLocalPort()));
		os.flush();
	}
}
