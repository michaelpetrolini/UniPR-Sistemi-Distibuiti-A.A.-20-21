package unipr.dia.sd.assegnamento2.election;

import java.rmi.Remote;
import java.rmi.RemoteException;

import unipr.dia.sd.assegnamento2.mutualexclusion.MutualExclusion;

public interface Election extends Remote {
	
	int getId() throws RemoteException;
	
	void getElectionMessage(Election message) throws RemoteException;
	
	void replyToElectionMessage() throws RemoteException;
	
	void getCoordinationMessage(MutualExclusion message) throws RemoteException;
}
