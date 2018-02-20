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

public class Withdrawal extends Change {

	private static final long serialVersionUID = 3L;

	public Withdrawal(String goodType, int amount, Account recepient, Account employee) {
		super(goodType, -1*amount, amount, recepient, employee);
	}
	
	public Withdrawal() {
	}

	@Override
	public boolean isValid() {
		// later can be made richer and possible depend on 
		// current state, etc. 
		if( this.getNetChange()< 0 &&
				this.getTotalAmount()> 0 && 
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
		Withdrawal wd = new Withdrawal(this.goodType, this.getNumericalAttributes().getAttribute(0), this.recepient, 
				this.employee);
		wd.employeeSignature = Arrays.copyOf(this.employeeSignature, this.employeeSignature.length);
		wd.recepientSignature = Arrays.copyOf(this.recepientSignature, this.recepientSignature.length);
		wd.dateCreated = this.dateCreated;
		return wd;
	}

	@Override
	public byte[] serializeRecord() {
		BverifySerialization.Record.Builder builder = BverifySerialization.Record.newBuilder();
		builder.setCategoricalAttributes(this.categoricalAttributes.serializeCategoricalAttributes());
		builder.setNumericalAttributes(this.numericalAttributes.serializeNumericalAttributes());
		builder.setDateCreated(this.dateCreated.getTime());
		builder.setRecordType(BverifySerialization.Record.Type.WITHDRAWAL);
		builder.setOtherData(ByteString.copyFrom(SerializationUtils.serialize(this)));
		return builder.build().toByteArray();
	}


	@Override
	public void parseFrom(byte[] data) throws InvalidProtocolBufferException {
		BverifySerialization.Record message = BverifySerialization.Record.parseFrom(data);
		Withdrawal wd = SerializationUtils.deserialize(message.getOtherData().toByteArray());
		this.categoricalAttributes = wd.categoricalAttributes;
		this.dateCreated = wd.dateCreated;
		this.employee = wd.employee;
		this.employeeSignature = wd.employeeSignature;
		this.goodType = wd.goodType;
		this.numericalAttributes = wd.numericalAttributes;
		this.recepient = wd.recepient;
		this.recepientSignature = wd.recepientSignature;
	}
	
}