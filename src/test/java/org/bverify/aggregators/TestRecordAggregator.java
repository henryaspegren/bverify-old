package org.bverify.aggregators;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bverify.accounts.Account;
import org.bverify.records.Deposit;
import org.bverify.records.Transfer;
import org.bverify.records.Withdrawal;
import org.junit.Assert;

import org.junit.Test;

import com.google.common.primitives.*;
		
public class TestRecordAggregator {
	
	public static Account alice = new Account("Alice", 1);
	public static Account bob = new Account("Bob", 2);
	public static Account charlie = new Account("Charlie", 3);
	
	public static MessageDigest md;
	
	static {
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Assert.fail("Something wrong with test environment");
		} 
	}
	
	@Test
	public void testEmptyAgg() {
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		RecordAggregation recordagg = aggregator.emptyAgg();
		
		Assert.assertArrayEquals(RecordAggregation.NULL_HASH, recordagg.getHash());
		Assert.assertEquals(0, recordagg.getNetAmount());
		Assert.assertEquals(0, recordagg.getTotalAmount());
	}
	
	@Test
	public void testAggDeposit() {
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		
		Deposit deposit = new Deposit("CORN", 100, alice, charlie);
		RecordAggregation recordagg = aggregator.aggVal(deposit);
		
		md.reset();
		md.update(deposit.toString().getBytes());
		byte[] correctHash = md.digest();
		
		Assert.assertArrayEquals(correctHash, recordagg.getHash());
		Assert.assertEquals(100, recordagg.getNetAmount());
		Assert.assertEquals(100, recordagg.getTotalAmount());	
	}
	
	@Test
	public void testAggWithdrawl() {
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		
		Withdrawal withdrawal = new Withdrawal("CORN", 50, alice, charlie);
		RecordAggregation recordagg = aggregator.aggVal(withdrawal);
		
		md.reset();
		md.update(withdrawal.toString().getBytes());
		byte[] correctHash = md.digest();
		
		Assert.assertArrayEquals(correctHash, recordagg.getHash());
		Assert.assertEquals(-50, recordagg.getNetAmount());
		Assert.assertEquals(50, recordagg.getTotalAmount());			
	}
	
	@Test
	public void testAggTransfer() {
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		
		Transfer transfer = new Transfer("CORN", 50, alice, charlie);
		RecordAggregation recordagg = aggregator.aggVal(transfer);
		
		md.reset();
		md.update(transfer.toString().getBytes());
		byte[] correctHash = md.digest();
		
		Assert.assertArrayEquals(correctHash, recordagg.getHash());
		Assert.assertEquals(0, recordagg.getNetAmount());
		Assert.assertEquals(50, recordagg.getTotalAmount());		
	}
	
	@Test
	public void testMultipleDepositAgg() {
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		
		Deposit dep1 = new Deposit("CORN", 50, alice, bob);
		Deposit dep2 = new Deposit("CORN", 25, alice, charlie);
		Deposit dep3 = new Deposit("CORN", 1, alice, bob);
		Deposit dep4 = new Deposit("CORN", 24, alice, bob);
		

		RecordAggregation recordagg = aggregator.aggChildren(
				aggregator.aggChildren(
						aggregator.aggVal(dep1),
						aggregator.aggVal(dep2)),
				aggregator.aggChildren(
						aggregator.aggVal(dep3),
						aggregator.aggVal(dep4)
						)
				);
				
		Assert.assertEquals(100, recordagg.getTotalAmount());
		Assert.assertEquals(100, recordagg.getNetAmount());

		
		md.reset();
		md.update(dep1.toString().getBytes());
		byte[] hash1 = md.digest();
		md.update(dep2.toString().getBytes());
		byte[] hash2 = md.digest();
		md.update(dep3.toString().getBytes());
		byte[] hash3 = md.digest();
		md.update(dep4.toString().getBytes());
		byte[] hash4 = md.digest();
		md.update(Ints.toByteArray(75));
		md.update(Ints.toByteArray(75));
		md.update(hash1);
		md.update(hash2);
		byte[] aggl = md.digest();
		md.update(Ints.toByteArray(25));
		md.update(Ints.toByteArray(25));
		md.update(hash3);
		md.update(hash4);
		byte[] aggr = md.digest();	
		md.update(Ints.toByteArray(100));
		md.update(Ints.toByteArray(100));
		md.update(aggl);
		md.update(aggr);
		
		byte[] correctHash1 = md.digest();
		Assert.assertArrayEquals(correctHash1, recordagg.getHash());

		RecordAggregation recordagg2 = aggregator.aggChildren(recordagg, recordagg);
		Assert.assertEquals(200, recordagg2.getTotalAmount());
		Assert.assertEquals(200, recordagg2.getNetAmount());
		md.update(Ints.toByteArray(200));
		md.update(Ints.toByteArray(200));
		md.update(correctHash1);
		md.update(correctHash1);
		byte[] correctHash2 = md.digest();
		Assert.assertArrayEquals(correctHash2, recordagg2.getHash());
		
	}
	
	@Test
	public void testMultipleAggMixed() {
CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		
		Deposit rec1 = new Deposit("CORN", 100, alice, bob);
		Withdrawal rec2 = new Withdrawal("CORN", 25, alice, charlie);
		Transfer rec3 = new Transfer("CORN", 1, alice, bob);
		Transfer rec4 = new Transfer("CORN", 24, alice, bob);
		

		RecordAggregation recordaggL = aggregator.aggChildren(
				aggregator.aggChildren(
						aggregator.aggVal(rec1),
						aggregator.aggVal(rec2)),
				aggregator.aggChildren(
						aggregator.aggVal(rec3),
						aggregator.aggVal(rec4)
						)
				);
		
		Assert.assertEquals(75, recordaggL.getNetAmount());
		Assert.assertEquals(150, recordaggL.getTotalAmount());
				
		Withdrawal rec5 = new Withdrawal("CORN", 25, alice, bob);
		Transfer rec6 = new Transfer("CORN", 1, bob, alice);
		Withdrawal rec7 = new Withdrawal("CORN", 10, charlie, bob);
		Withdrawal rec8 = new Withdrawal("CORN", 15, alice, charlie);
		

		RecordAggregation recordaggR = aggregator.aggChildren(
				aggregator.aggChildren(
						aggregator.aggVal(rec5),
						aggregator.aggVal(rec6)),
				aggregator.aggChildren(
						aggregator.aggVal(rec7),
						aggregator.aggVal(rec8)
						)
				);
		
		Assert.assertEquals(-50, recordaggR.getNetAmount());
		Assert.assertEquals(51, recordaggR.getTotalAmount());
		
		RecordAggregation combinedrecordagg = aggregator.aggChildren(recordaggL, recordaggR);
		
		md.reset();
		// left subtree
		md.update(rec1.toString().getBytes());
		byte[] hash1 = md.digest();
		md.update(rec2.toString().getBytes());
		byte[] hash2 = md.digest();
		md.update(rec3.toString().getBytes());
		byte[] hash3 = md.digest();
		md.update(rec4.toString().getBytes());
		byte[] hash4 = md.digest();
		md.update(Ints.toByteArray(125));
		md.update(Ints.toByteArray(75));
		md.update(hash1);
		md.update(hash2);
		byte[] aggll = md.digest();
		md.update(Ints.toByteArray(25));
		md.update(Ints.toByteArray(0));
		md.update(hash3);
		md.update(hash4);
		byte[] agglr = md.digest();
		md.update(Ints.toByteArray(150));
		md.update(Ints.toByteArray(75));
		md.update(aggll);
		md.update(agglr);
		byte[] aggtreel = md.digest();
		Assert.assertArrayEquals(aggtreel, 	recordaggL.getHash());

		// right subtree
		md.update(rec5.toString().getBytes());
		byte[] hash5 = md.digest();
		md.update(rec6.toString().getBytes());
		byte[] hash6 = md.digest();
		md.update(rec7.toString().getBytes());
		byte[] hash7 = md.digest();
		md.update(rec8.toString().getBytes());
		byte[] hash8 = md.digest();
		md.update(Ints.toByteArray(26));
		md.update(Ints.toByteArray(-25));
		md.update(hash5);
		md.update(hash6);
		byte[] aggrl = md.digest();
		md.update(Ints.toByteArray(25));
		md.update(Ints.toByteArray(-25));
		md.update(hash7);
		md.update(hash8);
		byte[] aggrr = md.digest();
		md.update(Ints.toByteArray(51));
		md.update(Ints.toByteArray(-50));
		md.update(aggrl);
		md.update(aggrr);
		byte[] aggtreer = md.digest();
		Assert.assertArrayEquals(aggtreer, 	recordaggR.getHash());

		
		md.update(Ints.toByteArray(201));
		md.update(Ints.toByteArray(25));
		md.update(aggtreel);
		md.update(aggtreer);
		byte[] finalHash = md.digest();
		
		Assert.assertEquals(201, combinedrecordagg.getTotalAmount());
		Assert.assertEquals(25, combinedrecordagg.getNetAmount());
		Assert.assertArrayEquals(finalHash, 	combinedrecordagg.getHash());

	}
	
	

}
