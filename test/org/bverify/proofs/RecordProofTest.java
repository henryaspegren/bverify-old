package org.bverify.proofs;

import org.apache.commons.lang3.SerializationUtils;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;
import org.junit.Assert;
import org.junit.Test;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

public class RecordProofTest extends ProofTestBase{

	
	@Test 
	public void testValidRecordProof(){
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
		HistoryTree<RecordAggregation, Record> tree = new HistoryTree<RecordAggregation, Record>(cgr, store);
		tree.append(deposit);				// 0
		tree.append(withdrawal);			// 1
		tree.append(transfer);				// 2
		tree.append(deposit);				// 3
		tree.append(deposit);				// 4
		try {
			RecordProof validProof = new RecordProof(1, 0, 4, tree);
			Assert.assertEquals(validProof.getRecordNumber(), 1);
			Assert.assertEquals(validProof.getRecord(), withdrawal);
			Assert.assertEquals(validProof.getCommitmentNumber(), 0);
			Assert.assertTrue(validProof.checkProof(tree.aggV(4).getHash()));
		} catch (ProofError e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test 
	public void testInvalidRecordProofWrongCommitment() {
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
		HistoryTree<RecordAggregation, Record> tree = new HistoryTree<RecordAggregation, Record>(cgr, store);
		tree.append(deposit);				// 0
		tree.append(withdrawal);			// 1
		tree.append(transfer);				// 2
		tree.append(deposit);				// 3
		tree.append(deposit);				// 4
		try {
			RecordProof validProof = new RecordProof(1, 0, 4, tree);
			Assert.assertFalse(validProof.checkProof(tree.aggV(3).getHash()));
		} catch (ProofError e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test public void testInvalidRecordProofBadHistoryTree() {
		// TODO: need to find a way to implement this
	}
	
	@Test 
	public void testRecordProofSerializationValid(){
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
		HistoryTree<RecordAggregation, Record> tree = new HistoryTree<RecordAggregation, Record>(cgr, store);
		tree.append(deposit);				// 0
		tree.append(withdrawal);			// 1
		tree.append(transfer);				// 2
		tree.append(deposit);				// 3
		tree.append(deposit);				// 4
		try {
			RecordProof validProof = new RecordProof(1, 0, 4, tree);
			byte[] asBytes = SerializationUtils.serialize(validProof);
			RecordProof fromBytes = (RecordProof) SerializationUtils.deserialize(asBytes);
			Assert.assertEquals(fromBytes.getRecordNumber(), 1);
			Assert.assertEquals(fromBytes.getRecord(), withdrawal);
			Assert.assertEquals(fromBytes.getCommitmentNumber(), 0);
			Assert.assertTrue(fromBytes.checkProof(tree.aggV(4).getHash()));
		} catch (ProofError e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	
}
