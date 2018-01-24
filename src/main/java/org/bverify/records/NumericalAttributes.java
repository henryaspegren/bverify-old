package org.bverify.records;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Wrapper class for manipulating numerical attributes
 * For now the numerical attributes are indexed by
 *  [0, ..., NUM_ATTRIBUTES - 1]
 * @author henryaspegren
 *
 */
public class NumericalAttributes implements Serializable {
	
	private static final long serialVersionUID = 1L;

	// Minimum possible representation size!
	private final int[] representation;
	
	// default number of numerical attributes
	public static final int DEFAULT_NUM_NUMERICAL_ATTRIBUTES = 2;
	
	/**
	 * Creates the default numerical attributes 
	 * (all 0)
	 */
	public NumericalAttributes() {
		this.representation = new int[DEFAULT_NUM_NUMERICAL_ATTRIBUTES];
	}
	
	public NumericalAttributes(int numberAttributes) {
		this.representation = new int[numberAttributes];
	}
	
	public NumericalAttributes(NumericalAttributes other) {
		this.representation = Arrays.copyOf(other.representation, other.representation.length);
	}
	
	public int getAttribute(int attributeIdx) {
		return this.representation[attributeIdx];
	}
	
	public void setAttribute(int attributeIdx, int val) {
		this.representation[attributeIdx] = val;
	}

	/**
	 * Creates a new numerical attributes by adding the contents 
	 * attribute-wise of this numerical attribute and the other 
	 * numerical attribute. Must have same number of attributes
	 * or throws a runtime error. Does NOT mutate this
	 * @param other
	 */
	public NumericalAttributes add(NumericalAttributes other) {
		if(this.numberOfAttributes() != other.numberOfAttributes()) {
			throw new RuntimeException("Error - Trying to OR two Numerical"
					+ "Attributes with Different Numbers of Attributes!");
		}
		NumericalAttributes newatt = new NumericalAttributes(this);
		for(int i = 0; i < this.representation.length; i++) {
			newatt.representation[i] = this.representation[i] + other.representation[i];
		}
		return newatt;
	}
	
	public int numberOfAttributes() {
		return this.representation.length;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(this.representation);
	}
	
	@Override
	/**
	 * Two Numerical Attributes are equal if they have 
	 * the same contents (do not need to be the same reference)
	 */
	public boolean equals(Object arg0) {
		if(arg0 instanceof NumericalAttributes) {
			NumericalAttributes arg0cast = (NumericalAttributes) arg0;
			return Arrays.equals(this.representation, arg0cast.representation);
		}else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		StringBuilder message = new StringBuilder();
		message.append("<Numerical Attributes:");
		message.append(System.lineSeparator());
		message.append(this.representation);
		message.append(">");
		return message.toString();
	}
	
	
}
