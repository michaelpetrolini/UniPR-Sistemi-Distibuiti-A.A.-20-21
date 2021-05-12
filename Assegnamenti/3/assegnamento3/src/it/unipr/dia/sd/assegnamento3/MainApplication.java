package it.unipr.dia.sd.assegnamento3;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;

public class MainApplication {

	public static void main(String[] args) throws Exception {
		if (args.length > 0) {
			String nodeType = args[0];
			String id = args[1];
			Integer nClients = Integer.valueOf(args[2]);
			Integer nCoordinators = Integer.valueOf(args[3]);
			
			if (nodeType.equals("coordinator") && id.equals("1")) {
				BrokerService broker = BrokerFactory.createBroker("broker:(tcp://localhost:61616)?persistent=false&useJmx=false");
				broker.start();
			}
			
			if (nodeType.equals("client")) {
				System.out.println("Starting client n " + id);
				Client client = new Client(id, nCoordinators);
				client.init();
			} else if (nodeType.equals("coordinator")) {
				System.out.println("Starting coordinator n " + id);
				Coordinator coordinator = new Coordinator(id, nClients);
				coordinator.init();
			} else {
				System.out.println("Type can be: client/coordinator");
			}
		}
	}
}
