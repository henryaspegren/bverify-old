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
	 * @param other
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
	
}


