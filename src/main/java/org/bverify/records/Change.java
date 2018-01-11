package org.bverify.records;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.Date;

import org.bverify.accounts.Account;


public abstract class Change implements Record {

	private static final long serialVersionUID = 1L;
	protected final Date dateCreated;
	protected final String goodType;
	protected final int amount;
	protected final Account recepient;
	protected final Account employee;
	
	protected byte[] recepientSignature;
	protected byte[] employeeSignature;
		
	public Change(String goodType, int amount, Account recepient, Account employee) {
		this.goodType = goodType;
		this.amount = amount;
		this.recepient = recepient;
		this.employee = employee;
		this.recepientSignature = null;
		this.employeeSignature = null;
		this.dateCreated = new Date();
	}

	public abstract int getTotalAmount();

	public abstract int getNetChange();
	
	public abstract byte[] getSignedPortion();
	
	public abstract String toString();
	
	public abstract boolean isValid();

	
	public String getTypeOfGood() {
		return this.goodType;
	}
	
	public Account getRecepientAccount() {
		return this.recepient;
	}
	
	public Account getEmployeeAccount() {
		return this.employee;
	}
	
	public void signEmployee() {
		try {
			this.employeeSignature = this.employee.sign(this.getSignedPortion());
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void signRecipient() {
		try {
			this.recepientSignature = this.recepient.sign(this.getSignedPortion());
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SignatureException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	public boolean isSigned() {
		if(this.recepientSignature != null && this.employeeSignature != null) {
			byte[] message = this.getSignedPortion();
			try {
				return this.employee.checkSignature(message, this.employeeSignature) 
						&& this.recepient.checkSignature(message, recepientSignature);
			} catch (InvalidKeyException e) {
				e.printStackTrace();
				return false;
			} catch (SignatureException e) {
				e.printStackTrace();
				return false;
			}
		}
		else {
			return false;
		}
	}

	public Date dateCreated() {
		return this.dateCreated;
	}
	
	

}
