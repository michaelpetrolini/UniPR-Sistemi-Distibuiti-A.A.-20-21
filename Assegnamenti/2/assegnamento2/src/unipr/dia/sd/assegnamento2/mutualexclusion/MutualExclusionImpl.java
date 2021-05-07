package unipr.dia.sd.assegnamento2.mutualexclusion;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.concurrent.BlockingQueue;

public class MutualExclusionImpl extends UnicastRemoteObject implements MutualExclusion{

	private static final long serialVersionUID = 1L;
	
	private int id;
	private BlockingQueue<MutualExclusion> requestList;
	private boolean resourceHolded;
	private boolean usingResource;

	public MutualExclusionImpl(int id, BlockingQueue<MutualExclusion> requestList) throws RemoteException {
		super();
		this.id = id;
		this.requestList = requestList;
	}
	
	@Override
	public int getId() {
		return id;
	}

	@Override
	public void requireAccess(MutualExclusion me) {
		try {
			requestList.put(me);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void accessGranted(MutualExclusion me) {
		setUsingResource(true);
	}

	@Override
	public void returnAccess() {
		System.out.println("La risorsa è stata restituita");
		setResourceHolded(false);
	}
	
	public boolean isResourceHolded() {
		return resourceHolded;
	}
	
	public void setResourceHolded(boolean flag) {
		resourceHolded = flag;
	}

	public boolean isUsingResource() {
		return usingResource;
	}
	
	public void setUsingResource(boolean flag) {
		usingResource = flag;
	}
	
	public void reset() {
		resourceHolded = false;
		usingResource = false;
	}
}
