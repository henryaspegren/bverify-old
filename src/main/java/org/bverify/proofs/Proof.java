package org.bverify.proofs;

import java.io.Serializable;

public interface Proof extends Serializable {
	
	/**
	 * Get the size of the proof in Bytes 
	 * This will be useful for benchmarking
	 * @return 
	 */
	public int getSizeInBytes();
	
}
