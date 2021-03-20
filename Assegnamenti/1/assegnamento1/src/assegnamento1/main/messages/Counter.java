package assegnamento1.main.messages;

public class Counter {
	private int counter;
	
	public Counter() {
		this.counter = 0;
	}
	
	public synchronized int getCounter() {
		return this.counter;
	}
	
	public synchronized int getIncrementedCounter() {
		return ++this.counter;
	}
	
	public synchronized void incrementCounter() {
		this.counter++;
	}
	
	public synchronized int setCounter(int set) {
		int diff = counter - set;
		this.counter = set;
		return diff;
	}
}
