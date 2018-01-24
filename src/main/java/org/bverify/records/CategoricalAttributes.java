package org.bverify.records;

import java.io.Serializable;
import java.util.BitSet;

/**
 * Wrapper class for manipulating categorical attributes
 * For now the categorical attributes are indexed by  
 *  [0, .... , NUM_ATTRIBUTES - 1]
 * @author henryaspegren
 *
 */
public class CategoricalAttributes implements Serializable {

	private static final long serialVersionUID = 1L;

	// Minimum possible representation size!
	private final BitSet representation;
	
	// default number of categorical attributes
	public static final int DEFAULT_NUM_CATEGORICAL_ATTRIBUTES = 64;
	
	/**
	 * Creates the default categorical attributes 
	 * which are all set to false
	 */
	public CategoricalAttributes() {
		this.representation = new BitSet(DEFAULT_NUM_CATEGORICAL_ATTRIBUTES);
	}
	
	public CategoricalAttributes(int numberAttributes) {
		this.representation = new BitSet(numberAttributes);
	}
	
	public CategoricalAttributes(CategoricalAttributes copy) {
		this.representation = (BitSet) copy.representation.clone();
	}
	
	public boolean getAttribute(int attributeIdx) {
		return this.representation.get(attributeIdx);
	}
	
	public void setAttribute(int attributeIdx, boolean value) {
		this.representation.set(attributeIdx, value);
	}
	
	public byte[] toByteArray() {
		return this.representation.toByteArray();
	}
	
	/**
	 * Creates a new categorical attributes by logically ORing this 
	 * categorical attribute with another set of categorical attributes.
	 * An attribute will be set to true if it is true in this or in the other. Must 
	 * have same number of attributes or throws a runtime error. Does 
	 * NOT mutate this
	 * @param other
	 */
	public CategoricalAttributes or(CategoricalAttributes other) {
		if(this.numberOfAttributes() != other.numberOfAttributes()) {
			throw new RuntimeException("Error - Trying to OR two Categorical"
					+ "Attributes with Different Numbers of Attributes!");
		}
		CategoricalAttributes newcatatt = new CategoricalAttributes(this);
		newcatatt.representation.or(other.representation);
		return newcatatt;
	}
	
	public int numberOfAttributes() {
		return this.representation.size();
	}
	
	@Override
	public int hashCode() {
		return this.representation.hashCode();
	}
	
	@Override
	public boolean equals(Object arg0) {
		if( arg0 instanceof CategoricalAttributes) {
			CategoricalAttributes arg0cast = (CategoricalAttributes) arg0;
			return this.representation.equals(arg0cast.representation);
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder message = new StringBuilder();
		message.append("<Categorical Attributes:");
		message.append(System.lineSeparator());
		message.append(this.representation);
		message.append(">");
		return message.toString();
	}
	
}
