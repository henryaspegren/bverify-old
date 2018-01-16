package org.bverify.bverify;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.bitcoinj.core.InsufficientMoneyException;
import org.junit.Assert;
import org.junit.Test;

import edu.rice.historytree.ProofError;

public class BVerifyClientTest extends BVerifyClientServerTest {
	
	
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

	
}
