package org.bverify.proofs;

import java.util.Arrays;

import org.bverify.aggregators.RecordAggregation;

public class AggregationProof implements Proof {
	
	private final RecordAggregation mainAgg;
	private final byte[] leftPreImageHash;
	private final byte[] rightPreImageHash;
	private final int commitmentNumber;
	

	public AggregationProof(RecordAggregation main, 
			byte[] leftPreImageHash, byte[] rightPreImageHash,
			int commitmnetNumber) {
		this.mainAgg = main;
		this.leftPreImageHash = leftPreImageHash;
		this.rightPreImageHash = rightPreImageHash;
		this.commitmentNumber = commitmnetNumber;
	}
	
	public boolean checkProof(byte[] commitmentHash) {
		// STEP 1 - verify the final preimage 
		// 			correctly reproduces the hash
		byte[] hashRes = RecordAggregation.calculateHash(
				this.mainAgg.getTotalAmount(), this.mainAgg.getNetAmount(), 
				this.leftPreImageHash, this.rightPreImageHash);
		
		boolean hashPreimageValid = Arrays.equals(hashRes, this.mainAgg.getHash());
		
		if(!hashPreimageValid) {
			return false;
		}
		
		// STEP 2 - check if the commitmentHash matches the 
		//			the record aggregation
		boolean commitmentMatches = Arrays.equals(commitmentHash, this.mainAgg.getHash());
		
		return commitmentMatches;
		
	}
	
	public RecordAggregation getAggregation() {
		return this.mainAgg;
	}
	
	public int getCommitmentNumber() {
		return this.commitmentNumber;
	}

	@Override
	public int getSizeInBytes() {
		return 0;
	}
	
	@Override 
	public String toString() {
		String message = String.format("AggreationProof - for commitment number: %d",
				this.getCommitmentNumber())+" and aggregation: "+
				this.mainAgg.toString();
		return message;
		
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof AggregationProof) {
			AggregationProof otherProof = (AggregationProof) other;
			boolean matches = (Arrays.equals(this.leftPreImageHash, otherProof.leftPreImageHash) &&
					Arrays.equals(this.rightPreImageHash, otherProof.rightPreImageHash) &&
					(this.commitmentNumber == otherProof.commitmentNumber) &&
					(this.mainAgg.equals(otherProof.mainAgg)));
			return matches;
		}
		return false;
	}

}
