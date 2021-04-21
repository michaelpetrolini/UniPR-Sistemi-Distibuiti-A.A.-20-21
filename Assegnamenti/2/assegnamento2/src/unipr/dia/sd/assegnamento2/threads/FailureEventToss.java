package unipr.dia.sd.assegnamento2.threads;

import java.util.Random;

import unipr.dia.sd.assegnamento2.Node;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public class FailureEventToss extends Thread{
	
	private static final double H = -1;
	private static final long DELAY = 1000;
	
	private Node node;
	private Random random;
	
	public FailureEventToss(Node node) {
		this.node = node;
		this.random = new Random();
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				if (random.nextFloat() <= H) 
					node.failed();
				Thread.sleep(DELAY);
			}	
		} catch (StateMachineException | InterruptedException e) {
			System.out.println(e);
		}
	}
}
