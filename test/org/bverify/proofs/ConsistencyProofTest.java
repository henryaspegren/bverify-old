package org.bverify.proofs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.SerializationUtils;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;
import org.junit.Assert;
import org.junit.Test;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

public class ConsistencyProofTest extends ProofTestBase {

	@Test
	public void testConsistencyProofCorrect() {
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
		HistoryTree<RecordAggregation, Record> tree = new HistoryTree<RecordAggregation, Record>(cgr, store);
		tree.append(deposit);				// 0
		tree.append(withdrawal);			// 1
		tree.append(transfer);				// 2
		tree.append(deposit);				// 3
		tree.append(deposit);				// 4
		tree.append(withdrawal);			// 5
		tree.append(withdrawal);			// 6
		tree.append(withdrawal);			// 7
		tree.append(transfer);				// 8
		tree.append(transfer);				// 9

		try {
			
			List<Integer> commitmentRecordNumbers = new ArrayList<Integer>();
			List<byte[]> correctCommitmentHashes = new ArrayList<byte[]>();
			
			commitmentRecordNumbers.add(3);
			correctCommitmentHashes.add(tree.aggV(3).getHash());
			
			commitmentRecordNumbers.add(5);
			correctCommitmentHashes.add(tree.aggV(5).getHash());

			commitmentRecordNumbers.add(9);
			correctCommitmentHashes.add(tree.aggV(9).getHash());

			
			ConsistencyProof proof = new ConsistencyProof(0, commitmentRecordNumbers, tree);
			
			Assert.assertEquals(0, proof.getStartingCommitmentNumber());
			Assert.assertEquals(2, proof.getEndingCommitmentNumber());
			
			Assert.assertTrue(proof.checkProof(correctCommitmentHashes));

		} catch (ProofError e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	
	@Test
	public void testConsistencyProofWrongCommitmentHash() {
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
		HistoryTree<RecordAggregation, Record> tree = new HistoryTree<RecordAggregation, Record>(cgr, store);
		tree.append(deposit);				// 0
		tree.append(withdrawal);			// 1
		tree.append(transfer);				// 2
		tree.append(deposit);				// 3
		try {
			
			List<Integer> commitmentRecordNumbers = new ArrayList<Integer>();
			List<byte[]> correctCommitmentHashes = new ArrayList<byte[]>();
			
			commitmentRecordNumbers.add(3);
			// put the wrong hash in
			correctCommitmentHashes.add(tree.aggV(2).getHash());
			ConsistencyProof proof = new ConsistencyProof(0, commitmentRecordNumbers, tree);
			
			Assert.assertFalse(proof.checkProof(correctCommitmentHashes));

		} catch (ProofError e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testConsistencyProofCorrectSerialization() {
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
		HistoryTree<RecordAggregation, Record> tree = new HistoryTree<RecordAggregation, Record>(cgr, store);
		tree.append(deposit);				// 0
		tree.append(withdrawal);			// 1
		tree.append(transfer);				// 2
		tree.append(deposit);				// 3
		tree.append(deposit);				// 4
		tree.append(withdrawal);			// 5
		tree.append(withdrawal);			// 6
		tree.append(withdrawal);			// 7
		tree.append(transfer);				// 8
		tree.append(transfer);				// 9

		try {
			
			List<Integer> commitmentRecordNumbers = new ArrayList<Integer>();
			List<byte[]> correctCommitmentHashes = new ArrayList<byte[]>();
			
			commitmentRecordNumbers.add(3);
			correctCommitmentHashes.add(tree.aggV(3).getHash());
			
			commitmentRecordNumbers.add(5);
			correctCommitmentHashes.add(tree.aggV(5).getHash());

			commitmentRecordNumbers.add(9);
			correctCommitmentHashes.add(tree.aggV(9).getHash());

			
			ConsistencyProof proof = new ConsistencyProof(0, commitmentRecordNumbers, tree);
			byte[] proofAsBytes = SerializationUtils.serialize(proof);
			ConsistencyProof proofFromBytes = (ConsistencyProof) SerializationUtils.deserialize(proofAsBytes);
			
			Assert.assertEquals(0, proofFromBytes.getStartingCommitmentNumber());
			Assert.assertEquals(2, proofFromBytes.getEndingCommitmentNumber());
			Assert.assertTrue(proofFromBytes.checkProof(correctCommitmentHashes));

		} catch (ProofError e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testConsistencyProofInvalidHistoryTree() {
		// TODO: think about how to test this
	}
	
}
