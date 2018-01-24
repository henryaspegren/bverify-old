package org.bverify.aggregators;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.bouncycastle.util.Arrays;
import org.bverify.records.Record;

import com.google.common.primitives.Ints;

/**
 * Represents a recursive aggregation of records. 
 *  
 *  Records have two types of attributes: categorical and numerical
 *  
 *  Here is the recursion for calculating hashes
 *  
 *  BASE CASES(records)
 * 		hash = SHA-256(Record -> to Bytes)	
 * 
 * 	RECURSIVE CASE
 * 		Agg(RecordAggregation a, RecordAggregation b) : 
 * 			for each numerical attribute:
 * 					new numerical attribute = a.att
 * 
 * 			new categorical attributes = a.attributes BITWISE OR b.attributes
 * 			hash = SHA-256(new numerical attributes || 
 * 							new categorical attributes || 
 * 											a.hash || b.hash )
 * 
 * 
 * @author henryaspegren
 *
 */
public class RecordAggregation implements Serializable {
	
	// version
	private static final long serialVersionUID = 2L;

	// 32 bytes for SHA-256 hash
	public static final byte[] NULL_HASH = new byte[32];
	
	// 64 attributes (64 bits)
	public static final int NUM_ATTRBUTES = 64;
	
	/**
	 * Numerical Attributes
	 */
	private final Map<String, Integer> numericalAttributes;
	
	/**
	 * Categorical Attributes 
	 */
	private final BitSet categoricalAttributes;
	
	/**
	 * Summary Hash
	 */
	private final byte[] hash;
	
	/**
	 * Creates an empty record aggregation
	 * 
	 */
	public RecordAggregation() {
		this.hash = RecordAggregation.NULL_HASH;
		this.categoricalAttributes = new BitSet(RecordAggregation.NUM_ATTRBUTES);
		this.numericalAttributes = new HashMap<String, Integer>();
		this.numericalAttributes.put(Record.totalAmount, 0);
		this.numericalAttributes.put(Record.netAmount, 0);
	}
	
	/**
	 * Creates a record aggregation of a single record  
	 * @param leftval
	 */
	public RecordAggregation(Record val) {
		this.categoricalAttributes = val.getCategoricalAttributes();
		this.numericalAttributes = val.getNumericalAttributes();

		byte[] recordSerialization = SerializationUtils.serialize(val);
		byte[] hashRes; 
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(recordSerialization);
			hashRes = md.digest();
			
		} catch (NoSuchAlgorithmException e) {
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
		this.numericalAttributes = new HashMap<String, Integer>();
		this.numericalAttributes.put(Record.totalAmount, totalAmount);
		this.numericalAttributes.put(Record.netAmount, netAmount);
		this.hash = hash;
		this.categoricalAttributes = new BitSet(RecordAggregation.NUM_ATTRBUTES);
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
		BitSet attributesLeft, attributesRight;
		
		if(leftagg != null) {
			leftTotal = leftagg.getTotalAmount();
			leftNet = leftagg.getNetAmount();
			hashLeft = leftagg.getHash();
			attributesLeft = leftagg.getCategoricalAttributes();
		}
		else {
			leftTotal = 0;
			leftNet = 0;
			hashLeft = RecordAggregation.NULL_HASH;
			// this is a bit field of all zeros
			attributesLeft = new BitSet(RecordAggregation.NUM_ATTRBUTES);
		}
		if(rightagg != null) {
			rightTotal = rightagg.getTotalAmount();
			rightNet = rightagg.getNetAmount();
			hashRight = rightagg.getHash();
			attributesRight = rightagg.getCategoricalAttributes();
		}
		else {
			rightTotal = 0; 
			rightNet = 0; 
			hashRight = RecordAggregation.NULL_HASH;
			// this is a bit field of all zeros
			attributesRight = new BitSet(RecordAggregation.NUM_ATTRBUTES);
		}
		
		this.numericalAttributes = new HashMap<String, Integer>();
		this.numericalAttributes.put(Record.totalAmount, leftTotal + rightTotal);
		this.numericalAttributes.put(Record.netAmount, leftNet + rightNet);
		
		// OR the bit fields
		attributesRight.or(attributesLeft);
		// and make a copy
		this.categoricalAttributes = (BitSet) attributesRight.clone();
		
		this.hash = RecordAggregation.calculateHash(this.numericalAttributes,
				hashLeft, hashRight, this.categoricalAttributes);


	}	
	
	public BitSet getCategoricalAttributes() {
		return (BitSet) this.categoricalAttributes.clone();
	}
	
	public Map<String, Integer> getNumericalAttributes(){
		return new HashMap<String, Integer>(this.numericalAttributes);
	}
	
	public int getTotalAmount() {
		return this.numericalAttributes.get(Record.totalAmount);
	}
	
	public int getNetAmount() {
		return this.numericalAttributes.get(Record.netAmount);
	}
	
	public byte[] getHash() {
		return this.hash;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Total Amnt: ");
		sb.append(this.getTotalAmount());
		sb.append(System.getProperty("line.separator"));
		sb.append("Net Amnt: ");
		sb.append(this.getNetAmount());
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
					arg.categoricalAttributes.equals(this.categoricalAttributes) &&
					arg.numericalAttributes.equals(this.numericalAttributes)) {
				return true;
			}
		}
		return false;
	}
	
	
	public static byte[] calculateHash(Map<String, Integer> numericalAttributes,
			byte[] hashLeft, byte[] hashRight, BitSet categoricalAttributes) {
		try {
			/**
			 * NOTE that the hash is computed over the hashes of the children
			 * AND over the attributes!
			 */
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			
			// we sort the keys lexicographically 
			List<String> sortedKeys = new ArrayList<String>(numericalAttributes.size());
			sortedKeys.addAll(numericalAttributes.keySet());
			Collections.sort(sortedKeys); 
			
			for(String key : sortedKeys) {
				md.update(Ints.toByteArray(numericalAttributes.get(key)));
			}

			md.update(categoricalAttributes.toByteArray());
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
