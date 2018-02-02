package org.bverify.proofs;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.Arrays;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.CategoricalAttributes;
import org.bverify.records.Record;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.NodeCursor;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

/**
 * This is a proof for a search for records based on categorical attributes.
 * Returns all records that <b>have at least</b> these attributes.
 * 
 * @author henryaspegren
 *
 */
public class CategoricalQueryProof implements Proof {

	private static final long serialVersionUID = 1L;
	private HistoryTree<RecordAggregation, Record> proofTree;
	private CategoricalAttributes filter;
	
	// this list of matching records does not actually 
	// need to be sent to the client!
	private List<Integer> matchingRecordNumbers;

	private int commitmentNumber;
	private int recordCommitmentNumber;

	public CategoricalQueryProof(CategoricalAttributes filter, HistoryTree<RecordAggregation, Record> treeRep,
			int commitmentNumber, int recordCommitmentNumber) throws ProofError {

		this.filter = new CategoricalAttributes(filter);
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
		this.commitmentNumber = commitmentNumber;
		this.recordCommitmentNumber = recordCommitmentNumber;
		this.matchingRecordNumbers = new ArrayList<Integer>();
		this.proofTree = treeRep.makePruned(store, this.recordCommitmentNumber);
		for (int recordNum = 0; recordNum <= recordCommitmentNumber; recordNum++) {
			NodeCursor<RecordAggregation, Record> cursor = treeRep.leaf(recordNum);
			RecordAggregation agg = cursor.getAgg();
			Record rec = cursor.getVal();
			if (rec == null || agg == null) {
				throw new ProofError("Missing values in Tree");
			}
			// if the attribute has the filter, we need to add it to the tree
			if (agg.hasCategoricalAttributes(filter)) {
				// copy that record along with the path to the root into the tree
				this.proofTree.copyV(treeRep, recordNum, true);
				// also keep track of that record number
				this.matchingRecordNumbers.add(recordNum);
			}
		}
	}

	public boolean checkProof(byte[] commitmentHash) {
		// First - check if the commitment hash matches
		byte[] aggHash = this.proofTree.aggV(this.recordCommitmentNumber).getHash();
		if (!Arrays.areEqual(commitmentHash, aggHash)) {
			return false;
		}
		// Now we need to walk the tree and check that stubs
		// do not match the filter
		boolean queryIsComplete = this.proofTree.checkStubs(agg -> {
			// all the stubs must not match the categorical attributes
			// (if they did than a record matching the query was
			// omitted)
			return !agg.hasCategoricalAttributes(filter);
		});
		if (!queryIsComplete) {
			return false;
		}

		// If the query is complete we check that none of the
		// records in the tree are omitted
		List<Integer> records = this.proofTree.getValueIndicies(agg -> {
			return agg.hasCategoricalAttributes(filter);
		});
		if (!records.equals(this.matchingRecordNumbers)) {
			return false;
		}

		// NOTE: (when we construct the tree from the serialization we
		// re-calculate the internal hashes, so we don't need to worry
		// about checking them)
		return true;
	}

	public List<Integer> getRecordNumbers() {
		return new ArrayList<Integer>(this.matchingRecordNumbers);

	}

	public List<Record> getRecords() {
		List<Record> res = new ArrayList<Record>();
		for (int idx : this.matchingRecordNumbers) {
			Record rec = this.proofTree.leaf(idx).getVal();
			// returns a copy - for safety
			res.add(rec.deepCopy());
		}
		return res;
	}

	public int getCommitmentNumber() {
		return this.commitmentNumber;
	}

	@Override
	public int getSizeInBytes() {
		return this.proofTree.serializeTree().length;
	}
	

	@Override
	public String toString() {
		StringBuilder message = new StringBuilder();
		message.append("CategoricalQueryProof for filter: ");
		message.append(System.lineSeparator());
		message.append(this.filter);
		message.append(System.lineSeparator());
		message.append("Proof Tree:");
		message.append(this.proofTree);
		return message.toString();
	}
	

	/**
	 * FOR TESTING PURPOSES ONLY - Can create invalid proofs!
	 * 
	 * @param newFilterAtts
	 */
	public void swapOutCategoricalAttributes(CategoricalAttributes newFilter) {
		this.filter = new CategoricalAttributes(newFilter);
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
		oos.writeObject(this.filter);
		oos.writeObject(this.matchingRecordNumbers);
		oos.writeObject(this.recordCommitmentNumber);
		oos.writeObject(this.commitmentNumber);
		// now we don't want to send the actual proof tree 
		// because it contains values that will be recalculated on the client side 
		oos.writeObject(this.proofTree.serializeTree());
	}
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
		this.filter = (CategoricalAttributes) ois.readObject();
		this.matchingRecordNumbers = (List<Integer>) ois.readObject();
		this.recordCommitmentNumber = (int) ois.readObject();
		this.commitmentNumber = (int) ois.readObject();
		
		// read in the serialized tree 
		this.proofTree = new HistoryTree<RecordAggregation, Record>(
				new CryptographicRecordAggregator(), new ArrayStore<RecordAggregation, Record>());
		this.proofTree.parseTree((byte[]) ois.readObject());
		
	}
	
	public String proofTreeToString() {
		return this.proofTree.toString();
	}
	
}
