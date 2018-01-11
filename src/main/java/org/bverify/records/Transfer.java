package org.bverify.records;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.Date;

import org.bverify.accounts.Account;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class Transfer implements Record {

	private static final long serialVersionUID = 1L;
	private final Date dateCreated;
	private final String goodType;
	private final int amount;
	private final Account sender;
	private final Account recepient;
	
	private byte[] senderSignature;
	private byte[] recepientSignature;
	
	public Transfer(String goodType, int amount, Account sender, Account recepient) {
		this.goodType = goodType;
		this.amount = amount;
		this.sender = sender;
		this.recepient = recepient;
		this.dateCreated = new Date();
	}
	
	@Override
	public int getTotalAmount() {
		assert this.amount > 0;
		return this.amount;
	}

	@Override
	public int getNetChange() {
		return 0;
	}

	public String getTypeOfGood() {
		return this.goodType;
	}
	
	public void signSender() {
		try {
			this.senderSignature = this.sender.sign(this.getSignedPortion());
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
	
	
	// Recipient 
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
		if( this.amount > 0 && this.isSigned()) {
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
	
	public byte[] getSignedPortion(){
		byte[] recordType = "TRANSFER".getBytes();
		byte[] goodType = this.goodType.getBytes();
		byte[] senderid = Longs.toByteArray(this.sender.getId());
		byte[] accountid = Longs.toByteArray(this.recepient.getId());
		byte[] amount = Ints.toByteArray(this.amount);
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
		stringRep.append("Amount: ");
		stringRep.append(this.amount);
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append("Date Created: ");
		stringRep.append(this.dateCreated);
		return stringRep.toString();		
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof Transfer) {
			Transfer ar = (Transfer) arg0;
			if(ar.dateCreated.equals(this.dateCreated) &&
					ar.goodType.equals(this.goodType) &&
					ar.amount == this.amount &&
					ar.recepient.equals(this.recepient) &&
					ar.sender.equals(this.sender)
			) {
				return true;
			}
		}
		return false;
	}

}
