package org.bverify.proofs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

/**
 * Proves that a series of commitments are <i>historically consistent</i>
 * with one another - that is that records have only been added (append-only).
 * @author henryaspegren
 *
 */
public class ConsistencyProof implements Proof {
	
	private static final long serialVersionUID = 1L;
	private int startingCommitmentNumber;
	private List<Integer> cmtRecordNumbers;
	private HistoryTree<RecordAggregation, Record> proofTree;
	
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
		oos.writeObject(this.startingCommitmentNumber);
		oos.writeObject(this.cmtRecordNumbers);
		// now we don't want to send the actual proof tree 
		// because it contains values that will be recalculated on the client side 
		oos.writeObject(this.proofTree.serializeTree());
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		this.startingCommitmentNumber = (int) ois.readObject();
		this.cmtRecordNumbers = (List<Integer>) ois.readObject();
		// read in the serialized tree 
		this.proofTree = new HistoryTree<RecordAggregation, Record>(
				new CryptographicRecordAggregator(), new ArrayStore<RecordAggregation, Record>());
		this.proofTree.parseTree((byte[]) ois.readObject());
	}

}
