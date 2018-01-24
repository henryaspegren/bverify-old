package org.bverify.records;

import java.io.Serializable;
import java.util.Date;


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
	
	/**
	 * Get the signed portion of the record
	 * @return
	 */
	public byte[] getSignedPortion();

}


