package edu.buffalo.cse562.data;

public interface Buffer {
	
	public Tuple readTuple();
	public void reset();
	public boolean isDone();
}