package org.bverify.records;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.BitSet;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bverify.accounts.Account;
import org.bverify.aggregators.RecordAggregation;


public abstract class Change implements Record {

	private static final long serialVersionUID = 2L;
	
	// should be later moved over to a categorical attribute
	protected final String goodType; 
	
	protected Date dateCreated;
	protected byte[] recepientSignature;
	protected byte[] employeeSignature;
	protected final Account employee;
	protected final Account recepient;
	
	// categorical attributes
	protected final BitSet categoricalAttributes;
	
	// numerical attributes, 
	protected final Map<String, Integer> numericalAttributes;
	
	
	public Change(String goodType, int netAmount, int totalAmount,
			Account recepient, Account employee) {
		this.goodType = goodType;
		this.recepient = recepient;
		this.employee = employee;
		this.numericalAttributes = new HashMap<String, Integer>();
		this.numericalAttributes.put(Record.totalAmount, totalAmount);
		this.numericalAttributes.put(Record.netAmount, netAmount);
		this.categoricalAttributes = new BitSet(RecordAggregation.NUM_ATTRBUTES);
		this.dateCreated = new Date();
	}
	
	@Override
	public int getTotalAmount() {
		return this.numericalAttributes.get(Record.totalAmount);
	}

	@Override
	public int getNetChange() {
		return this.numericalAttributes.get(Record.netAmount);
	}
	
	@Override
	public int getNumericalAttribute(String attribute) {
		return this.numericalAttributes.get(attribute);
	}
	
	public abstract byte[] getSignedPortion();
	
	@Override
	public abstract String toString();
	
	@Override
	public abstract boolean isValid();

	@Override
	public String getTypeOfGood() {
		return this.goodType;
	}
	
	public Account getRecepientAccount() {
		return this.recepient;
	}
	
	public Account getEmployeeAccount() {
		return this.employee;
	}
	
	/**
	 * Note that the signature produced will be different each time
	 * due to the fact we are using ECDSA (select a K at random)
	 */
	public void signEmployee() {
		try {
			this.employeeSignature = this.employee.sign(this.getSignedPortion());
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			e.printStackTrace();
			throw new RuntimeException("Fatal Error - can't sign");
		};
	}
	
	/**
	 * Note that the signature produced will be different each time
	 * due to the fact we are using ECDSA (select a K at random)
	 */
	public void signRecipient() {
		try {
			this.recepientSignature = this.recepient.sign(this.getSignedPortion());
		} catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchProviderException
				| SignatureException e) {
			e.printStackTrace();
			throw new RuntimeException("Fatal Error - can't sign");
		};
	}

	@Override
	public boolean isSigned() {
		if(this.recepientSignature != null && this.employeeSignature != null) {
			byte[] message = this.getSignedPortion();
			try {
				return this.employee.checkSignature(message, this.employeeSignature) 
						&& this.recepient.checkSignature(message, recepientSignature);
			} catch (InvalidKeyException | SignatureException e) {
				e.printStackTrace();
				return false;
			}
		}
		else {
			return false;
		}
	}

	@Override
	public Date dateCreated() {
		return this.dateCreated;
	}
	
	@Override 
	public void setDateCreated(Date date) {
		this.dateCreated = date;
	}
	
	@Override
	public BitSet getCategoricalAttributes() {
		// make sure to not return a mutable reference
		return (BitSet) this.categoricalAttributes.clone();
	}
	
	@Override
	public Map<String, Integer> getNumericalAttributes(){
		// this creates a copy which is safe to mutate
		// (strings and integers are immutable types in Java)
		return new HashMap<String, Integer>(this.numericalAttributes);
	}
	
	protected StringBuilder getStringHelper() {
		StringBuilder stringRep = new StringBuilder();
		stringRep.append("Recepient: ");
		stringRep.append(this.recepient.getName());
		stringRep.append("\t");
		stringRep.append(this.recepientSignature);
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append("Employee: ");
		stringRep.append(this.employee.getName());
		stringRep.append("\t");
		stringRep.append(this.employeeSignature);
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append("Good: ");
		stringRep.append(this.goodType);
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append("Date Created: ");
		stringRep.append(this.dateCreated);
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append("Categorical Attributes: ");
		stringRep.append(this.categoricalAttributes);
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append("Numerical Attributes: ");
		for( Entry<String, Integer> entry :this.numericalAttributes.entrySet() ) {
			stringRep.append(System.getProperty("line.separator"));
			stringRep.append(entry.getKey());
			stringRep.append("\t");
			stringRep.append(entry.getValue());
		}
		return stringRep;
	}

}
