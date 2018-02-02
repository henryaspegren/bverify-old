package org.bverify.benchmarks;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
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
public class HistoryTreeSizeBenchmarks {
	
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
	
	public static List<Record> makeListOfRecords(int numberOfRecords, int numberOfCategoricalAttributes,
			int numberOfNumericalAttributes){
		List<Record> res = new ArrayList<Record>();
		for(int i = 0; i < numberOfRecords; i++) {
			// creates a random record 
			Record record = new SimpleRecord(numberOfNumericalAttributes, numberOfCategoricalAttributes);
			res.add(record);
		}
		return res;
	}
	
	public static int getSizeOfRecords(int numberOfRecords, int numberOfCategoricalAttributes,
			int numberOfNumericalAttributes){
		int size = 0;
		for(int i = 0; i < numberOfRecords; i++) {
			// creates a random record 
			Record record = new SimpleRecord(numberOfNumericalAttributes, numberOfCategoricalAttributes);
			size = size + record.serializeRecord().length;
		}
		return size;
	}	
	
	public static void recordandRecordAggregationSizes(int max, String fileName) {
        try(
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("NumberNumerical", "NumberCategorical", "RecordSize", "RecordAggregationSize"))
		;) 
        {
    		CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
            for(int numNum = 1; numNum <= max; numNum = numNum*2) {
	    		for(int numCat = 1; numCat <= max; numCat = numCat*2) {
	    			SimpleRecord sr = new SimpleRecord(numNum, numCat);
	    			RecordAggregation agg = aggregator.aggVal(sr);
	    			int recordAggregationSize = agg.serializatRecordAggregation().length;
	    			int recordSize = sr.serializeRecord().length;
	    			System.out.println("Numerical: "+numNum+" Categorical: "+numCat+
	    					"\t\t| RecordSize: "+recordSize + " RecordAggregationSize: "+recordAggregationSize);
	    			csvPrinter.printRecord(numNum, numCat, recordSize, recordAggregationSize);
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
	
	public static void rawRecordsSizes(int maxRecords, int maxAttributes, String fileName) {
        try(
                BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
                CSVPrinter csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT
                        .withHeader("NumberRecords", "NumberAttributes", "Size"))
		;) 
        {
            for(int numRecords = 1; numRecords <= maxRecords; numRecords = numRecords*2) {
	    		for(int numAtts = 1; numAtts <= maxAttributes; numAtts = numAtts*2) {
					int size = getSizeOfRecords(numRecords, numAtts, numAtts);
	    			System.out.println("[Raw Records] Records: "+numRecords+" Attributes: "+numAtts+
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
		//recordandRecordAggregationSizes(1000, "./analysis/benchmarking/record_and_record_aggregation_size.csv");
		//rawRecordsSizes(10000, 1000, "./analysis/benchmarking/raw_records_size.csv");
		//fullHistoryTreeSizes(100000, 128, "./analysis/benchmarking/full_history_tree_size.csv");
		//prunedHistoryTree(100000, 128, "./analysis/benchmarking/pruned_history_tree_size.csv");
	}
	
	
}
