package org.bverify.bverify;

import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.bitcoinj.core.InsufficientMoneyException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.rice.historytree.ProofError;

public class BVerifyClientTest extends BVerifyClientServerTest {
	
	public static BVerifyServer bverifyserver;
	public static BVerifyClient bverifyclient;
	
	private static Semaphore semAppended;
	
	@Before
	public void getReady() {
	    bverifyserver = new BVerifyServer(catenaServer);
	    try {
	        semAppended = new Semaphore(0);
			this.createSemaphoredCatenaClient(this.txid, semAppended, null);
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
			Assert.fail();
		}
	    bverifyclient = new BVerifyClient(catenaClient, bverifyserver);

	}

	
	@Test 
	public void testClientVerification(){
		try {
	        bverifyserver.addRecord(deposit);			// 1
	        this.waitForBlock();
	        this.waitForBlock();
	        this.waitForBlock();

	        this.waitForStatements(1, semAppended);
	        bverifyclient.loadStatements();
	        System.out.println(bverifyclient.totalCommitments());
	        System.out.println(bverifyclient.currentCommitment());
	        
	        bverifyserver.addRecord(deposit);			// 2
	        bverifyserver.addRecord(deposit);			// 3
	        
	        this.waitForStatements(1, semAppended);

	        //Assert.assertEquals(1,bverifyclient.totalCommitments());
	        //Assert.assertEquals(1,bverifyclient.currentCommitment());
	        
	        bverifyclient.verifyConsistency();
	        System.out.println(bverifyclient.totalCommitments());
	        System.out.println(bverifyclient.currentCommitment());
	        /*
	        //Assert.assertEquals(2,bverifyclient.totalCommitments());
	        //Assert.assertEquals(1,bverifyclient.currentCommitment());	
	        
	        bverifyserver.addRecord(deposit);			// 4
	        bverifyserver.addRecord(deposit);			// 5
	        bverifyserver.addRecord(deposit);			// 6
	        //											----
	        bverifyserver.addRecord(deposit);			// 7
	        bverifyserver.addRecord(deposit);			// 8
	        bverifyserver.addRecord(deposit);			// 9
	        //											----
	        bverifyserver.addRecord(deposit);			// 10


	        
	        bverifyclient.loadStatements();
	        System.out.println(bverifyclient.totalCommitments());
	        System.out.println(bverifyclient.currentCommitment());
	       // Assert.assertEquals(3,bverifyclient.totalCommitments());
	        //Assert.assertEquals(2,bverifyclient.currentCommitment());
	       
	        bverifyclient.verifyConsistency();
	        System.out.println(bverifyclient.totalCommitments());
	        System.out.println(bverifyclient.currentCommitment());
	        
	        //Assert.assertEquals(3,bverifyclient.totalCommitments());
	        //Assert.assertEquals(3,bverifyclient.currentCommitment());
	         * 
	         */

			
			
		} catch (InsufficientMoneyException | ProofError e) {
			e.printStackTrace();
			Assert.fail();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Assert.fail();
		}
	}
}
