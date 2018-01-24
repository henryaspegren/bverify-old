package org.bverify.aggregators;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.SerializationUtils;
import org.bouncycastle.util.Arrays;
import org.bverify.records.CategoricalAttributes;
import org.bverify.records.NumericalAttributes;
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
 * 			for each numerical attribute att:
 * 					new numerical attribute = a.att + b.att
 * 
 * 			new categorical attributes = a.attributes BITWISE OR b.attributes
 * 			
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
	private static final long serialVersionUID = 3L;

	// 32 bytes for SHA-256 hash
	public static final byte[] NULL_HASH = new byte[32];
		
	/**
	 * Numerical Attributes
	 */
	private final NumericalAttributes numericalAttributes;
	
	/**
	 * Categorical Attributes 
	 */
	private final CategoricalAttributes categoricalAttributes;
	
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
		this.categoricalAttributes = new CategoricalAttributes();
		this.numericalAttributes = new NumericalAttributes();
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
		this.numericalAttributes = new NumericalAttributes();
		this.numericalAttributes.setAttribute(0, totalAmount);
		this.numericalAttributes.setAttribute(1, netAmount);
		this.hash = hash;
		this.categoricalAttributes = new CategoricalAttributes();
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
		NumericalAttributes numericalLeft, numericalRight;
		CategoricalAttributes categoricalLeft, categoricalRight;
		byte[] hashLeft, hashRight;
		
		if(leftagg != null) {
			numericalLeft = leftagg.getNumericalAttributes();
			categoricalLeft = leftagg.getCategoricalAttributes();
			hashLeft = leftagg.getHash();
		}
		else {
			numericalLeft = new NumericalAttributes();
			categoricalLeft = new CategoricalAttributes();
			hashLeft = RecordAggregation.NULL_HASH;
		}
		if(rightagg != null) {
			numericalRight = rightagg.getNumericalAttributes();
			categoricalRight = rightagg.getCategoricalAttributes();
			hashRight = rightagg.getHash();
		}
		else {
			numericalRight = new NumericalAttributes();
			categoricalRight = new CategoricalAttributes();
			hashRight = RecordAggregation.NULL_HASH;
		}
		
		// ADD the numerical attributes 
		this.numericalAttributes = numericalLeft.add(numericalRight);
		
		// OR the categorical attributes
		this.categoricalAttributes = categoricalLeft.or(categoricalRight);
		
		this.hash = RecordAggregation.calculateHash(this.numericalAttributes,
				this.categoricalAttributes, hashLeft, hashRight);


	}	
	
	public CategoricalAttributes getCategoricalAttributes() {
		return new CategoricalAttributes(this.categoricalAttributes);
	}
	
	public NumericalAttributes getNumericalAttributes(){
		return new NumericalAttributes(this.numericalAttributes);
	}
	
	public int getTotalAmount() {
		return this.numericalAttributes.getAttribute(0);
	}
	
	public int getNetAmount() {
		return this.numericalAttributes.getAttribute(1);
	}
	
	public byte[] getHash() {
		return this.hash;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.categoricalAttributes);
		sb.append(System.getProperty("line.separator"));
		sb.append(this.numericalAttributes);
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
	
	
	public static byte[] calculateHash(NumericalAttributes numericalAttributes,
			CategoricalAttributes categoricalAttributes,
			byte[] hashLeft, byte[] hashRight) {
		try {
			/**
			 * NOTE that the hash is computed over the hashes of the children
			 * AND over the attributes!
			 */
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			
			for(int i = 0; i < numericalAttributes.numberOfAttributes(); i ++) {
				md.update(Ints.toByteArray(numericalAttributes.getAttribute(i)));
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
