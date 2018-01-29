package org.bverify.proofs;

import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;
import org.spongycastle.util.Arrays;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

public class RecordProof implements Proof {
	
	private static final long serialVersionUID = 1L;
	private final int recordNumber;
	private final int commitmentNumber;
	private final int commitmentRecordNumber;
	private final HistoryTree<RecordAggregation, Record> proofTree;
	private final ArrayStore<RecordAggregation, Record> newdatastore;

	public RecordProof(int recordNumber, int commitmentNumber, 
			int commitmentRecordNumber, HistoryTree<RecordAggregation, Record> recordTree) throws ProofError {
		this.recordNumber = recordNumber;
		this.commitmentNumber = commitmentNumber;
		this.commitmentRecordNumber = commitmentRecordNumber;
		this.newdatastore = new ArrayStore<RecordAggregation, Record>();
		
		// fixed - proof now is minimal and optimal, no extra paths
		this.proofTree = recordTree.makePruned(this.newdatastore, commitmentRecordNumber);
				
		this.proofTree.copyV(recordTree, recordNumber, true);	
	}
	
	public Record getRecord() {
		return this.proofTree.leaf(this.recordNumber).getVal();
	}
	
	public boolean checkProof(byte[] commitmentHash) {
		// TODO: need to think carefully about whether an internal
		// consistency check of the history tree is required (depends on what gets saved)
		// and implement it. does depend on the serialization
		byte[] calculatedHashUsingMerklePath = this.proofTree.aggV(this.commitmentRecordNumber).getHash();
		
		// STEP 1 - check if the commitment matches the root of this tree
		boolean matches = Arrays.areEqual(commitmentHash, calculatedHashUsingMerklePath);
		return matches;
	}
	
	public int getRecordNumber() {
		return this.recordNumber;
	}

	public int getCommitmentNumber() {
		return this.commitmentNumber;
	}
	
	
	@Override
	public String toString() {
		String message = String.format("RecordProof - for record number: %d",
				this.getRecordNumber())+" and commitment num: "+
				this.getCommitmentNumber();
		return message;
	}
	
	@Override
	public int getSizeInBytes() {
		// TODO Auto-generated method stub
		return 0;
	}

}
