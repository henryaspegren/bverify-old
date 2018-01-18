package org.bverify.proofs;

import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;
import org.spongycastle.util.Arrays;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

public class RecordProof implements Proof {
	
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
		
		// Construct a proof by making a Merkle path to the record 
		// TODO: this is inefficient because makePruned potentially 
		//			adds an extra path! (always uses the tree.version, 
		//			which adds uncommitted records). We need to write 
		//			a new method to avoid this
		this.proofTree = recordTree.makePruned(this.newdatastore);
		
		this.proofTree.copyV(recordTree, recordNumber, true);
		
		// this is currently required because we need to be able to reproduce the 
		// current agg --- can be replaced in the future 
		this.proofTree.copyV(recordTree, commitmentRecordNumber, false);	
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
