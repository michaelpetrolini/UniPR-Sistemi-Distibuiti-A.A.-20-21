package assegnamento1.sync;

import java.util.Random;

public class ConcurrentRandom {
	private Random random;
	
	public ConcurrentRandom(int seed) {
		this.random = new Random(seed);
	}
	
	public synchronized float nextFloat() {
		return random.nextFloat();
	}
}
