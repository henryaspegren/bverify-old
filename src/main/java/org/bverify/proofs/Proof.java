package org.bverify.proofs;


public interface Proof {
	
	/**
	 * Get the size of the proof in Bytes 
	 * This will be useful for benchmarking and performance analaysis
	 * @return 
	 */
	public int getSizeInBytes();
	
}
