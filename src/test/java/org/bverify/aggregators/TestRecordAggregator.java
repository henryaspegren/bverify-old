package org.bverify.aggregators;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.SerializationUtils;
import org.bverify.accounts.Account;
import org.bverify.records.CategoricalAttributes;
import org.bverify.records.Deposit;
import org.bverify.records.NumericalAttributes;
import org.bverify.records.SimpleRecord;
import org.bverify.records.Transfer;
import org.bverify.records.Withdrawal;
import org.junit.Assert;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

		
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
		NumericalAttributes numl = new NumericalAttributes();
		numl.setAttribute(1, 75);
		numl.setAttribute(0, 75);
		byte[] aggl = RecordAggregation.calculateHash(numl, new CategoricalAttributes(),
				hash1, hash2);
		NumericalAttributes numr = new NumericalAttributes();
		numr.setAttribute(1, 25);
		numr.setAttribute(0, 25);
		byte[] aggr = RecordAggregation.calculateHash(numr, new CategoricalAttributes(),
				hash3, hash4);
		
		NumericalAttributes numfinal = new NumericalAttributes();
		numfinal.setAttribute(1, 100);
		numfinal.setAttribute(0, 100);

		byte[] correctHash1 = RecordAggregation.calculateHash(numfinal,
				new CategoricalAttributes(), aggl, aggr);
		
		Assert.assertArrayEquals(correctHash1, recordagg.getHash());

		RecordAggregation recordagg2 = aggregator.aggChildren(recordagg, recordagg);
		Assert.assertEquals(200, recordagg2.getTotalAmount());
		Assert.assertEquals(200, recordagg2.getNetAmount());
		NumericalAttributes mapfinal2 = new NumericalAttributes();
		mapfinal2.setAttribute(0, 200);
		mapfinal2.setAttribute(1, 200);
		byte[] correctHash2 = RecordAggregation.calculateHash(mapfinal2, 
				new CategoricalAttributes(), correctHash1, correctHash1);
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
	
	public void testRecordAggregationPrint() {
		Deposit recordA = new Deposit(goodCorn, 101, alice, bob);
		Withdrawal recordB = new Withdrawal(goodCorn, 10, alice, charlie);
		RecordAggregation recordaggA = new RecordAggregation(recordA);
		RecordAggregation recordaggB = new RecordAggregation(recordB);
		RecordAggregation recordcombined = new RecordAggregation(recordaggA, recordaggB);	
		System.out.println(recordcombined);
	}
	
	public void testRecordAggregationSimpleRecords() {
		NumericalAttributes n1 = new NumericalAttributes(10);
		n1.setAttribute(5, 100);
		
		CategoricalAttributes c1 = new CategoricalAttributes(10);
		c1.setAttribute(0, true);
		c1.setAttribute(2, true);
		c1.setAttribute(4, true);
		c1.setAttribute(5, true);

		NumericalAttributes n2 = new NumericalAttributes(10);
		n2.setAttribute(4, 100);
		n2.setAttribute(5, -50);
	
		CategoricalAttributes c2 = new CategoricalAttributes(10);
		c2.setAttribute(1, true);
		c2.setAttribute(5, true);
		
		SimpleRecord sr1 = new SimpleRecord(c1,n1);
		SimpleRecord sr2 = new SimpleRecord(c2,n2);
		
		
		RecordAggregation agg = new RecordAggregation(new RecordAggregation(sr1),
				new RecordAggregation(sr2));
		
		CategoricalAttributes cfinal = new CategoricalAttributes(10);
		cfinal.setAttribute(0, true);
		cfinal.setAttribute(1, true);
		cfinal.setAttribute(2, true);
		cfinal.setAttribute(4, true);
		cfinal.setAttribute(5, true);
		
		NumericalAttributes nfinal = new NumericalAttributes(10);
		nfinal.setAttribute(4, 100);
		nfinal.setAttribute(5, 50);

		Assert.assertEquals(cfinal, agg.getCategoricalAttributes());
		Assert.assertEquals(nfinal, agg.getNumericalAttributes());
		
	}
	
	public void testRecordAggregationOr() {
		NumericalAttributes n1 = new NumericalAttributes(10);		
		CategoricalAttributes c1 = new CategoricalAttributes(10);
		c1.setAttribute(0, true);
		c1.setAttribute(2, true);
		c1.setAttribute(4, true);
		c1.setAttribute(6, true);
		
		SimpleRecord sr = new SimpleRecord(c1, n1);
		RecordAggregation agg = new RecordAggregation(sr);
		CategoricalAttributes filter = new CategoricalAttributes(10);
		filter.setAttribute(0, true);
		filter.setAttribute(2, true);
		
		Assert.assertTrue(agg.hasCategoricalAttributes(filter));
		Assert.assertFalse(agg.matchesCategoricalAttributes(filter));
		
		filter.setAttribute(4, true);
		filter.setAttribute(6, true);
		
		Assert.assertTrue(agg.hasCategoricalAttributes(filter));
		Assert.assertTrue(agg.matchesCategoricalAttributes(filter));
		
	}
	
	public void testRecordAggregationWithNulls() {
		SimpleRecord sr = new SimpleRecord(100, 100);
		RecordAggregation agg = new RecordAggregation(sr);
		RecordAggregation aggWithNullL = new RecordAggregation(null, agg);
		
		Assert.assertEquals(sr.getCategoricalAttributes(), aggWithNullL.getCategoricalAttributes());
		Assert.assertEquals(sr.getNumericalAttributes(), aggWithNullL.getNumericalAttributes());
		
		RecordAggregation aggWithNullR = new RecordAggregation(agg, null);
		
		Assert.assertEquals(sr.getCategoricalAttributes(), aggWithNullR.getCategoricalAttributes());
		Assert.assertEquals(sr.getNumericalAttributes(), aggWithNullR.getNumericalAttributes());
		
		
	}
	
	
}
