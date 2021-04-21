package unipr.dia.sd.assegnamento2.threads;

import java.util.Random;

import unipr.dia.sd.assegnamento2.Node;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public class ResourceEventToss extends Thread{
	private static final double K = 0.1;
	private static final long DELAY = 1000;
	
	private Node node;
	private Random random;
	
	public ResourceEventToss(Node node) {
		this.node = node;
		this.random = new Random();
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				if (random.nextFloat() <= K) 
					node.accessResource();
				Thread.sleep(DELAY);
			}	
		} catch (StateMachineException | InterruptedException e) {
			System.out.println(e);
		}
	}
}
