package org.bverify.records;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.bverify.accounts.Account;

import com.google.common.primitives.Longs;

public class Withdrawal extends Change {
	
	public Withdrawal(String goodType, int amount, Account recepient, Account employee) {
		super(goodType, amount, recepient, employee);
	}

	public int getTotalAmount() {
		return -1*this.amount;
	}

	public int getNetChange() {
		return this.amount;
	}
	
	public Account getRecepientAccount() {
		return this.recepient;
	}
	
	public Account getEmployeeAccount() {
		return this.employee;
	}
	
	public boolean isValid() {
		// later can be made richer and possible depend on 
		// current state, etc. 
		if( this.amount < 0 & this.isSigned()) {
			return true;
		}
		else {
			return false;
		}
	}

	// TODO: Needs to be redone with Google protobuf
	// this will implement the serialization and deserialization 
	// for me!
	public byte[] getSignedPortion(){
		byte[] recordType = "WITHDRAWAL".getBytes();
		byte[] goodType = this.goodType.getBytes();
		byte[] employeeid = Longs.toByteArray(this.employee.getId());
		byte[] accountid = Longs.toByteArray(this.recepient.getId());
		byte amount = (byte) this.amount;
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
	
	public String toString() {
		StringBuilder stringRep = new StringBuilder();
		stringRep.append("WITHDRAWAL");
		stringRep.append(System.getProperty("line.separator"));
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
		stringRep.append("Amount: ");
		stringRep.append(this.amount);
		stringRep.append(System.getProperty("line.separator"));
		stringRep.append("Date Created: ");
		stringRep.append(this.dateCreated);
		return stringRep.toString();
	}
	
	
	

}