package unipr.dia.sd.assegnamento2.failures;

import java.util.Random;

import unipr.dia.sd.assegnamento2.Node;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public class WakingEventToss extends Thread{
	private static final double K = 0.2;
	private static final long DELAY = 1000;
	
	private Node node;
	private Random random;
	
	public WakingEventToss(Node node) {
		this.node = node;
		this.random = new Random();
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				if (random.nextFloat() <= K) 
					node.wakeUp();
				Thread.sleep(DELAY);
			}	
		} catch (StateMachineException | InterruptedException e) {
			System.out.println(e);
		}
	}
}
