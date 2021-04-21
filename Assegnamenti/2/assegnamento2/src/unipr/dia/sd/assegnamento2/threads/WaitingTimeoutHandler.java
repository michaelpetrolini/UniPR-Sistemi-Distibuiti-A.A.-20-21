package unipr.dia.sd.assegnamento2.threads;

import unipr.dia.sd.assegnamento2.Node;
import unipr.dia.sd.assegnamento2.mutualexception.MutualExceptionManagerImpl;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public class WaitingTimeoutHandler extends Thread{
	
	private Node node;
	private MutualExceptionManagerImpl manager;
	private int timeout;
	
	public WaitingTimeoutHandler(Node node, MutualExceptionManagerImpl manager, int timeout) {
		this.node = node;
		this.manager = manager;
		this.timeout = timeout;
	}

	@Override
	public void run() {
		try {
			Thread.sleep(timeout);
			if(!manager.isUsingResource())
				node.failureDetected();
		} catch (InterruptedException | StateMachineException e) {
			e.printStackTrace();
		}
	}
}
