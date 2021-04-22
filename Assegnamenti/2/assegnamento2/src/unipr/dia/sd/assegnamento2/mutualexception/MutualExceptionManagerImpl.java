package unipr.dia.sd.assegnamento2.mutualexception;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import unipr.dia.sd.assegnamento2.Node;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public class MutualExceptionManagerImpl extends UnicastRemoteObject implements MutualExceptionManager{

	private static final long serialVersionUID = 1L;
	private static final int MAX_DELAY = 5000;
	private static final int MINTIME = 100;
	private static final int MAXTIME = 500;	
	
	private Node node;
	private Registry registry;
	private List<Integer> requestList;
	private Random random;
	private boolean resourceHolded;
	private boolean usingResource;

	public MutualExceptionManagerImpl(Node node, Registry registry) throws RemoteException {
		super();
		this.node = node;
		this.registry = registry;
		this.requestList = new ArrayList<>();
		this.random = new Random();
	}
	
	public void reset() {
		requestList.clear();
		resourceHolded = false;
		usingResource = false;
	}

	@Override
	public void requireAccess(int id) throws RemoteException, NotBoundException, InterruptedException, StateMachineException {
		if(node.isCoordinator())
			System.out.println("Il nodo " + id + " ha richiesto l'utilizzo della risorsa");
			requestList.add(id);
	}

	public void manageRequests()
			throws RemoteException, NotBoundException, AccessException, InterruptedException, StateMachineException {
		if (requestList.size() >= 1 && !resourceHolded) {
			resourceHolded = true;
			int next = requestList.remove(0);
			MutualExceptionManager manager = (MutualExceptionManager) registry.lookup("MEM_" + next);
			System.out.println("Do il controllo della risorsa a " + next);
			manager.accessGranted();
			
			long start = System.currentTimeMillis();
			long end = System.currentTimeMillis();
			while (resourceIsStillBeingHolded() && end - start <= MAX_DELAY)
				end = System.currentTimeMillis();
			
			if (resourceIsStillBeingHolded())
				System.out.println("Sembra che il nodo " + next + " sia fallito mentre controllava la risorsa, gliela ritiro");
				manageFailure(next);
		}
	}

	@Override
	public void accessGranted() throws RemoteException, InterruptedException, StateMachineException, NotBoundException {
		if (node.isWaiting()) {
			System.out.println("Ricevuto il controllo della risorsa");
			usingResource = true;
			node.grantAccess();
		}
	}
	
	public void useResource(int coordinatorId) throws InterruptedException, StateMachineException, AccessException, RemoteException, NotBoundException {
		int waiting = random.nextInt(MAXTIME - MINTIME) + MINTIME;

		long start = System.currentTimeMillis();
		long end = System.currentTimeMillis();
		while (end - start <= waiting && !node.isDead())
			end = System.currentTimeMillis();
		
		if (!node.isDead())
			usingResource = false;
	}

	@Override
	public void returnAccess(int id) throws RemoteException, NotBoundException, InterruptedException, StateMachineException {
		System.out.println("La risorsa è stata restituita");
		resourceHolded = false;
	}
	
	public void requireResource(int coordinatorId) throws AccessException, RemoteException, NotBoundException, InterruptedException, StateMachineException {
		System.out.println("Richiedo la risorsa al coordinatore " + coordinatorId);
		MutualExceptionManager coordinator = (MutualExceptionManager) registry.lookup("MEM_" + coordinatorId);
		coordinator.requireAccess(node.getId());
	}
	
	private boolean resourceIsStillBeingHolded() {
		return resourceHolded;
	}

	private void manageFailure(int id) {
		resourceHolded = false;
	}
	
	public void freeResource(int coordinatorId) throws AccessException, RemoteException, NotBoundException, InterruptedException, StateMachineException {
		System.out.println("Restituisco il controllo della risorsa al coordinatore " + coordinatorId);
		MutualExceptionManager coordinator = (MutualExceptionManager) registry.lookup("MEM_" + coordinatorId);
		coordinator.returnAccess(node.getId());
	}

	public boolean isUsingResource() {
		return usingResource;
	}
}
