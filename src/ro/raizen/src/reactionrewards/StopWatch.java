package ro.raizen.src.reactionrewards;

public class StopWatch {
	private long startTime = 0;
	private long stopTime = 0;
	
	public void start() {
		startTime = System.currentTimeMillis();
	}
	
	public void stop() {
		stopTime = System.currentTimeMillis();
	}
	
	public void reset() {
		startTime = 0;
		stopTime = 0;
	}
	
	public long getTime() {
		return (stopTime - startTime);
	}
	
}
