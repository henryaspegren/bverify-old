package org.bverify.records;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bverify.serialization.BverifySerialization;

import com.google.protobuf.InvalidProtocolBufferException;

/**
 * This is a generic record class with numeric and categorical attributes
 * @author henryaspegren
 *
 */
public class SimpleRecord implements Record {
	
	private static final long serialVersionUID = 1L;
	
	private NumericalAttributes numericalAttributes;
	private CategoricalAttributes categoricalAttributes;
	
	private Date datecreated;
	
	public static int MAX_NUM = 100;
	public static int MIN_NUM = -100;
	
	// set a seed
	public static Random prng = new Random(10401);
		
	
	/**
	 * Creates a random simple record containing the specified number of 
	 * numerical and categorical attributes. The value of categorical 
	 * attributes is true 50% of the time and false 50% of the time. 
	 * Numerical attributes are selected uniformly at random in the range 
	 * [MIN_NUM, MAX_NUM]
	 * @param numNumericalAttributes
	 * @param numCategoricalAttributes
	 */
	public SimpleRecord(int numNumericalAttributes, int numCategoricalAttributes) {
		this.numericalAttributes = new NumericalAttributes(numNumericalAttributes);
		this.categoricalAttributes = new CategoricalAttributes(numCategoricalAttributes);
		for(int i = 0; i < numNumericalAttributes; i++) {
			int randomInt = ThreadLocalRandom.current().nextInt(MIN_NUM, MAX_NUM + 1);
			boolean randomBoolean = prng.nextBoolean();
			this.numericalAttributes.setAttribute(i, randomInt);
			this.categoricalAttributes.setAttribute(i, randomBoolean);
		}
		this.datecreated = new Date();
		
		
	}
	
	public SimpleRecord(CategoricalAttributes ca, NumericalAttributes na) {
		this.numericalAttributes = new NumericalAttributes(na);
		this.categoricalAttributes = new CategoricalAttributes(ca);
		this.datecreated = new Date();
	}
	
	// creates an empty record
	public SimpleRecord() {
	}

	@Override
	public CategoricalAttributes getCategoricalAttributes() {
		return new CategoricalAttributes(this.categoricalAttributes);
	}
	
	@Override
	public NumericalAttributes getNumericalAttributes() {
		return new NumericalAttributes(this.numericalAttributes);
	}
	
	@Override
	public boolean isValid() {
		return true;
	}
	
	@Override
	public Date dateCreated() {
		return this.datecreated;
	}
	
	@Override
	public void setDateCreated(Date date) {
		this.datecreated = date;
		
	}
	
	@Override 
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<SimpleRecord: ");
		sb.append("|");
		sb.append(this.numericalAttributes);
		sb.append("|");
		sb.append(this.categoricalAttributes);
		sb.append("|");
		sb.append(this.datecreated);
		sb.append(">");
		return sb.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof SimpleRecord) {
			SimpleRecord cast = (SimpleRecord) other;
			return this.categoricalAttributes.equals(cast.categoricalAttributes) &&
					this.numericalAttributes.equals(cast.numericalAttributes) &&
					this.datecreated.equals(cast.datecreated);
		}
		return false;
	}

	@Override
	public Record deepCopy() {
		SimpleRecord sr =  new SimpleRecord(this.categoricalAttributes, this.numericalAttributes);
		sr.datecreated = this.datecreated;
		return sr;
	}

	@Override
	public byte[] serializeRecord() {
		BverifySerialization.Record.Builder builder = BverifySerialization.Record.newBuilder();
		builder.setCategoricalAttributes(this.categoricalAttributes.serializeCategoricalAttributes());
		builder.setNumericalAttributes(this.numericalAttributes.serializeNumericalAttributes());
		builder.setDateCreated(this.datecreated.getTime());
		builder.setRecordType(BverifySerialization.Record.Type.SIMPLE_RECORD);
		return builder.build().toByteArray();
	}

	@Override
	public void parseFrom(byte[] data) throws InvalidProtocolBufferException {
		BverifySerialization.Record message = BverifySerialization.Record.parseFrom(data);
		this.categoricalAttributes = CategoricalAttributes.parseCategoricalAttributes(message.getCategoricalAttributes().toByteArray());
		this.numericalAttributes = NumericalAttributes.parseNumericalAttributes(message.getNumericalAttributes().toByteArray());
		this.datecreated = new Date(message.getDateCreated());
	}
	
	
	
		
}
