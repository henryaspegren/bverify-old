package org.bverify.records;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class Transfer implements Record {

	private static final long serialVersionUID = 2L;
	
	private Date dateCreated;
	private final String goodType;
	private final Account sender;
	private final Account recepient;
	
	private byte[] senderSignature;
	private byte[] recepientSignature;
	
	private final BitSet categoricalAttributes;
	private final Map<String, Integer> numericalAttributes;
	
	public Transfer(String goodType, int amount, Account sender, Account recepient) {
		this.goodType = goodType;
		this.sender = sender;
		this.recepient = recepient;
		this.dateCreated = new Date();
		this.categoricalAttributes = new BitSet(RecordAggregation.NUM_ATTRBUTES);
		this.numericalAttributes  = new HashMap<String, Integer>();
		this.numericalAttributes.put(Record.totalAmount, amount);
		this.numericalAttributes.put(Record.netAmount, 0);
	}
	
	@Override
	public BitSet getCategoricalAttributes() {
		// return a copy to avoid returning a mutable reference
		return (BitSet) this.categoricalAttributes.clone();
	}
	
	@Override
	public int getTotalAmount() {
		return this.numericalAttributes.get(Record.totalAmount);
	}

	@Override
	public int getNetChange() {
		return this.numericalAttributes.get(Record.netAmount);
	}

	public String getTypeOfGood() {
		return this.goodType;
	}
	
	public void signSender() {
		try {
			this.senderSignature = this.sender.sign(this.getSignedPortion());
		} catch (InvalidKeyException | NoSuchProviderException
				| NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
		} 
	}
	
	
	// Recipient 
	public void signRecipient() {
		try {
			this.recepientSignature = this.recepient.sign(this.getSignedPortion());
		} catch (InvalidKeyException | NoSuchProviderException
				| NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
		} 
	}

	@Override
	public boolean isSigned() {
		if(this.recepientSignature != null && this.senderSignature != null) {
			byte[] message = this.getSignedPortion();
			try {
				return this.sender.checkSignature(message, this.senderSignature) 
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

	@Override
	public boolean isValid() {
		if( this.numericalAttributes.get(Record.totalAmount) > 0 
				&& this.numericalAttributes.get(Record.netAmount) 
				== 0 && this.isSigned()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	@Override
	public Date dateCreated() {
		return this.dateCreated;
	}
	
	public Account getSender() {
		return this.sender;
	}
	
	public Account getRecepient() {
		return this.recepient;
	}
	
	@Override
	public void setDateCreated(Date date) {
		this.dateCreated = date;
	}
	
	public byte[] getSignedPortion(){
		byte[] recordType = "TRANSFER".getBytes();
		byte[] goodType = this.goodType.getBytes();
		byte[] senderid = Longs.toByteArray(this.sender.getId());
		byte[] accountid = Longs.toByteArray(this.recepient.getId());
		byte[] amount = Ints.toByteArray(this.numericalAttributes.get(Record.totalAmount));
		byte[] date = this.dateCreated.toString().getBytes();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		byte[] messageBytes;
		try {
			outputStream.write( recordType );
			outputStream.write( goodType );
			outputStream.write( senderid );
			outputStream.write( accountid );
			outputStream.write( amount );
			outputStream.write( date );
			messageBytes = outputStream.toByteArray( );	

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			messageBytes = "ERROR!!!!".getBytes();
		}

		return messageBytes;
		
	}
	
	@Override
	public String toString() {
		StringBuilder stringRep = new StringBuilder();
		stringRep.append("TRANSFER");
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append("Recepient: ");
		stringRep.append(this.recepient.getName());
		stringRep.append("\t");
		stringRep.append(this.recepientSignature);
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append("Sender: ");
		stringRep.append(this.sender.getName());
		stringRep.append("\t");
		stringRep.append(this.senderSignature);
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append("Good: ");
		stringRep.append(this.goodType);
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append("Date Created: ");
		stringRep.append(this.dateCreated);
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
		return stringRep.toString();		
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof Transfer) {
			Transfer ar = (Transfer) arg0;
			if(ar.dateCreated.equals(this.dateCreated) &&
					ar.goodType.equals(this.goodType) &&
					ar.numericalAttributes.equals(this.numericalAttributes) &&
					ar.recepient.equals(this.recepient) &&
					ar.sender.equals(this.sender)
			) {
				return true;
			}
		}
		return false;
	}

	@Override
	public int getNumericalAttribute(String attribute) {
		return this.numericalAttributes.get(attribute);
	}
	
	@Override
	public Map<String, Integer> getNumericalAttributes(){
		return new HashMap<String, Integer>(this.numericalAttributes);
	}

}
