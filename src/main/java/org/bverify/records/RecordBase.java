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
	protected final String goodType; 
	
	// basic record data
	protected Date dateCreated;
	
	// attributes
	protected final CategoricalAttributes categoricalAttributes;
	protected final NumericalAttributes numericalAttributes;
	
	public RecordBase(CategoricalAttributes categoricalAttributes,
			NumericalAttributes numericalAttributes, 
			String goodType) {
		this.categoricalAttributes = categoricalAttributes;
		this.numericalAttributes = numericalAttributes;
		this.goodType = goodType;
		this.dateCreated = new Date();
	}
	
	/** These must be implemented and depend on the record type */

	@Override
	public abstract boolean isSigned();

	@Override
	public abstract boolean isValid();
	
	@Override
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

	@Override
	public int getTotalAmount() {
		return this.numericalAttributes.getAttribute(0);
	}

	@Override
	public int getNetChange() {
		return this.numericalAttributes.getAttribute(1);
	}

	@Override
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
