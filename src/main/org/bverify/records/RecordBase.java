package org.bverify.records;

import java.util.Date;

/**
 * Abstract base class for creating records that provides
 * the attribute management that records must provide. 
 * @author henryaspegren
 *
 */
public abstract class RecordBase implements Record {
	
	private static final long serialVersionUID = 3L;

	// should be later moved over to a categorical attribute
	protected String goodType; 
	
	// basic record data
	protected Date dateCreated;
	
	// attributes
	protected CategoricalAttributes categoricalAttributes;
	protected NumericalAttributes numericalAttributes;
	
	public RecordBase(CategoricalAttributes categoricalAttributes,
			NumericalAttributes numericalAttributes, 
			String goodType) {
		this.categoricalAttributes = categoricalAttributes;
		this.numericalAttributes = numericalAttributes;
		this.goodType = goodType;
		this.dateCreated = new Date();
	}
	
	public RecordBase() {
	}

	/** These must be implemented and depend on the record type */

	public abstract boolean isSigned();

	@Override
	public abstract boolean isValid();
	
	public abstract byte[] getSignedPortion();
	
	@Override
	public CategoricalAttributes getCategoricalAttributes() {
		return new CategoricalAttributes(
				this.categoricalAttributes);
	}

	@Override
	public NumericalAttributes getNumericalAttributes() {
		return new NumericalAttributes(this.numericalAttributes);
	}

	public int getTotalAmount() {
		return this.numericalAttributes.getAttribute(0);
	}

	public int getNetChange() {
		return this.numericalAttributes.getAttribute(1);
	}

	public String getTypeOfGood() {
		return this.goodType;
	}

	@Override
	public Date dateCreated() {
		return this.dateCreated;
	}

	@Override
	public void setDateCreated(Date date) {
		this.dateCreated = date;
	}

}
