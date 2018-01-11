package org.bverify.aggregators;

import org.bverify.accounts.Account;
import org.bverify.records.Withdrawal;
import org.bverify.records.Transfer;
import org.bverify.records.Deposit;
import org.bverify.records.Record;
import org.junit.Assert;
import org.junit.Test;

import com.google.protobuf.InvalidProtocolBufferException;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

public class TestRecordHistoryTree {
	
	public static Account alice = new Account("Alice", 1);
	public static Account bob = new Account("Bob", 2);
	
	public static String goodCorn = "CORN";	
	
	@Test
	public void testRecordHistTreeSerialization() throws InvalidProtocolBufferException {
		
        CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation,Record>();    
		
		HistoryTree<RecordAggregation, Record> histtree = new HistoryTree<RecordAggregation, Record>(aggregator, store);
		
		Deposit dep = new Deposit(goodCorn, 10, alice, bob);
		Withdrawal wd = new Withdrawal(goodCorn, 5, alice, bob);
		Transfer tf = new Transfer(goodCorn, 5, alice, bob);
		
		histtree.append(dep);
		histtree.append(wd);
		histtree.append(tf);
		
		int version = 3;
		
		byte[] asByte = histtree.serializeTree();
		
        CryptographicRecordAggregator aggregatorFromBytes = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> storeFromBytes = new ArrayStore<RecordAggregation,Record>();    
		
		HistoryTree<RecordAggregation, Record> histtreeFromBytes = new HistoryTree<RecordAggregation, Record>(
				aggregatorFromBytes, storeFromBytes);
		
		histtreeFromBytes.updateTime(version);
		
		histtreeFromBytes.parseTree(asByte);
		
		// check that they have the same aggregation
		// TODO: implement hisstree equality helper methods
		Assert.assertEquals(histtree.agg(), histtreeFromBytes.agg());
		Assert.assertEquals(histtree.leaf(0).getVal(), histtreeFromBytes.leaf(0).getVal());
		Assert.assertEquals(histtree.leaf(1).getVal(), histtreeFromBytes.leaf(1).getVal());
		Assert.assertEquals(histtree.leaf(2).getVal(), histtreeFromBytes.leaf(2).getVal());
		
	}
	
	
	@Test
	public void testRecordHistTreePruned() throws ProofError {
        CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation,Record>();    	
		HistoryTree<RecordAggregation, Record> histtree = new HistoryTree<RecordAggregation, Record>(aggregator, store);
		
		Deposit dep1 = new Deposit(goodCorn, 10, alice, bob);
		Deposit dep2 = new Deposit(goodCorn, 10, alice, bob);

		Withdrawal wd = new Withdrawal(goodCorn, 5, alice, bob);
		Transfer tf = new Transfer(goodCorn, 5, alice, bob);
		
		histtree.append(dep1);
		histtree.append(dep2);
		histtree.append(dep1);
		histtree.append(dep2);
		histtree.append(dep1);
		histtree.append(dep2);

		histtree.append(wd);
		histtree.append(tf);
		histtree.append(wd);
		histtree.append(tf);
		histtree.append(wd);
		histtree.append(tf);
		
		ArrayStore<RecordAggregation, Record> newStore = new ArrayStore<RecordAggregation,Record>();    	
		HistoryTree<RecordAggregation, Record> prunedHisttree = histtree.makePruned(newStore);
		
		Assert.assertEquals(histtree.agg(), prunedHisttree.agg());
		
		// when creating a pruned tree defaults to have a path to the most 
		// recent leaf (i.e the leaf == version)
		Assert.assertEquals(histtree.leaf(11).getAgg(), prunedHisttree.leaf(11).getAgg());
		prunedHisttree.copyV(histtree, 0, true);
		
		Assert.assertEquals(histtree.leaf(0).getVal(), prunedHisttree.leaf(0).getVal());
		
		// should both have the aggregation for versions 0 and 1 of the tree
		Assert.assertEquals(histtree.aggV(1), prunedHisttree.aggV(1));
		Assert.assertEquals(histtree.aggV(0), prunedHisttree.aggV(0));
		
		// and have the correct values 
		Assert.assertEquals(20, prunedHisttree.aggV(1).getNetAmount());
		Assert.assertEquals(10, prunedHisttree.aggV(0).getNetAmount());
	}
}
