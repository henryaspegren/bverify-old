package org.bverify.records;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.util.Arrays;

import org.bverify.accounts.Account;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class Transfer extends RecordBase  {

	private static final long serialVersionUID = 3L;
	
	private final Account sender;
	private final Account recepient;
	private byte[] senderSignature;
	private byte[] recepientSignature;
		
	public Transfer(String goodType, int amount, Account sender, Account recepient) {
		super(new CategoricalAttributes(), new NumericalAttributes(), goodType);
		this.sender = sender;
		this.recepient = recepient;
		this.numericalAttributes.setAttribute(1, 0);
		this.numericalAttributes.setAttribute(0, amount);
	}
	
	public void signSender() {
		try {
			this.senderSignature = this.sender.sign(this.getSignedPortion());
		} catch (InvalidKeyException | NoSuchProviderException
				| NoSuchAlgorithmException | SignatureException e) {
			e.printStackTrace();
		} 
	}
	
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
		if( this.getTotalAmount() > 0 
				&& this.getNetChange()
				== 0 && this.isSigned()) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public Account getSender() {
		return this.sender;
	}
	
	public Account getRecepient() {
		return this.recepient;
	}
	
	@Override
	public byte[] getSignedPortion(){
		byte[] recordType = "TRANSFER".getBytes();
		byte[] goodType = this.goodType.getBytes();
		byte[] senderid = Longs.toByteArray(this.sender.getId());
		byte[] accountid = Longs.toByteArray(this.recepient.getId());
		byte[] amount = Ints.toByteArray(this.getTotalAmount());
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
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append(this.categoricalAttributes);
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append(this.numericalAttributes);
		return stringRep.toString();		
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof Transfer) {
			Transfer ar = (Transfer) arg0;
			if(ar.dateCreated.equals(this.dateCreated) &&
					ar.goodType.equals(this.goodType) &&
					ar.numericalAttributes.equals(this.numericalAttributes) &&
					ar.categoricalAttributes.equals(this.categoricalAttributes) &&
					ar.recepient.equals(this.recepient) &&
					ar.sender.equals(this.sender)
			) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public Record deepCopy() {
		Transfer tf = new Transfer(this.goodType, this.getNumericalAttributes().getAttribute(0), this.sender, 
				this.recepient);
		tf.senderSignature = Arrays.copyOf(this.senderSignature, this.senderSignature.length);
		tf.recepientSignature = Arrays.copyOf(this.recepientSignature, this.recepientSignature.length);
		tf.dateCreated = this.dateCreated;
		return tf;
	}

	@Override
	public byte[] serializeRecord() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void parseFrom(byte[] data) {
		// TODO Auto-generated method stub
		
	}
}
