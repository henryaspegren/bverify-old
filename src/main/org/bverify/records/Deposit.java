package org.bverify.records;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.lang3.SerializationUtils;
import org.bverify.accounts.Account;
import org.bverify.serialization.BverifySerialization;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

public class Deposit extends Change {
	
	private static final long serialVersionUID = 3L;

	public Deposit(String goodType, int amount, Account recepient, Account employee) {
		super(goodType, amount, amount, recepient, employee);
	}
	
	
	public Deposit() {
		super();
	}


	@Override
	public boolean isValid() {
		// later can be made richer and possible depend on 
		// current state, etc. 
		if( this.getTotalAmount() > 0 &&
				this.getNetChange() > 0 && this.isSigned()) {
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
		byte[] amount = Ints.toByteArray(this.getTotalAmount());
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
					ar.categoricalAttributes.equals(this.categoricalAttributes) &&
					ar.recepient.equals(this.recepient) &&
					ar.employee.equals(this.employee) 
			) {
				return true;
			}
		}
		return false;
	}


	@Override
	public Record deepCopy() {
		Deposit dep = new Deposit(this.goodType, this.getNumericalAttributes().getAttribute(0), this.recepient, 
				this.employee);
		dep.employeeSignature = Arrays.copyOf(this.employeeSignature, this.employeeSignature.length);
		dep.recepientSignature = Arrays.copyOf(this.recepientSignature, this.recepientSignature.length);
		dep.dateCreated = this.dateCreated;
		return dep;
	}


	@Override
	public byte[] serializeRecord() {
		BverifySerialization.Record.Builder builder = BverifySerialization.Record.newBuilder();
		builder.setCategoricalAttributes(this.categoricalAttributes.serializeCategoricalAttributes());
		builder.setNumericalAttributes(this.numericalAttributes.serializeNumericalAttributes());
		builder.setDateCreated(this.dateCreated.getTime());
		builder.setRecordType(BverifySerialization.Record.Type.DEPOSIT);
		builder.setOtherData(ByteString.copyFrom(SerializationUtils.serialize(this)));
		return builder.build().toByteArray();
	}


	@Override
	public void parseFrom(byte[] data) throws InvalidProtocolBufferException {
		BverifySerialization.Record message = BverifySerialization.Record.parseFrom(data);
		Deposit dep = SerializationUtils.deserialize(message.getOtherData().toByteArray());
		this.categoricalAttributes = dep.categoricalAttributes;
		this.dateCreated = dep.dateCreated;
		this.employee = dep.employee;
		this.employeeSignature = dep.employeeSignature;
		this.goodType = dep.goodType;
		this.numericalAttributes = dep.numericalAttributes;
		this.recepient = dep.recepient;
		this.recepientSignature = dep.recepientSignature;
	}

}
