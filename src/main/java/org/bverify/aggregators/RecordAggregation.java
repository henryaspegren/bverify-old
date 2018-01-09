package org.bverify.aggregators;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.bverify.records.Record;

import com.google.common.primitives.Ints;


public class RecordAggregation {
	
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
		byte[] recordSerialization = val.toString().getBytes();
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
		
		byte[] hashRes; 
		try {
			/**
			 * NOTE that the hash is computed over the hashes of the children
			 * AND over the amount aggregation
			 */
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(Ints.toByteArray(this.totalAmount));
			md.update(Ints.toByteArray(this.netAmount));
			md.update(hashLeft);
			md.update(hashRight);
			hashRes = md.digest();
			
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			hashRes = null;
		}
		this.hash = hashRes;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}
	

}
