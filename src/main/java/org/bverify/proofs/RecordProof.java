package org.bverify.proofs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.lang3.SerializationUtils;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;
import org.spongycastle.util.Arrays;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

public class RecordProof implements Proof {
	
	private static final long serialVersionUID = 1L;
	private int recordNumber;
	private int commitmentNumber;
	private int commitmentRecordNumber;
	private HistoryTree<RecordAggregation, Record> proofTree;

	public RecordProof(int recordNumber, int commitmentNumber, 
			int commitmentRecordNumber, HistoryTree<RecordAggregation, Record> recordTree) throws ProofError {
		this.recordNumber = recordNumber;
		this.commitmentNumber = commitmentNumber;
		this.commitmentRecordNumber = commitmentRecordNumber;
		ArrayStore<RecordAggregation, Record> newdatastore = new ArrayStore<RecordAggregation, Record>();
		
		// fixed - proof now is minimal and optimal, no extra paths
		this.proofTree = recordTree.makePruned(newdatastore, commitmentRecordNumber);
				
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
		return SerializationUtils.serialize(this).length;
	}
	

	/**
	 * Special Serilization is needed so we don't send the whole proof tree.
	 * Any value that can be recalculated on the client side 
	 * is committed - this reduces the space required for the proof AND 
	 * makes sure that the client recomputes the values to check the validity 
	 * of the proof.
	 * @param oos
	 * @throws IOException
	 */
	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.writeObject(this.recordNumber);
		oos.writeObject(this.commitmentNumber);
		oos.writeObject(this.commitmentRecordNumber);
		// now we don't want to send the actual proof tree 
		// because it contains values that will be recalculated on the client side 
		oos.writeObject(this.proofTree.serializeTree());
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		this.recordNumber = (int) ois.readObject();
		this.commitmentNumber = (int) ois.readObject();
		this.commitmentRecordNumber = (int) ois.readObject();
		
		// read in the serialized tree 
		this.proofTree = new HistoryTree<RecordAggregation, Record>(
				new CryptographicRecordAggregator(), new ArrayStore<RecordAggregation, Record>());
		this.proofTree.parseTree((byte[]) ois.readObject());
		
	}

}
