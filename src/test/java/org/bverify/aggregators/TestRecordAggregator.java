package org.bverify.aggregators;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.bverify.accounts.Account;
import org.bverify.records.Deposit;
import org.bverify.records.Record;
import org.bverify.records.Transfer;
import org.bverify.records.Withdrawal;
import org.junit.Assert;

import com.google.common.primitives.*;

import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Test;

		
public class TestRecordAggregator extends TestCase {
	
	public static Account alice = new Account("Alice", 1);
	public static Account bob = new Account("Bob", 2);
	public static Account charlie = new Account("Charlie", 3);
	
	public static String goodCorn = "CORN";
	
	public static MessageDigest md;
	
	static {
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			Assert.fail("Something wrong with test environment");
		} 
	}
	
	/**
	 * Test suite 
	 * @param testName
	 */
    public TestRecordAggregator( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( TestRecordAggregator.class );
    }
	
	
	public void testEmptyAgg() {
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		RecordAggregation recordagg = aggregator.emptyAgg();
		
		Assert.assertArrayEquals(RecordAggregation.NULL_HASH, recordagg.getHash());
		Assert.assertEquals(0, recordagg.getNetAmount());
		Assert.assertEquals(0, recordagg.getTotalAmount());
	}
	
	public void testAggDeposit() {
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		
		Deposit deposit = new Deposit(goodCorn, 100, alice, charlie);
		RecordAggregation recordagg = aggregator.aggVal(deposit);
		
		md.reset();
		md.update(SerializationUtils.serialize(deposit));
		byte[] correctHash = md.digest();
		
		Assert.assertArrayEquals(correctHash, recordagg.getHash());
		Assert.assertEquals(100, recordagg.getNetAmount());
		Assert.assertEquals(100, recordagg.getTotalAmount());	
	}
	
	public void testAggWithdrawl() {
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		
		Withdrawal withdrawal = new Withdrawal(goodCorn, 50, alice, charlie);
		RecordAggregation recordagg = aggregator.aggVal(withdrawal);
		
		md.reset();
		md.update(SerializationUtils.serialize(withdrawal));
		byte[] correctHash = md.digest();
		
		System.out.println(withdrawal);
		
		Assert.assertArrayEquals(correctHash, recordagg.getHash());
		Assert.assertEquals(-50, recordagg.getNetAmount());
		Assert.assertEquals(50, recordagg.getTotalAmount());			
	}
	
	public void testAggTransfer() {
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		
		Transfer transfer = new Transfer(goodCorn, 50, alice, charlie);
		RecordAggregation recordagg = aggregator.aggVal(transfer);
		
		md.reset();
		md.update(SerializationUtils.serialize(transfer));
		byte[] correctHash = md.digest();
		
		Assert.assertArrayEquals(correctHash, recordagg.getHash());
		Assert.assertEquals(0, recordagg.getNetAmount());
		Assert.assertEquals(50, recordagg.getTotalAmount());		
	}
	
	public void testMultipleDepositAgg() {
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		
		Deposit dep1 = new Deposit(goodCorn, 50, alice, bob);
		Deposit dep2 = new Deposit(goodCorn, 25, alice, charlie);
		Deposit dep3 = new Deposit(goodCorn, 1, alice, bob);
		Deposit dep4 = new Deposit(goodCorn, 24, alice, bob);
		

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
		md.update(SerializationUtils.serialize(dep1));
		byte[] hash1 = md.digest();
		md.update(SerializationUtils.serialize(dep2));
		byte[] hash2 = md.digest();
		md.update(SerializationUtils.serialize(dep3));
		byte[] hash3 = md.digest();
		md.update(SerializationUtils.serialize(dep4));
		byte[] hash4 = md.digest();
		Map<String, Integer> mapl = new HashMap<String, Integer>();
		mapl.put(Record.totalAmount, 75);
		mapl.put(Record.netAmount, 75);
		byte[] aggl = RecordAggregation.calculateHash(mapl, hash1, hash2, 
				new BitSet(RecordAggregation.NUM_ATTRBUTES));
		Map<String, Integer> mapr = new HashMap<String, Integer>();
		mapr.put(Record.totalAmount, 25);
		mapr.put(Record.netAmount, 25);
		byte[] aggr = RecordAggregation.calculateHash(mapr, hash3, hash4, 
				new BitSet(RecordAggregation.NUM_ATTRBUTES));
		Map<String, Integer> mapfinal = new HashMap<String, Integer>();
		mapfinal.put(Record.totalAmount, 100);
		mapfinal.put(Record.netAmount, 100);
		byte[] correctHash1 = RecordAggregation.calculateHash(mapfinal, aggl, aggr, 
				new BitSet(RecordAggregation.NUM_ATTRBUTES));
		
		Assert.assertArrayEquals(correctHash1, recordagg.getHash());

		RecordAggregation recordagg2 = aggregator.aggChildren(recordagg, recordagg);
		Assert.assertEquals(200, recordagg2.getTotalAmount());
		Assert.assertEquals(200, recordagg2.getNetAmount());
		Map<String, Integer> mapfinal2 = new HashMap<String, Integer>();
		mapfinal2.put(Record.totalAmount, 200);
		mapfinal2.put(Record.netAmount, 200);
		byte[] correctHash2 = RecordAggregation.calculateHash(mapfinal2, correctHash1, correctHash1, 
				new BitSet(RecordAggregation.NUM_ATTRBUTES));
		Assert.assertArrayEquals(correctHash2, recordagg2.getHash());
		
	}
	
	public void testMultipleAggMixed() {
CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		
		Deposit rec1 = new Deposit(goodCorn, 100, alice, bob);
		Withdrawal rec2 = new Withdrawal(goodCorn, 25, alice, charlie);
		Transfer rec3 = new Transfer(goodCorn, 1, alice, bob);
		Transfer rec4 = new Transfer(goodCorn, 24, alice, bob);
		

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
				
		Withdrawal rec5 = new Withdrawal(goodCorn, 25, alice, bob);
		Transfer rec6 = new Transfer(goodCorn, 1, bob, alice);
		Withdrawal rec7 = new Withdrawal(goodCorn, 10, charlie, bob);
		Withdrawal rec8 = new Withdrawal(goodCorn, 15, alice, charlie);
		

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
		
		
		Assert.assertEquals(201, combinedrecordagg.getTotalAmount());
		Assert.assertEquals(25, combinedrecordagg.getNetAmount());

	}
	
	public void testRecordAggregationSerializationSingleRecord() {
		Deposit record = new Deposit(goodCorn, 101, alice, bob);
		RecordAggregation recordagg = new RecordAggregation(record);
		
		byte[] recordaggbytes = SerializationUtils.serialize(recordagg);
		RecordAggregation recordaggFromBytes = SerializationUtils.deserialize(recordaggbytes);
		
		Assert.assertEquals(recordagg, recordaggFromBytes);
	}
	
	public void testRecordAggregationSerilizationMultiple() {
		Deposit recordA = new Deposit(goodCorn, 101, alice, bob);
		Withdrawal recordB = new Withdrawal(goodCorn, 10, alice, charlie);
		RecordAggregation recordaggA = new RecordAggregation(recordA);
		RecordAggregation recordaggB = new RecordAggregation(recordB);
		RecordAggregation recordcombined = new RecordAggregation(recordaggA, recordaggB);	
		
		byte[] recordaggbytes = SerializationUtils.serialize(recordcombined);
		RecordAggregation recordaggFromBytes = SerializationUtils.deserialize(recordaggbytes);
		
		Assert.assertEquals(recordcombined, recordaggFromBytes);
	}
	
	

}
