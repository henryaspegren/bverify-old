package org.bverify.records;

import java.io.Serializable;
import java.util.BitSet;
import java.util.Date;
import java.util.Map;


/**
 * Interface for the records that <b>bverify</b> stores.
 * For now we model records as having 2 types of attributes:
 * 			- categorical attributes - booleans 
 * 			- numerical attributes 	 - integers
 * 
 * @author henryaspegren
 *
 */
public interface Record extends Serializable {
	
	public static String totalAmount = "totalAmount";
	public static String netAmount = "netAmount";
	
	/**
	 * Return a bit field of the categorical attributes of this record
	 * (Returns a copy that is safe to mutate)
	 * @return
	 */
	public BitSet getCategoricalAttributes();
	
	/**
	 * Returns a map of the numerical attributes of this record. 
	 * (Numerical attributes are represented as mappings of strings to ints)
	 * @return
	 */
	public Map<String, Integer> getNumericalAttributes();
	
	/**
	 * Return the numerical attribute value. Numerical attributes
	 * are identified by strings 
	 * @param attribute - The numerical attribute to get the value of
	 * @return may return null if no such attribute exists
	 */
	public int getNumericalAttribute(String attribute);
	
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
