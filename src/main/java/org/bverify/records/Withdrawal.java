package org.bverify.records;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bverify.accounts.Account;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class Withdrawal extends Change {

	private static final long serialVersionUID = 2L;

	public Withdrawal(String goodType, int amount, Account recepient, Account employee) {
		super(goodType, -1*amount, amount, recepient, employee);
	}
	
	@Override
	public boolean isValid() {
		// later can be made richer and possible depend on 
		// current state, etc. 
		if( this.getNumericalAttribute("netAmount") < 0 &&
				this.getNumericalAttribute("totalAmount") > 0 && 
				this.isSigned()) {
			return true;
		}
		else {
			return false;
		}
	}

	// TODO: Needs to be redone with Google protobuf
	// this will implement the serialization and deserialization 
	// for me!
	@Override
	public byte[] getSignedPortion(){
		byte[] recordType = "WITHDRAWAL".getBytes();
		byte[] goodType = this.goodType.getBytes();
		byte[] employeeid = Longs.toByteArray(this.employee.getId());
		byte[] accountid = Longs.toByteArray(this.recepient.getId());
		byte[] amount = Ints.toByteArray(this.getNumericalAttribute("totalAmount"));
		byte[] date = this.dateCreated.toString().getBytes();

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream( );
		byte[] messageBytes;
		try {
			outputStream.write( recordType );
			outputStream.write( goodType );
			outputStream.write( employeeid );
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
		stringRep.append("WITHDRAWAL");
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append(this.getStringHelper());
		return stringRep.toString();
	}
	
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof Withdrawal) {
			Withdrawal ar = (Withdrawal) arg0;
			if(ar.dateCreated.equals(this.dateCreated) &&
					ar.goodType.equals(this.goodType) &&
					ar.numericalAttributes.equals(this.numericalAttributes) &&
					ar.recepient.equals(this.recepient) &&
					ar.employee.equals(this.employee)
					) {
				return true;
			}
		}
		return false;
	}

}