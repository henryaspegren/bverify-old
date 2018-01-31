package org.bverify.benchmarks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.SerializationUtils;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;
import org.bverify.records.SimpleRecord;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;


/**
 * Class for benchmarking the size of history trees 
 * and observing how this scales with the number of 
 * records and the number of attributes 
 * @author henryaspegren
 *
 */
public class HistoryTreeBenchmarks {
	
	/**
	 * Creates a history tree with the specified number 
	 * of records. Records are generated randomly 
	 * @param numberOfRecords 
	 * @param numberOfCategoricalAttributes	 - number of categorical attributes in the records.
	 * 			assigned values randomly 
	 * @param numberOfNumericalAttributes - number of numerical attributes in the records.
	 * 			numerical values chosen randomly
	 * @return
	 */
	public static HistoryTree<RecordAggregation, Record> makeHistoryTreeWithRecords(int numberOfRecords,
			int numberOfCategoricalAttributes, int numberOfNumericalAttributes){
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation,Record>();    
		HistoryTree<RecordAggregation, Record> histtree = new HistoryTree<RecordAggregation, Record>(aggregator, store);
		for(int i = 0; i < numberOfRecords; i++) {
			// creates a random record 
			Record record = new SimpleRecord(numberOfNumericalAttributes, numberOfCategoricalAttributes);
			histtree.append(record);
		}
		return histtree;
		
	}
	
	public static void simpleRecordSizes(int max, String fileName) {
        try(
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("NumberNumerical", "NumberCategorical", "Size"))
		;) 
        {
        	
            for(int numNum = 1; numNum <= max; numNum = numNum*2) {
	    		for(int numCat = 1; numCat <= max; numCat = numCat*2) {
	    			SimpleRecord sr = new SimpleRecord(numNum, numCat);
	    			int size = SerializationUtils.serialize(sr).length;
	    			System.out.println("[Simple Record] Numerical: "+numNum+" Categorical: "+numCat+
	    					"\t\t| Size : "+size);
	    			csvPrinter.printRecord(numNum, numCat, size);
	    		}
            }		
            csvPrinter.flush();            
        } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void recordAggregationSizes(int max, String fileName) {
        try(
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("NumberNumerical", "NumberCategorical", "Size"))
		;) 
        {
    		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
            for(int numNum = 1; numNum <= max; numNum = numNum*2) {
	    		for(int numCat = 1; numCat <= max; numCat = numCat*2) {
	    			SimpleRecord sr = new SimpleRecord(numNum, numCat);
	    			RecordAggregation agg = aggregator.aggVal(sr);
	    			int size = SerializationUtils.serialize(agg).length;
	    			System.out.println("[Record Aggregation] Numerical: "+numNum+" Categorical: "+numCat+
	    					"\t\t| Size : "+size);
	    			csvPrinter.printRecord(numNum, numCat, size);
	    		}
            }		
            csvPrinter.flush();            
        } catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void fullHistoryTreeSizes(int maxRecords, int maxAttributes, String fileName) {
        try(
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("NumberRecords", "NumberAttributes", "Size"))
		;) 
        {
            for(int numRecords = 1; numRecords <= maxRecords; numRecords = numRecords*2) {
	    		for(int numAtts = 1; numAtts <= maxAttributes; numAtts = numAtts*2) {
					HistoryTree<RecordAggregation, Record> res = makeHistoryTreeWithRecords(numRecords,
							numAtts, numAtts);
	    			int size = res.serializeTree().length;
	    			System.out.println("[History Tree] Records: "+numRecords+" Attributes: "+numAtts+
	    					"\t\t| Size : "+size);
	    			csvPrinter.printRecord(numRecords, numAtts, size);
	    		}
            }		
            csvPrinter.flush();            
        } catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void prunedHistoryTree(int maxRecords, int maxAttributes, String fileName) {
    try(
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
            CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                    .withHeader("NumberRecords", "NumberAttributes", "Size"))
	;) 
    {
    	Random r = new Random();
        for(int numRecords = 1; numRecords <= maxRecords; numRecords = numRecords*2) {
    		for(int numAtts = 1; numAtts <= maxAttributes; numAtts = numAtts*2) {
				HistoryTree<RecordAggregation, Record> res = makeHistoryTreeWithRecords(numRecords,
						numAtts, numAtts);
				HistoryTree<RecordAggregation, Record> pruned = res.makePruned(
						new ArrayStore<RecordAggregation, Record>(), 
						res.version());
				// copy a random leaf
				int leafCopied = r.nextInt(res.version()+1);
				pruned.copyV(res, leafCopied, true);
    			int size = pruned.serializeTree().length;
    			System.out.println("[Pruned History Tree] Records: "+numRecords+" Attributes: "+numAtts+
    					" Copied: "+leafCopied+" Size : "+size);
    			csvPrinter.printRecord(numRecords, numAtts, size);
    		}
        }		
        csvPrinter.flush();            
    } catch (IOException | ProofError e) {
		e.printStackTrace();
	}
}
	/**
	 * Run the benchmarking code
	 * @param args
	 */
	public static void main(String[] args) {
		//simpleRecordSizes(10000, "./analysis/benchmarking/simple_record_size.csv");
		//recordAggregationSizes(10000, "./analysis/benchmarking/record_aggregation_size.csv");
		//fullHistoryTreeSizes(40000, 1000, "./analysis/benchmarking/full_history_tree_size.csv");
		//prunedHistoryTree(40000, 1000, "./analysis/benchmarking/pruned_history_tree_size.csv");
		
		
		SimpleRecord sr = new SimpleRecord(1, 1);
		int srlength = sr.serializeRecord().length;
		System.out.println(sr);
		System.out.println("SIMPLE RECORD - "+srlength);
				
		CryptographicRecordAggregator cagg = new CryptographicRecordAggregator();
		RecordAggregation recordAgg = cagg.aggVal(sr);
		System.out.println("AGG(SIMPLE RECORD) - "+recordAgg.serializatRecordAggregation().length);
		
		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation,Record>();    
		HistoryTree<RecordAggregation, Record> histtree = new HistoryTree<RecordAggregation, Record>(aggregator, store);		
		ArrayList<Record> listOfRecords = new ArrayList<Record>();
		int totalRecordSerialization = 0;
		for(int i = 0; i < 100000; i++) {
			SimpleRecord newsr = new SimpleRecord(1, 1);
			totalRecordSerialization = totalRecordSerialization+  newsr.serializeRecord().length;
			histtree.append(newsr);
			listOfRecords.add(newsr);
		}
		System.out.println("TOTAL RECORDS: "+totalRecordSerialization);
		int length = histtree.serializeTree().length;
		int lengthList = SerializationUtils.serialize(listOfRecords).length;
		System.out.println("HISTORY TREE<"+histtree.version()+">: " +length + 
				" - "+length/(double) totalRecordSerialization);
		System.out.println("LIST<"+listOfRecords.size()+">: "+ lengthList + 
				" - "+lengthList/(double) totalRecordSerialization);
				
	

		
	}
	
	
}
