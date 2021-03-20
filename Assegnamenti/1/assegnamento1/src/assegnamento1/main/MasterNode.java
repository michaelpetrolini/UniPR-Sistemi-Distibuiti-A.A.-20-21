package assegnamento1.main;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import assegnamento1.main.messages.NodeStatistics;
import assegnamento1.main.messages.RegisterRequest;

public class MasterNode {
	private ServerSocket server;
	private List<RegisterRequest> nodesMap = new ArrayList<>();
	private List<NodeStatistics> statsList = new ArrayList<>();
	private static final int N = 10;
	
	public MasterNode() {
		try {
			server = new ServerSocket(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void start() {
		try {
			System.out.println("Starting to create nodes...");
			for (int i = 0; i < N; i++) {
				new CommunicationNode(i, server.getInetAddress().getHostName(), server.getLocalPort()).start();
				Socket client = server.accept();
				ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(client.getInputStream()));
				Object obj = is.readObject();
				if (obj instanceof RegisterRequest) {
					RegisterRequest rr = (RegisterRequest) obj;
					nodesMap.add(rr);
				}
			}
			System.out.println("Nodes ready. Press Enter to start messages exchange.");
			System.in.read();
			System.out.println("Starting to wake up all nodes...");
			for (int i = 0; i < N; i++) {
				Socket node = new Socket("localhost", nodesMap.get(i).getPort());
				ObjectOutputStream os = new ObjectOutputStream(node.getOutputStream());
				os.writeObject(nodesMap);
				os.flush();
				node.close();
			}
			while (statsList.size() < N) {
				Socket c = server.accept();
				ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(c.getInputStream()));
				Object obj = is.readObject();
				if (obj instanceof NodeStatistics) {
					NodeStatistics stat = (NodeStatistics) obj;
					System.out.println(stat.toString());
					statsList.add(stat);
				}
				c.close();
			}
			System.out.println("Finish");
			
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
}
