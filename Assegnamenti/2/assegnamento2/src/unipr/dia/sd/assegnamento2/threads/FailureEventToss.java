package unipr.dia.sd.assegnamento2.threads;

import java.util.Random;

import unipr.dia.sd.assegnamento2.Node;
import unipr.dia.sd.assegnamento2.statemachine.StateMachineException;

public class FailureEventToss extends Thread{
	
	private static final double DEATHNODE = 0.002;
	private static final int MINTIME = 100;
	private static final int MAXTIME = 500;
	
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
				if (random.nextFloat() <= DEATHNODE && !node.isDead()) 
					node.failed();
				int delay = random.nextInt(MAXTIME - MINTIME) + MINTIME;
				Thread.sleep(delay);
			}	
		} catch (StateMachineException | InterruptedException e) {
			System.out.println(e);
		}
	}
}
