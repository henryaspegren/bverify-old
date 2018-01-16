package org.bverify.records;

import java.io.Serializable;
import java.util.Date;


/**
 * Interface for the transaction records that <b>bverify</b> stores.
 * Records modify the state of the warehouse (e.g. by withdrawing goods
 * from the warehouse)
 * 
 * @author henryaspegren
 *
 */
public interface Record extends Serializable {
	
	/**
	 * Get the total amount of goods referenced in this record. These
	 * could be loaned, deposited, withdrawn, etc
	 * @return
	 */
	public int getTotalAmount();
	
	/**
	 * Get the net change in the number of units stored by the warehouse reflected
	 * by this record
	 * @return
	 */
	public int getNetChange();
		
	/**
	 * Get the type of good referenced in this record. 
	 * For now goods are identified by a string
	 * @return
	 */
	public String getTypeOfGood();
	
	/**
	 * Returns true if the record has been signed by the required parties
	 * and false if it has not been signed
	 * @return
	 */
	public boolean isSigned();
	
	/**
	 * Returns true if the record, as a whole is valid
	 * and false if it is invalid 
	 * 
	 * TODO: define what makes a record valid v.s. invalid
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
}
