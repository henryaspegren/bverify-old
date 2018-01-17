package org.bverify.bverify;

import java.util.Arrays;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;
import org.junit.Assert;
import org.junit.Test;

import edu.rice.historytree.AggWithChildren;
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
			
			CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();

			AggWithChildren<RecordAggregation> aggProofOne = bverifyserver.constructAggregationProof(1);
			
			RecordAggregation leftAgg1 = aggregator.aggChildren(aggregator.aggVal(deposit), aggregator.aggVal(deposit));
			RecordAggregation rightAgg1 = aggregator.aggChildren(aggregator.aggVal(transfer), aggregator.emptyAgg());
			RecordAggregation mainAgg1 = aggregator.aggChildren(leftAgg1, rightAgg1);
			
			Assert.assertEquals(leftAgg1, aggProofOne.getLeft());
			Assert.assertEquals(rightAgg1, aggProofOne.getRight());
			Assert.assertEquals(mainAgg1, aggProofOne.getMain());
			
			AggWithChildren<RecordAggregation> aggProofTwo = bverifyserver.constructAggregationProof(2);
			
			RecordAggregation leftAgg2 = aggregator.aggChildren(
					leftAgg1, aggregator.aggChildren(aggregator.aggVal(transfer), aggregator.aggVal(deposit)));
			
			RecordAggregation rightAgg2 = aggregator.aggChildren(
					aggregator.aggChildren(aggregator.aggVal(deposit), aggregator.aggVal(withdrawal)),
					aggregator.emptyAgg());
			RecordAggregation mainAgg2 = aggregator.aggChildren(leftAgg2, rightAgg2);
			
			Assert.assertEquals(leftAgg2, aggProofTwo.getLeft());
			Assert.assertEquals(rightAgg2, aggProofTwo.getRight());
			Assert.assertEquals(mainAgg2, aggProofTwo.getMain());
			
			
			
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
			
			HistoryTree<RecordAggregation, Record> proofTree = bverifyserver.constructRecordProof(4);
			
			Assert.assertEquals(deposit, proofTree.leaf(4).getVal()); 
			RecordAggregation agg = proofTree.aggV(8);
			Assert.assertTrue(Arrays.equals(bverifyserver.getCommitment(3), agg.getHash()));
			
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
			byte[] hashCommitOne = hisstree.agg().getHash();

			
			hisstree.append(deposit);
			hisstree.append(deposit);
			hisstree.append(withdrawal);
			byte[] hashCommitTwo = hisstree.agg().getHash();
			
			hisstree.append(withdrawal);
			hisstree.append(deposit);
			hisstree.append(transfer);
			byte[] hashCommitThree = hisstree.agg().getHash();
		
			Assert.assertEquals(11, bverifyserver.getTotalNumberOfRecords());
			Assert.assertEquals(9, bverifyserver.getTotalNumberOfCommittedRecords());
			Assert.assertEquals(3, bverifyserver.getTotalNumberOfCommitments());
			
			Assert.assertEquals(2, bverifyserver.commitmentHashToVersion(hashCommitOne));
			Assert.assertEquals(5, bverifyserver.commitmentHashToVersion(hashCommitTwo));
			Assert.assertEquals(8, bverifyserver.commitmentHashToVersion(hashCommitThree));

			HistoryTree<RecordAggregation, Record> proof = bverifyserver.constructConsistencyProof(1, 3);
			
			Assert.assertTrue(Arrays.equals(hashCommitOne, proof.aggV(2).getHash()));
			Assert.assertTrue(Arrays.equals(hashCommitTwo, proof.aggV(5).getHash()));
			Assert.assertTrue(Arrays.equals(hashCommitThree, proof.aggV(8).getHash()));			
			
		}catch(ProofError | InsufficientMoneyException e) {
			e.printStackTrace();
			Assert.fail();
		}
	};
	
}
