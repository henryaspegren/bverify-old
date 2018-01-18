package org.bverify.proofs;

import java.util.Arrays;

import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.junit.Test;
import org.junit.Assert;

public class AggregationProofTest extends ProofTestBase {
	
	@Test 
	public void testAggregationProofValid() {
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		RecordAggregation leftAgg = cgr.aggVal(deposit);
		RecordAggregation rightAgg = cgr.aggVal(withdrawal);
		RecordAggregation mainAgg = cgr.aggChildren(leftAgg, rightAgg);
		
		AggregationProof aggProof = new AggregationProof(mainAgg, leftAgg.getHash(), 
				rightAgg.getHash(), 1);
		
		Assert.assertEquals(mainAgg, aggProof.getAggregation());
		Assert.assertTrue(aggProof.checkProof(mainAgg.getHash()));		
	}
	
	@Test 
	public void testAggregationProofIncorrectCommitment() {
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		RecordAggregation leftAgg = cgr.aggVal(deposit);
		RecordAggregation rightAgg = cgr.aggVal(withdrawal);
		
		RecordAggregation mainAgg = cgr.aggChildren(leftAgg, rightAgg);
		RecordAggregation wrongAgg = cgr.aggChildren(leftAgg, leftAgg);
		
		AggregationProof aggProof = new AggregationProof(mainAgg, leftAgg.getHash(), 
				rightAgg.getHash(), 1);
		
		Assert.assertEquals(mainAgg, aggProof.getAggregation());
		
		// commitment is incorrect!
		Assert.assertFalse(aggProof.checkProof(wrongAgg.getHash()));		
	}
	
	@Test
	public void testAggregationProofIncorrectHashInputs() {
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		RecordAggregation leftAgg = cgr.aggVal(deposit);
		RecordAggregation rightAgg = cgr.aggVal(withdrawal);
		
		RecordAggregation mainAgg = cgr.aggChildren(leftAgg, rightAgg);
		// tamper with the aggregation values
		RecordAggregation wrongAgg = RecordAggregation.modifyRecordAggregation(
				mainAgg, 123456, 123456);
				
		// have the same hash, but different pre-images
		Assert.assertTrue(Arrays.equals(mainAgg.getHash(), wrongAgg.getHash()));
		Assert.assertNotEquals(mainAgg, wrongAgg);

		// the preimage is invalid!
		AggregationProof aggProof = new AggregationProof(wrongAgg, leftAgg.getHash(), 
				rightAgg.getHash(), 1);
		
	}
	
	@Test 
	public void testAggregationProofIncorrectHashWrongOrder() {
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		RecordAggregation leftAgg = cgr.aggVal(deposit);
		RecordAggregation rightAgg = cgr.aggVal(withdrawal);
		RecordAggregation mainAgg = cgr.aggChildren(leftAgg, rightAgg);
		
		// children are flipped so the preimage is invalid
		AggregationProof aggProof = new AggregationProof(mainAgg, 
				rightAgg.getHash(), leftAgg.getHash(), 1);
		
		Assert.assertEquals(mainAgg, aggProof.getAggregation());
		Assert.assertFalse(aggProof.checkProof(mainAgg.getHash()));
	}
	
}
