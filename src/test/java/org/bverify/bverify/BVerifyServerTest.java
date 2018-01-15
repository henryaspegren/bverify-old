package org.bverify.bverify;

import java.util.Arrays;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

public class BVerifyServerTest extends BVerifyClientServerTest {
	
	public static BVerifyServer bverifyserver;
	

	@Before
    public void getReady() {
		try {
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
		}catch(InsufficientMoneyException e) {
			Assert.fail();
		}
		
	};
	
	@Test
	public void testServerCommitments(){
		try {
			
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
			
		}catch(ProofError e) {
			e.printStackTrace();
			Assert.fail();
		}
	};
	
}
