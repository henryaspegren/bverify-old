package org.bverify.aggregators;

import org.bverify.records.Record;

import com.google.protobuf.ByteString;

import edu.rice.historytree.AggregationInterface;

public class CryptographicRecordAggregator implements AggregationInterface<RecordAggregation, Record> {

	public String getName() {
		return "CryptoRecordAgg";
	}

	public String getConfig() {
		return "";
	}
	
	public AggregationInterface<RecordAggregation, Record> setup(String config) {
		return this;
	}

	public RecordAggregation aggChildren(RecordAggregation leftAnn, RecordAggregation rightAnn) {
		return new RecordAggregation(leftAnn, rightAnn);
	}

	public RecordAggregation aggVal(Record event) {
		return new RecordAggregation(event);
	}

	public RecordAggregation emptyAgg() {
		return new RecordAggregation();
	}

	
	/** 
	 * These require using google protobuf
	 * TODO: create a serialization format
	 */
	public ByteString serializeVal(Record val) {
		return ByteString.copyFromUtf8(val.toString());
	}

	public ByteString serializeAgg(RecordAggregation agg) {
		return ByteString.copyFromUtf8(agg.toString());
	}

	public RecordAggregation parseAgg(ByteString b) {
		// TODO Auto-generated method stub
		return null;
	}

	public Record parseVal(ByteString b) {
		// TODO Auto-generated method stub
		return null;
	}

	public AggregationInterface<RecordAggregation, Record> clone() {
		// this aggregator is stateless
		return this;
	}


}
