package org.bverify.records;

import java.io.Serializable;
import java.util.Date;

import org.bverify.serialization.BverifySerialization;

import com.google.protobuf.InvalidProtocolBufferException;


/**
 * Interface for the records that <b>bverify</b> stores.
 * For now we model records as having 2 types of attributes:
 * 			<li> categorical attributes - booleans  </li>
 * 			<li> numerical attributes - integers </li>
 * See {@link org.bverify.records.NumericalAttributes} and 
 * 		{@link org.bverify.records.CategoricalAttriubtes} for details
 * @author henryaspegren
 *
 */
public interface Record extends Serializable {
	
	/**
	 * Make a deep copy of this record
	 * @return
	 */
	public Record deepCopy();

	/**
	 * Returns a copy of the categorical attributes of this record
	 * (copy is safe to mutate)
	 * @return
	 */
	public CategoricalAttributes getCategoricalAttributes();
	
	/**
	 * Returns a copy of the numerical attributes of this records
	 * (copy is safe to mutate)
	 * @return
	 */
	public NumericalAttributes getNumericalAttributes();
	
	/**
	 * Returns true if the record, as a whole is valid
	 * and false if it is invalid. 
	 * @return
	 */
	public boolean isValid();
	
	/**
	 * Returns the date the record was created 
	 * @return
	 */
	public Date dateCreated();
	
	/**
	 * Set the date of creation of a record.
	 * @param date - the date of creation
	 */
	public void setDateCreated(Date date);
	
	
	/**
	 * Serialize the Record to a byte array
	 * @return
	 */
	public byte[] serializeRecord();
	
	
	/**
	 * Parse the byte data (serialized record)
	 * into a Record
	 * @param data
	 * @throws InvalidProtocolBufferException 
	 */
	public void parseFrom(byte[] data) throws InvalidProtocolBufferException;
	
	
	/**
	 * 
	 * @param data
	 * @return
	 * @throws InvalidProtocolBufferException
	 */
	public static Record parseRecord(byte[] data) throws InvalidProtocolBufferException {
		BverifySerialization.Record message = BverifySerialization.Record.parseFrom(data);
		Record newRecord;
		switch (message.getRecordType()) {
			case SIMPLE_RECORD:
				newRecord = new SimpleRecord();
				newRecord.parseFrom(data);
				return newRecord;
			case DEPOSIT:
				newRecord = new Deposit();
				newRecord.parseFrom(data);
				return newRecord;
			case WITHDRAWAL:
				newRecord = new Withdrawal();
				newRecord.parseFrom(data);
				return newRecord;
			case TRANSFER:
				newRecord = new Transfer();
				newRecord.parseFrom(data);
				return newRecord;
			default:
				throw new InvalidProtocolBufferException("No serialization avaialble");
			}
	}
		
}


