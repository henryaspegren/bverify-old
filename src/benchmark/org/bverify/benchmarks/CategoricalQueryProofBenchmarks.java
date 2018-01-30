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
import org.bverify.proofs.CategoricalQueryProof;
import org.bverify.records.CategoricalAttributes;
import org.bverify.records.NumericalAttributes;
import org.bverify.records.Record;
import org.bverify.records.SimpleRecord;

import com.google.common.primitives.UnsignedBytes;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

/**
 * Class for benchmarking the size of categorical queries depending on the
 * number of records and their ordering
 * 
 * @author henryaspegren
 *
 */
public class CategoricalQueryProofBenchmarks {

	/** Use the same seed value so we get consistent benchmarking results */
	public static int SEED_VAL = 91764;

	/**
	 * Produce random records for benchmark
	 * 
	 * @param numberOfRecords
	 *            - number of records to produce
	 * @param numNumericalAttributes
	 *            - number of numerical attributes (all set to 0)
	 * @param numCategoricalAttributes
	 *            - number of categorical attributes, set to true uniformly at
	 *            random with probability p.
	 * @param p
	 *            - the probability of having any given categorical attribute
	 * @return
	 */
	public static ArrayList<Record> getListOfRandomRecords(int numberOfRecords, int numNumericalAttributes,
			int numCategoricalAttributes, double p) {
		assert p <= 1 && p >= 0;

		Random prng = new Random(SEED_VAL);
		ArrayList<Record> res = new ArrayList<Record>();

		for (int i = 0; i < numberOfRecords; i++) {

			NumericalAttributes num = new NumericalAttributes(numNumericalAttributes);
			CategoricalAttributes cat = new CategoricalAttributes(numCategoricalAttributes);
			for (int j = 0; j < cat.numberOfAttributes(); j++) {
				boolean value = false;
				// choose whether it is true or false uniformly at random
				// with a Bernoulli flip with probability p
				if (prng.nextDouble() < p) {
					value = true;
				}
				cat.setAttribute(j, value);
			}

			Record record = new SimpleRecord(cat, num);

			res.add(record);
		}

		return res;
	}

	/**
	 * Sorts all records lexicographically by categorical attributes. This results
	 * in records with similar categorical attributes being located closer to each
	 * other. The sorting window slides along the list of records and sorts the
	 * records within it. If the sorting window is larger than the list the entire
	 * list is sorted. If sortingWindow is 0 than the list is not sorted at all
	 * 
	 * @param records
	 * @param sortingWindow
	 */
	public static void sortListOfRecords(ArrayList<Record> records, int sortingWindow) {
		if (sortingWindow == 0) {
			return;
		}
		if (sortingWindow >= records.size()) {
			records.subList(0, records.size() - 1).sort((o1, o2) -> {
				return UnsignedBytes.lexicographicalComparator().compare(o1.getCategoricalAttributes().toByteArray(),
						o2.getCategoricalAttributes().toByteArray());
			});
			return;
		}
		for (int start = 0; start + sortingWindow < records.size(); start = start + sortingWindow) {
			records.subList(start, start + sortingWindow).sort((o1, o2) -> {
				return UnsignedBytes.lexicographicalComparator().compare(o1.getCategoricalAttributes().toByteArray(),
						o2.getCategoricalAttributes().toByteArray());
			});
		}
	}

	/**
	 * Get stats about best and worst possible performances for comparison
	 * 
	 * @param records
	 * @param filter
	 * @return
	 */
	public static BaselineStats getStats(ArrayList<Record> records, CategoricalAttributes filter) {
		int numberOfRecordsAll = records.size();
		int sizeOfRecordsAll = SerializationUtils.serialize(records).length;
		ArrayList<Record> matching = new ArrayList<Record>();
		for (Record r : records) {
			if (r.getCategoricalAttributes().hasAttributes(filter)) {
				matching.add(r);
			}
		}
		int numberOfRecordsMatching = matching.size();
		int sizeOfRecordsMatching = SerializationUtils.serialize(matching).length;
		return new BaselineStats(numberOfRecordsAll, numberOfRecordsMatching, sizeOfRecordsAll, sizeOfRecordsMatching);

	}

	public static void benchmarkEfficiencyOfProof(int numberOfRecords, String fileName) {
		double[] pvals = { 0.8, 0.5, 0.2, 0.1 };
		int[] sortingWindows = { 0, (int) (numberOfRecords * 0.1), (int) (numberOfRecords * 0.25),
				(int) (numberOfRecords * 0.75), numberOfRecords };

		CategoricalAttributes filter0 = new CategoricalAttributes(3);
		filter0.setAttribute(0, false);
		filter0.setAttribute(1, false);
		filter0.setAttribute(2, false);
		CategoricalAttributes filter1 = new CategoricalAttributes(3);
		filter1.setAttribute(0, true);
		filter1.setAttribute(1, false);
		filter1.setAttribute(2, false);
		CategoricalAttributes filter2 = new CategoricalAttributes(3);
		filter2.setAttribute(0, true);
		filter2.setAttribute(1, true);
		filter2.setAttribute(2, false);
		CategoricalAttributes filter3 = new CategoricalAttributes(3);
		filter3.setAttribute(0, true);
		filter3.setAttribute(1, true);
		filter3.setAttribute(2, true);
		CategoricalAttributes[] filters = { filter0, filter1, filter2, filter3 };
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
				CSVPrinter csvPrinter = new CSVPrinter(writer,
						CSVFormat.DEFAULT.withHeader("Query", "p", "w", "NumberOfRecordsMatching", "ProofSize",
								"SizeOfRecordsMatching", "NumberOfRecordsAll", "SizeOfRecordsAll",
								"SizeOfProofAll"));) {
			for (double p : pvals) {
				for (int w : sortingWindows) {
					for (CategoricalAttributes filter : filters) {
						// get the list of random records
						ArrayList<Record> records = getListOfRandomRecords(numberOfRecords, 1, 3, p);
						// sort it (if needed)
						sortListOfRecords(records, w);
						CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
						ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
						HistoryTree<RecordAggregation, Record> tree = new HistoryTree<RecordAggregation, Record>(
								aggregator, store);
						for (Record r : records) {
							tree.append(r);
						}
						double sizeOfProofAll = tree.serializeTree().length;
						BaselineStats stats = getStats(records, filter);
						CategoricalQueryProof proof = new CategoricalQueryProof(filter, tree, 1, numberOfRecords - 1);
						System.out.println("<---p = " + p +" w = "+w+" filter = " + filter + " --->");

						csvPrinter.printRecord(filter.toString(), p, w, stats.numberOfRecordsMatching,
								proof.getSizeInBytes(), stats.sizeOfRecordsMatching, stats.numberOfRecordsAll,
								stats.sizeOfRecordsAll, sizeOfProofAll);
					}
				}
			}
		} catch (ProofError | IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		benchmarkEfficiencyOfProof(100000, "./analysis/benchmarking/query_proof_size.csv");
	}

}
