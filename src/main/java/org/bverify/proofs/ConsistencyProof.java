package org.bverify.proofs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

public class ConsistencyProof implements Proof {
	
	private final int startingCommitmentNumber;
	private final List<Integer> cmtRecordNumbers;
	private final HistoryTree<RecordAggregation, Record> proofTree;
	
	public ConsistencyProof(int startingCommitmentNumber,
			List<Integer> commitmentRecordNumbers, 
			HistoryTree<RecordAggregation,
			Record> tree) throws ProofError {
		this.startingCommitmentNumber = startingCommitmentNumber;
		this.cmtRecordNumbers = new ArrayList<Integer>();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
		// make a pruned tree
		// around the last commitment version
		int lastCmtRecordNumber = commitmentRecordNumbers.get(commitmentRecordNumbers.size()-1);
		this.proofTree = tree.makePruned(store, lastCmtRecordNumber);
	
		// add in the required paths to reproduce the 
		// previous commitments
		// TODO: this is inefficient - adds a lot more 
		// entires than what we need!
		for(int cmtRecordNumber : commitmentRecordNumbers) {
			cmtRecordNumbers.add(cmtRecordNumber);
			this.proofTree.copyV(tree, cmtRecordNumber, false);
		}
	}
	
	public boolean checkProof(List<byte[]> commitmentHashes) {
		if(commitmentHashes.size() != this.cmtRecordNumbers.size()) {
			return false;
		}
		int idx = 0;
		for( int cmtNumber : this.cmtRecordNumbers) {
			byte[] hashInProofTree = this.proofTree.aggV(cmtNumber).getHash();
			byte[] correctHash = commitmentHashes.get(idx);
			boolean hashesMatch = Arrays.equals(hashInProofTree, correctHash);
			if(!hashesMatch) {
				return false;
			}
			idx++;
		}
		return true;
	}
	
	public int getStartingCommitmentNumber() {
		return this.startingCommitmentNumber;
	}
	
	public int getEndingCommitmentNumber() {
		return this.startingCommitmentNumber + this.cmtRecordNumbers.size() - 1;
	}
	
	@Override
	public String toString() {
		String message = String.format("ConsistencyProof - for commitment numbers: "
				+ "%d - through  - %d", this.getStartingCommitmentNumber(), this.getEndingCommitmentNumber());
		return message;
	}
	
	@Override
	public int getSizeInBytes() {
		// TODO Auto-generated method stub
		return 0;
	}

}
