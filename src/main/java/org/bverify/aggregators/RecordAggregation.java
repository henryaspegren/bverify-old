package org.bverify.aggregators;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.SerializationUtils;
import org.bouncycastle.util.Arrays;
import org.bverify.records.Record;

import com.google.common.primitives.Ints;

/**
 * Represents a recursive aggregation of records. 
 *  
 *  Here is the recursion:
 *  
 *  BASE CASES(records)
 * 		Deposit(amount): netAmount: amount, totalAmount: amount, 
 * 		Transfer(amount): netAmount: 0, totalAmount: amount
 * 		Withdrawal(amount): netAmount: -1*amount, totalAmount: amount
 * 		
 * 		for all of these 
 * 		hash = SHA-256(Record -> to Bytes)	
 * 
 * 	RECURSIVE CASE
 * 		Agg(RecordAggregation a, RecordAggregation b) : 
 * 			netAmount = a.netAmount + b.netAmount;
 * 			totalAmount = a.totalAmount + b.totalAmount
 * 			hash = SHA-256(netAmount || totalAmount || a.hash || b.hash)
 * 
 * 
 * @author henryaspegren
 *
 */
public class RecordAggregation implements Serializable {
	
	
	private static final long serialVersionUID = 1L;

	// 32 bytes for SHA-256 hash
	public static final byte[] NULL_HASH = new byte[32];
	
	/**
	
	/** For now just try to do very basic aggregation
	 *  over record data.
	 */
	private final int totalAmount; 
	private final int netAmount;
	private final byte[] hash;
		
	
	/**
	 * Creates an empty record aggregation
	 * 
	 */
	public RecordAggregation() {
		this.totalAmount = 0;
		this.netAmount = 0;
		this.hash = RecordAggregation.NULL_HASH;
	}
	
	
	/**
	 * Creates a record aggregation of a single record  
	 * @param leftval
	 */
	public RecordAggregation(Record val) {
		this.totalAmount = val.getTotalAmount();
		this.netAmount = val.getNetChange();
		// for now this is just the string but 
		// TODO: replace with a google protobuf
		byte[] recordSerialization = SerializationUtils.serialize(val);
		byte[] hashRes; 
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(recordSerialization);
			hashRes = md.digest();
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			hashRes = null;
		}
		this.hash = hashRes;

	}
	
	/**
	 * FOR TESTING PURPOSES ONLY
	 * - Creates a record with the desired parameters. This can be used
	 * to create inconsistent records {@link #modifyRecordAggregation(RecordAggregation, int, int)}
	 * @param totalAmount
	 * @param netAmount
	 * @param hash
	 */
	public RecordAggregation(int totalAmount, int netAmount, byte[] hash) {
		this.totalAmount = totalAmount;
		this.netAmount = netAmount;
		this.hash = hash;
	}
	
	/**
	 * Creates a record aggregation from two other record aggregations.
	 * For now just does a very simple aggregation of adding amounts of records.
	 * 						agg
	 * 					   /   \ 
	 * 				      /     \
	 * 				  leftagg   rightagg
	 * @param leftagg - the 'left' record aggregation, may be null (unusual case)
	 * @param rightagg - the 'right' record aggregation, may be null
	 */
	public RecordAggregation(RecordAggregation leftagg, RecordAggregation rightagg) {
		int leftTotal, leftNet, rightTotal, rightNet;
		byte[] hashLeft, hashRight;
		
		if(leftagg != null) {
			leftTotal = leftagg.getTotalAmount();
			leftNet = leftagg.getNetAmount();
			hashLeft = leftagg.getHash();
		}
		else {
			leftTotal = 0;
			leftNet = 0;
			hashLeft = RecordAggregation.NULL_HASH;
		}
		if(rightagg != null) {
			rightTotal = rightagg.getTotalAmount();
			rightNet = rightagg.getNetAmount();
			hashRight = rightagg.getHash();
		}
		else {
			rightTotal = 0; 
			rightNet = 0; 
			hashRight = RecordAggregation.NULL_HASH;
		}
		
		this.totalAmount = leftTotal + rightTotal;
		this.netAmount = leftNet + rightNet;

		this.hash = RecordAggregation.calculateHash(this.totalAmount, this.netAmount,
				hashLeft, hashRight);


	}	
	
	public int getTotalAmount() {
		return this.totalAmount;
	}
	
	public int getNetAmount() {
		return this.netAmount;
	}
	
	public byte[] getHash() {
		return this.hash;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Total Amnt: ");
		sb.append(this.totalAmount);
		sb.append(System.getProperty("line.separator"));
		sb.append("Net Amnt: ");
		sb.append(this.netAmount);
		sb.append(System.getProperty("line.separator"));
		sb.append("Hash: ");
		try {
			sb.append(new String(this.hash, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof RecordAggregation) {
			RecordAggregation arg = (RecordAggregation) arg0;
			if(Arrays.areEqual(arg.getHash(), this.getHash()) &&
					arg.netAmount == this.netAmount &&
					arg.totalAmount == this.totalAmount) {
				return true;
			}
		}
		return false;
	}
	
	
	public static byte[] calculateHash(int totalAmount, int netAmount,
			byte[] hashLeft, byte[] hashRight) {
		try {
			/**
			 * NOTE that the hash is computed over the hashes of the children
			 * AND over the amount aggregation
			 */
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(Ints.toByteArray(totalAmount));
			md.update(Ints.toByteArray(netAmount));
			md.update(hashLeft);
			md.update(hashRight);
			byte[] hashRes = md.digest();
			return hashRes;
			
		}catch(NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("FATAL BUG");
		}
	}

	/**
	 * FOR TESTING PURPOSES ONLY - Used copy a record aggregation 
	 * but modify the totals - can create an invalid record
	 * @param prevAgg
	 * @param newTotal
	 * @param newNet
	 * @return
	 */
	public static RecordAggregation modifyRecordAggregation(RecordAggregation prevAgg,
			int newTotal, int newNet) {
		return new RecordAggregation(newTotal, newNet, prevAgg.getHash());
	}
	
}
