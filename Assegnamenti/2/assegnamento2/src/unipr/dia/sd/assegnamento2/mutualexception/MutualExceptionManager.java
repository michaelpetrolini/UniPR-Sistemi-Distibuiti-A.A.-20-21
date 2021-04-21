package unipr.dia.sd.assegnamento2.mutualexception;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;

import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public interface MutualExceptionManager extends Remote{
	
	void requireAccess(int id) throws RemoteException, NotBoundException, InterruptedException, StateMachineException;
	
	void accessGranted() throws RemoteException, InterruptedException, StateMachineException, NotBoundException;
	
	void returnAccess() throws RemoteException, NotBoundException, InterruptedException, StateMachineException;
}
