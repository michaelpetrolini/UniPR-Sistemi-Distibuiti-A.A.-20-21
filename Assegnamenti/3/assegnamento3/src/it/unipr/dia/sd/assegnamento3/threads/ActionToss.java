package it.unipr.dia.sd.assegnamento3.threads;

import java.util.Random;

import it.unipr.dia.sd.assegnamento3.Client;
import it.unipr.dia.sd.assegnamento3.Client.ActionType;
import it.unipr.dia.sd.assegnamento3.statemachine.StateMachineException;

public class ActionToss extends Thread {
	private static final double REQUEST_PROB = 0.005;
	private static final double WRITE_PROB = 0.5;
	private static final int DELAY = 100;
	
	private Client client;
	private Random random;
	
	public ActionToss(Client client) {
		this.client = client;
		this.random = new Random();
	}
	
	@Override
	public void run() {
		try {
			while (true) {
				if (random.nextFloat() <= REQUEST_PROB) {
					if (random.nextFloat() <= WRITE_PROB) {
						client.action(ActionType.WRITE);
					} else {
						client.action(ActionType.READ);
					}
				}
				Thread.sleep(DELAY);
			}
		} catch (StateMachineException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
