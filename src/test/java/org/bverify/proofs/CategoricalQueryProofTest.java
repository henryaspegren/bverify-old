package org.bverify.proofs;

import java.util.ArrayList;
import java.util.List;

import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.CategoricalAttributes;
import org.bverify.records.NumericalAttributes;
import org.bverify.records.Record;
import org.bverify.records.SimpleRecord;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

public class CategoricalQueryProofTest {

	public static final List<SimpleRecord> simplerecords = new ArrayList<SimpleRecord>();


	
	@BeforeClass
	public static void setup() {
		// entry 0 has attribute 0 true
		// entry 1 has attribute 0 and 1 true
		// ....
		// entry 9 has attributes 0 - 9 (all atributes) true
		for (int i = 0; i < 10; i++) {
			CategoricalAttributes cat = new CategoricalAttributes(10);
			NumericalAttributes num = new NumericalAttributes(1);
			for (int j = 0; j <= i; j++) {
				cat.setAttribute(j, true);
			}
			simplerecords.add(new SimpleRecord(cat, num));
		}
	}
	
	@Test
	public void testCategoricalProofCorrect() {
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
		HistoryTree<RecordAggregation, Record> tree = new HistoryTree<RecordAggregation, Record>(cgr, store);
		for(SimpleRecord sr : simplerecords) {
			tree.append(sr);
		}
		CategoricalAttributes filter = new CategoricalAttributes(10);
		filter.setAttribute(0, true);
		filter.setAttribute(6, true);
		try {
			CategoricalQueryProof catqproof = new CategoricalQueryProof(filter, tree,
					1, 9);
			
			Assert.assertEquals(1, catqproof.getCommitmentNumber());
			
			List<Integer> correctRecordNumbers = new ArrayList<Integer>();
			correctRecordNumbers.add(6);
			correctRecordNumbers.add(7);
			correctRecordNumbers.add(8);
			correctRecordNumbers.add(9);
	
			Assert.assertEquals(correctRecordNumbers, catqproof.getRecordNumbers());	
			
			Assert.assertEquals(simplerecords.subList(6, 10),
					catqproof.getRecords());
			
			// proof should evaluate to true
			Assert.assertTrue(catqproof.checkProof(tree.aggV(9).getHash()));
			
		} catch (ProofError e) {
			Assert.fail("Proof error");
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCategoricalProofIncorrectRecordOmitted() {
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
		HistoryTree<RecordAggregation, Record> tree = new HistoryTree<RecordAggregation, Record>(cgr, store);
		for(SimpleRecord sr : simplerecords) {
			tree.append(sr);
		}
		CategoricalAttributes filter = new CategoricalAttributes(10);
		filter.setAttribute(0, true);
		filter.setAttribute(3, true);
		try {
			CategoricalQueryProof catqproof = new CategoricalQueryProof(filter, tree,
					1, 9);
			CategoricalAttributes fakeFilter = new CategoricalAttributes(filter);
			// here all the subtrees are actually included, 
			// but the record 4 is not included
			fakeFilter.setAttribute(4, true);
			catqproof.swapOutCategoricalAttributes(fakeFilter);
			// proof should evaluate to false
			Assert.assertFalse(catqproof.checkProof(tree.aggV(9).getHash()));
			
		} catch (ProofError e) {
			Assert.fail("Proof error");
			e.printStackTrace();
		}
	}
	
	
	@Test
	public void testCategoricalProofIncorrectStubOmitted() {
		CryptographicRecordAggregator cgr = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation, Record>();
		HistoryTree<RecordAggregation, Record> tree = new HistoryTree<RecordAggregation, Record>(cgr, store);
		for(SimpleRecord sr : simplerecords) {
			tree.append(sr);
		}
		CategoricalAttributes filter = new CategoricalAttributes(10);
		filter.setAttribute(0, true);
		filter.setAttribute(3, true);
		try {
			CategoricalQueryProof catqproof = new CategoricalQueryProof(filter, tree,
					1, 9);
			CategoricalAttributes fakeFilter = new CategoricalAttributes(filter);
			// and entire subtree is omitted!
			fakeFilter.setAttribute(9, true);
			catqproof.swapOutCategoricalAttributes(fakeFilter);
			// proof should evaluate to false
			Assert.assertFalse(catqproof.checkProof(tree.aggV(9).getHash()));
			
		} catch (ProofError e) {
			Assert.fail("Proof error");
			e.printStackTrace();
		}
	}
	
	
	
	
	
}
