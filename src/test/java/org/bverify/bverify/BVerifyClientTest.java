package org.bverify.bverify;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bverify.records.Record;
import org.bverify.records.RecordUtils;
import org.bverify.records.Transfer;
import org.junit.Assert;
import org.junit.Test;

import edu.rice.historytree.ProofError;

public class BVerifyClientTest extends BVerifyClientServerTest {
	
	
	@Test
	public void testRecordProofSuccess() {
		try {
			BVerifyServer bverifyserver = new BVerifyServer(this.catenaServer);
	        Semaphore semAppended = new Semaphore(0);
	        this.createSemaphoredCatenaClient(this.txid, semAppended, null);
			BVerifyClient bverifyclient = new BVerifyClient(this.catenaClient, bverifyserver);
			// add three records to get a commitment 
			bverifyserver.addRecord(deposit);
			bverifyserver.addRecord(withdrawal);
			bverifyserver.addRecord(transfer);
			waitForBlock();
	        waitForStatements(1, semAppended);
	        bverifyclient.loadStatements();
	        bverifyclient.verifyConsistency();
	        Record record = bverifyclient.getAndVerifyRecord(2);
	        Assert.assertEquals(withdrawal, record);
	        
		}catch(InsufficientMoneyException | IOException | InterruptedException | ProofError e){
			e.printStackTrace();
			Assert.fail();
		}
	}
	
	@Test
	public void testDetectTamperedRecordProof() {
		try {
			BVerifyServer bverifyserver = new BVerifyServer(this.catenaServer);
	        Semaphore semAppended = new Semaphore(0);
	        this.createSemaphoredCatenaClient(this.txid, semAppended, null);
			BVerifyClient bverifyclient = new BVerifyClient(this.catenaClient, bverifyserver);
			// add three records to get a commitment 
			bverifyserver.addRecord(deposit);
			bverifyserver.addRecord(withdrawal);
			bverifyserver.addRecord(transfer);
			waitForBlock();
	        waitForStatements(1, semAppended);
	        
	        bverifyclient.loadStatements();
	        bverifyclient.verifyConsistency();
	        
	        // modify the record on the server
	        Record modifiedRecord = RecordUtils.modifyDepositAmount(deposit, 123456789);
	        bverifyserver.changeRecord(2, modifiedRecord);
	        
	        Record record = bverifyclient.getAndVerifyRecord(2);
	        Assert.assertNotEquals(deposit, record);
	        Assert.fail("Invalid Record Not Caught!");
	        
		}catch(ProofError e){
			e.printStackTrace();
		}catch(InsufficientMoneyException | IOException | InterruptedException e) {
			
		}
	}
	
	@Test
	public void testConsistencyProofSuccess(){
		try {
			BVerifyServer bverifyserver = new BVerifyServer(this.catenaServer);
	        Semaphore semAppended = new Semaphore(0);
	        this.createSemaphoredCatenaClient(this.txid, semAppended, null);
			BVerifyClient bverifyclient = new BVerifyClient(this.catenaClient, bverifyserver);
			// add three records to get a commitment 
			bverifyserver.addRecord(deposit);
			bverifyserver.addRecord(deposit);
			bverifyserver.addRecord(deposit);
			waitForBlock();
	        waitForStatements(1, semAppended);
	        bverifyclient.loadStatements();
	        bverifyclient.verifyConsistency();
	        Assert.assertEquals(1, bverifyclient.totalCommitments());
	        Assert.assertEquals(1, bverifyclient.currentCommitment());
	        
	        bverifyserver.addRecord(withdrawal);
	        bverifyserver.addRecord(transfer);
	        bverifyserver.addRecord(transfer);
			waitForBlock();

	        // get another commitment
	        bverifyserver.addRecord(transfer);
	        bverifyserver.addRecord(transfer);
	        bverifyserver.addRecord(transfer);
			waitForBlock();
			
	        waitForStatements(2, semAppended);
	        bverifyclient.loadStatements();
	        Assert.assertEquals(3, bverifyclient.totalCommitments());
	        Assert.assertEquals(1, bverifyclient.currentCommitment());
	        bverifyclient.verifyConsistency();
	        Assert.assertEquals(3, bverifyclient.totalCommitments());
	        Assert.assertEquals(3, bverifyclient.currentCommitment());
		}
		catch(InsufficientMoneyException | IOException | InterruptedException | ProofError e){
			e.printStackTrace();
			Assert.fail();
		}

	}
	
	
	@Test
	public void testDetectInconsistencyClient(){
		try {
			BVerifyServer bverifyserver = new BVerifyServer(this.catenaServer);
	        Semaphore semAppended = new Semaphore(0);
	        this.createSemaphoredCatenaClient(this.txid, semAppended, null);
			BVerifyClient bverifyclient = new BVerifyClient(this.catenaClient, bverifyserver);
			// add three records to get a commitment 
			bverifyserver.addRecord(deposit);		// 1
			bverifyserver.addRecord(deposit);		// 2
			bverifyserver.addRecord(deposit);		// 3
			waitForBlock();
	        waitForStatements(1, semAppended);
	        bverifyclient.loadStatements();
	        bverifyclient.verifyConsistency();
	        Assert.assertEquals(1, bverifyclient.totalCommitments());
	        Assert.assertEquals(1, bverifyclient.currentCommitment());
	        
	        // add some records
	        bverifyserver.addRecord(withdrawal);	// 4
	        bverifyserver.addRecord(transfer);		// 5
	        bverifyserver.addRecord(transfer); 		// 6
	        
			waitForBlock();
			// now these records are committed
			
			// NOW MODIFY A PREVIOUS RECORD 
			Transfer modifiedTransfer = RecordUtils.modifyTransferAmount(transfer, 12345);
			bverifyserver.changeRecord(5, modifiedTransfer);

	        // add some more records
	        bverifyserver.addRecord(transfer);		// 7
	        bverifyserver.addRecord(transfer);		// 8
	        bverifyserver.addRecord(transfer);		// 9
	        
	        // commit them
			waitForBlock();
			
	        waitForStatements(2, semAppended);
	        
	        bverifyclient.loadStatements();
	        Assert.assertEquals(3, bverifyclient.totalCommitments());
	        Assert.assertEquals(1, bverifyclient.currentCommitment());
	        
	        // this should fail since there is inconsistency 
	        // - Record number 5 has been modified!
	        bverifyclient.verifyConsistency();
	        // if the inconsistency is not detected the test has failed
	        Assert.fail("Inconsistency not detected!");

		}
		catch(InsufficientMoneyException | IOException | InterruptedException | ProofError e){
			if(e instanceof ProofError) {
				System.out.println(e.getMessage());
				Assert.assertTrue(true);
			}
			else {
				Assert.fail();
			}
		}

	}
	


	
}
