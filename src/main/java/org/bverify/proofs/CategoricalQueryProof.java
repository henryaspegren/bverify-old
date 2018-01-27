package org.bverify.proofs;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.util.Arrays;
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
 * @author henryaspegren
 *
 */
public class CategoricalQueryProof implements Proof {

	private final HistoryTree<RecordAggregation, Record> proofTree;
	private CategoricalAttributes filter;
	private final List<Integer> matchingRecordNumbers;
	
	
	private final int commitmentNumber;
	private final int recordCommitmentNumber;
	
	public CategoricalQueryProof(CategoricalAttributes filter, 
			HistoryTree<RecordAggregation, Record> treeRep, 
			int commitmentNumber, int recordCommitmentNumber) throws ProofError {
		
		this.filter = new CategoricalAttributes(filter);
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation,
				Record>();
		this.commitmentNumber = commitmentNumber;
		this.recordCommitmentNumber = recordCommitmentNumber;
		this.matchingRecordNumbers = new ArrayList<Integer>();
		this.proofTree = treeRep.makePruned(store, this.recordCommitmentNumber);
		
		for(int recordNum = 0; recordNum <= recordCommitmentNumber; recordNum++) {
			NodeCursor<RecordAggregation, Record> cursor = treeRep.leaf(recordNum);
			RecordAggregation agg = cursor.getAgg();
			Record rec = cursor.getVal();
			if(rec == null || agg == null) {
				throw new ProofError("Missing values in Tree");
			}
			// if the attribute has the filter, we need to add it to the tree
			if(agg.hasCategoricalAttributes(filter)) {
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
		if(!Arrays.areEqual(commitmentHash, aggHash)) {
			return false;
		}
		// Now we need to walk the tree and check that stubs 
		// do not match the filter 
		boolean queryIsComplete = this.proofTree.checkStubs(agg -> 
			{
				// all the stubs must not match the categorical attributes
				// (if they did than a record matching the query was
				//		omitted)
				return !agg.hasCategoricalAttributes(filter);
			});				 	 
		if(!queryIsComplete) {
			return false;
		}
		
		// If the query is complete we check that none of the 
		// records in the tree are omitted
		List<Integer> records = this.proofTree.getValueIndicies(agg -> 
		{
				return agg.hasCategoricalAttributes(filter);
		});
		if(!records.equals(this.matchingRecordNumbers)) {
			return false;
		}
		
		//  NOTE: (when we construct the tree from the serialization we 
		// 	re-calculate the internal hashes, so we don't need to worry
		// 	about checking them)
		return true;
	}
	
	public List<Integer> getRecordNumbers(){
		return new ArrayList<Integer>(this.matchingRecordNumbers);
		
	}
	
	public List<Record> getRecords(){
		List<Record> res=  new ArrayList<Record>();
		for(int idx: this.matchingRecordNumbers) {
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
		// TODO Auto-generated method stub
		return 0;
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
	 * @param newFilterAtts
	 */
	public void swapOutCategoricalAttributes(CategoricalAttributes newFilter) {
		this.filter = new CategoricalAttributes(newFilter);
	}
	
}
