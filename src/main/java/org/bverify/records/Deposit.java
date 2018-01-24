package org.bverify.records;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bverify.accounts.Account;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

public class Deposit extends Change {
	
	private static final long serialVersionUID = 2L;

	public Deposit(String goodType, int amount, Account recepient, Account employee) {
		super(goodType, amount, amount, recepient, employee);
	}
	
	
	@Override
	public boolean isValid() {
		// later can be made richer and possible depend on 
		// current state, etc. 
		if( this.numericalAttributes.get("totalAmount") > 0 &&
				this.numericalAttributes.get("netAmount") > 0 && this.isSigned()) {
			return true;
		}
		else {
			return false;
		}
	}

	// TODO: Needs to be redone with Google protobuf / in a more general way
	// this will implement the serialization and deserialization 
	// for me!
	@Override
	public byte[] getSignedPortion(){
		byte[] recordType = "DEPOSIT".getBytes();
		byte[] goodType = this.goodType.getBytes();
		byte[] employeeid = Longs.toByteArray(this.employee.getId());
		byte[] accountid = Longs.toByteArray(this.recepient.getId());
		byte[] amount = Ints.toByteArray(this.numericalAttributes.get("totalAmount"));
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
		stringRep.append("--DEPOSIT--");
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append(this.getStringHelper());
		return stringRep.toString();
	}
	
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof Deposit) {
			Deposit ar = (Deposit) arg0;
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
