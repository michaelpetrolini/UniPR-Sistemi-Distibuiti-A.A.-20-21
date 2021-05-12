package it.unipr.dia.sd.assegnamento3;

import javax.jms.JMSException;

import it.unipr.dia.sd.assegnamento3.statemachine.StateMachineException;

public class MainApplication {

	public static void main(String[] args) throws StateMachineException, JMSException {
		if (args.length > 0) {
			String nodeType = args[0];
			String id = args[1];
			Integer nClients = Integer.valueOf(args[2]);
			Integer nCoordinators = Integer.valueOf(args[3]);
			
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
