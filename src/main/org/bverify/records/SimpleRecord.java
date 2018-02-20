package org.bverify.records;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

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
	
	public static final int MAX_NUM = 100;
	public static final int MIN_NUM = -100;
	
	// set a seed
	public static final Random prng = new Random(10401);
		
	
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
			int randomInt = getValInRange(prng, MAX_NUM, MIN_NUM);
			boolean randomBoolean = prng.nextBoolean();
			this.numericalAttributes.setAttribute(i, randomInt);
			this.categoricalAttributes.setAttribute(i, randomBoolean);
		}
		this.datecreated = new Date();
	}
	
	public SimpleRecord(int numNumericalAttributes, int numCategoricalAttributes, Random prngRandomness) {
		this.numericalAttributes = new NumericalAttributes(numNumericalAttributes);
		this.categoricalAttributes = new CategoricalAttributes(numCategoricalAttributes);
		for(int i = 0; i < numNumericalAttributes; i++) {
			int randomInt = getValInRange(prngRandomness, MAX_NUM, MIN_NUM);
			boolean randomBoolean = prngRandomness.nextBoolean();
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
	
	
	/**
	 * Produces a list of (pseudo) random records using the given seed value and params
	 * @param seed - for the same selection of the seed, the same list of records will be produced
	 * @param numberOfRecords
	 * @param numNumericalAttributes
	 * @param numCategoricalAttributes
	 * @param date - the date these records are created
	 * @return
	 */
	public static List<SimpleRecord> simpleRecordFacotry(int seed, int numberOfRecords, 
			int numNumericalAttributes, int numCategoricalAttributes, Date date) {
		Random prng = new Random(seed);
		List<SimpleRecord> res = new ArrayList<SimpleRecord>();
		for(int i = 0; i < numberOfRecords; i++) {
			SimpleRecord sr = new SimpleRecord(numNumericalAttributes, numCategoricalAttributes, prng);
			sr.setDateCreated(date);
			res.add(sr);
		}
		return res;
		
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
	
	/**
	 * Select a number uniformly at random in the range [min, max]
	 * inclusive
	 * @param prng - the source of randomness to use
	 * @param max - any integer, can also be negative
	 * @param min - any integer, can be negative
	 * @return
	 */
	private static int getValInRange(Random prng, int max, int min) {
		 int r = prng.nextInt((max - min) + 1) + min;
		 return r;
	}
		
}
