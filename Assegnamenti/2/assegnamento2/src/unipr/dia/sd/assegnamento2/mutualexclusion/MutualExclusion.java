package unipr.dia.sd.assegnamento2.mutualexclusion;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MutualExclusion extends Remote{
	
	int getId() throws RemoteException;
	
	void requireAccess(MutualExclusion me) throws RemoteException;
	
	void accessGranted(MutualExclusion me) throws RemoteException;
	
	void returnAccess() throws RemoteException;
}
