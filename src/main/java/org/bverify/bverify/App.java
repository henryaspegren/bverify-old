package org.bverify.bverify;

import org.bverify.accounts.Account;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Deposit;
import org.bverify.records.Record;
import org.bverify.records.Transfer;
import org.bverify.records.Withdrawal;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.storage.ArrayStore;

/**
 * Main entry point for <b>bverify</b>
 * To be redone.
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {

    		/*
		AggregationInterface<byte[],byte[]> aggobj = new SHA256AggB64();
		ArrayStore<byte[], byte[]> store = new ArrayStore<byte[],byte[]>();
		
		HistoryTree<byte[],byte[]> tree = new HistoryTree<byte[], byte[]>(aggobj,store);
		tree.append("somedata".getBytes());
		tree.append("moredata".getBytes());
		tree.append("evenmoredata".getBytes());
		tree.append("xxxxxxxxx".getBytes());
        //System.out.println(tree.toString());
        //System.out.println(tree.agg());
         */
   
        Account alice = new Account("Alice", 1);
        Account bob = new Account("Bob", 2);
        
        Deposit exampleDeposit = new Deposit("CORN", 100, alice, bob);
        exampleDeposit.signRecipient();
        exampleDeposit.signEmployee();
        
        Withdrawal exampleWithdrawal = new Withdrawal("CORN", -50, alice, bob);
        exampleWithdrawal.signRecipient();
        exampleWithdrawal.signEmployee();
        
        Transfer exampleTransfer = new Transfer("CORN", 50, alice, bob);
        exampleTransfer.signRecipient();
        exampleTransfer.signSender();
        
        // make a hist tree of these - with special aggregation method
        CryptographicRecordAggregator aggregator = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> store = new ArrayStore<RecordAggregation,Record>();    
		
		HistoryTree<RecordAggregation, Record> histtree = new HistoryTree<RecordAggregation, Record>(aggregator, store);
		
		histtree.append(exampleDeposit);
		histtree.append(exampleTransfer);
		System.out.println(histtree);
		System.out.println();
		histtree.append(exampleWithdrawal);
		System.out.println(histtree);
		System.out.println(histtree.agg());
		System.out.println();    
		byte[] test = histtree.serializeTree();
		System.out.println(test.toString());
		
		
		
    }
    
    
    
    
}
