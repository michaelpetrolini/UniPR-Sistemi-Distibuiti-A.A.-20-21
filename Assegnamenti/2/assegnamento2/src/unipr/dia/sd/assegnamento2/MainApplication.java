package unipr.dia.sd.assegnamento2;

import java.rmi.RemoteException;

import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public class MainApplication {

	public static void main(String[] args) throws StateMachineException, RemoteException {
		Integer id = Integer.valueOf(args[0]);
		Node node = new Node(id);
		node.init();
	}

}
