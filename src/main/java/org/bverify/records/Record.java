package org.bverify.records;

/**
 * Interface for the transaction records that <b>bverify</b> stores.
 * Records modify the state of the warehouse (e.g. by withdrawing goods
 * from the warehouse)
 * 
 * @author henryaspegren
 *
 */
public interface Record {
	
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
	 * Get the total number of units loaned in this record
	 * @return
	 */
	public int getTotalLoaned();
	
	
	/**
	 * Get the type of good referenced in this record. 
	 * For now goods are identified by a string
	 * @return
	 */
	public String getTypeOfGood();
	
}
