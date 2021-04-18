package unipr.dia.sd.assegnamento2.election;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public interface ElectionManager extends Remote {
	
	void getElectionMessage(ElectionMessage message) throws RemoteException, NotBoundException, InterruptedException, StateMachineException;
	
	void replyToElectionMessage(ElectionMessage message) throws RemoteException;
	
	void getCoordinationMessage(int id) throws RemoteException, StateMachineException;
}
