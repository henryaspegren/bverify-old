package org.bverify.bverify;

import java.util.ArrayList;
import java.util.List;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.proofs.AggregationProof;
import org.bverify.proofs.ConsistencyProof;
import org.bverify.proofs.RecordProof;
import org.bverify.records.Record;
import org.junit.Assert;
import org.junit.Test;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

public class BVerifyServerTest extends BVerifyClientServerTest {
	
	@Test
	public void testRecordAggregationProof() {
		try {
			BVerifyServer bverifyserver = new BVerifyServer(catenaServer);
		    bverifyserver.addRecord(deposit); 				// 0 \	
		    bverifyserver.addRecord(deposit);				// 1 /\
		    bverifyserver.addRecord(transfer);				// 2 \ \	 
			// 											-----	   /
		    bverifyserver.addRecord(deposit);				// 3 /
		    bverifyserver.addRecord(deposit);				// 4 \
			bverifyserver.addRecord(withdrawal); 			// 5 /
			// 											-----
			bverifyserver.addRecord(withdrawal);			// 6
			bverifyserver.addRecord(deposit); 				// 7
			bverifyserver.addRecord(transfer); 				// 8
			// 											-----
			bverifyserver.addRecord(transfer); 				// 9
			bverifyserver.addRecord(transfer); 				// 10
			
			
			// check agg proof for commitment 0
			CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
			RecordAggregation leftAgg0 = aggregator.aggChildren(aggregator.aggVal(deposit), aggregator.aggVal(deposit));
			RecordAggregation rightAgg0 = aggregator.aggChildren(aggregator.aggVal(transfer), aggregator.emptyAgg());
			RecordAggregation mainAgg0 = aggregator.aggChildren(leftAgg0, rightAgg0);
			AggregationProof correctAggProofCommitmentZero = new AggregationProof(
					mainAgg0, leftAgg0.getHash(), rightAgg0.getHash(), 0);
			

			AggregationProof aggProofZero = bverifyserver.constructAggregationProof(0);
				
			Assert.assertEquals(correctAggProofCommitmentZero, aggProofZero);
			
			// check agg proof for commitment 1
			RecordAggregation leftAgg1 = aggregator.aggChildren(
					leftAgg0, aggregator.aggChildren(aggregator.aggVal(transfer), aggregator.aggVal(deposit)));
			RecordAggregation rightAgg1 = aggregator.aggChildren(
					aggregator.aggChildren(aggregator.aggVal(deposit), aggregator.aggVal(withdrawal)),
					aggregator.emptyAgg());
			RecordAggregation mainAgg1 = aggregator.aggChildren(leftAgg1, rightAgg1);
			AggregationProof correctAggProofCommitmentOne = new AggregationProof(
					mainAgg1, leftAgg1.getHash(), rightAgg1.getHash(), 1);
			
			AggregationProof aggProofOne = bverifyserver.constructAggregationProof(1);
			
			Assert.assertEquals(correctAggProofCommitmentOne, aggProofOne);
			
		}catch(InsufficientMoneyException e) {
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testServerRecordProof() {
		try {
			BVerifyServer bverifyserver = new BVerifyServer(catenaServer);
		    bverifyserver.addRecord(deposit); 				// 0
		    bverifyserver.addRecord(deposit);				// 1
		    bverifyserver.addRecord(transfer);				// 2
			// 											-----
		    bverifyserver.addRecord(deposit);				// 3
		    bverifyserver.addRecord(deposit);				// 4
			bverifyserver.addRecord(withdrawal); 			// 5
			// 											-----
			bverifyserver.addRecord(withdrawal);			// 6
			bverifyserver.addRecord(deposit); 				// 7
			bverifyserver.addRecord(transfer); 				// 8
			// 											-----
			bverifyserver.addRecord(transfer); 				// 9
			bverifyserver.addRecord(transfer); 				// 10
			
			RecordProof proof = bverifyserver.constructRecordProof(4);
			
			// check that the proof is constructed for the right record
			// and is valid
			Assert.assertEquals(deposit, proof.getRecord()); 
			Assert.assertEquals(2, proof.getCommitmentNumber());
			Assert.assertTrue(proof.checkProof(bverifyserver.getCommitment(2)));
			
		}catch(InsufficientMoneyException | ProofError e) {
			e.printStackTrace();
			Assert.fail();
		}
	}

	
	@Test
	public void testServerCommitments(){
		try {
			BVerifyServer bverifyserver;
			
		    bverifyserver = new BVerifyServer(catenaServer);
		    bverifyserver.addRecord(deposit); 				// 0 
		    bverifyserver.addRecord(deposit);				// 1
		    bverifyserver.addRecord(transfer);				// 2
			// 											-----
		    bverifyserver.addRecord(deposit);				// 3
		    bverifyserver.addRecord(deposit);				// 4
			bverifyserver.addRecord(withdrawal); 			// 5
			// 											-----
			bverifyserver.addRecord(withdrawal);			// 6
			bverifyserver.addRecord(deposit); 				// 7
			bverifyserver.addRecord(transfer); 				// 8
			// 											-----
			bverifyserver.addRecord(transfer); 				// 9
			bverifyserver.addRecord(transfer); 				// 10
			
	        CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
			ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation,Record>();    
			HistoryTree<RecordAggregation, Record> hisstree  = new HistoryTree<RecordAggregation, Record>(aggregator, store);


			hisstree.append(deposit);
			hisstree.append(deposit);
			hisstree.append(transfer);
			byte[] hashCommitZero = hisstree.agg().getHash();

			
			hisstree.append(deposit);
			hisstree.append(deposit);
			hisstree.append(withdrawal);
			byte[] hashCommitOne = hisstree.agg().getHash();
			
			hisstree.append(withdrawal);
			hisstree.append(deposit);
			hisstree.append(transfer);
			byte[] hashCommitTwo = hisstree.agg().getHash();
		
			Assert.assertEquals(11, bverifyserver.getTotalNumberOfRecords());
			Assert.assertEquals(9, bverifyserver.getTotalNumberOfCommittedRecords());
			Assert.assertEquals(3, bverifyserver.getTotalNumberOfCommitments());
			
			Assert.assertEquals(2, bverifyserver.commitmentHashToVersion(hashCommitZero));
			Assert.assertEquals(5, bverifyserver.commitmentHashToVersion(hashCommitOne));
			Assert.assertEquals(8, bverifyserver.commitmentHashToVersion(hashCommitTwo));

			ConsistencyProof proof = bverifyserver.constructConsistencyProof(0, 2);
			
			Assert.assertEquals(0, proof.getStartingCommitmentNumber());
			Assert.assertEquals(2, proof.getEndingCommitmentNumber());
			
			List<byte[]> hashCommits = new ArrayList<byte[]>();
			hashCommits.add(hashCommitZero);
			hashCommits.add(hashCommitOne);
			hashCommits.add(hashCommitTwo);
			Assert.assertTrue(proof.checkProof(hashCommits));
			
		}catch(ProofError | InsufficientMoneyException e) {
			e.printStackTrace();
			Assert.fail();
		}
	};
	
}
