package org.bverify.bverify;

import org.bverify.accounts.Account;
import org.bverify.records.Deposit;
import org.bverify.records.Transfer;
import org.bverify.records.Withdrawal;

import edu.rice.historytree.AggregationInterface;
import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.aggs.SHA256AggB64;
import edu.rice.historytree.storage.ArrayStore;

/**
 * Main entry point for <b>bverify</b>
 * To be redone.
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {

		AggregationInterface<byte[],byte[]> aggobj = new SHA256AggB64();
		ArrayStore<byte[], byte[]> store = new ArrayStore<byte[],byte[]>();
		
		HistoryTree<byte[],byte[]> tree = new HistoryTree<byte[], byte[]>(aggobj,store);
		tree.append("somedata".getBytes());
		tree.append("moredata".getBytes());
		tree.append("evenmoredata".getBytes());
		tree.append("xxxxxxxxx".getBytes());
        //System.out.println(tree.toString());
        //System.out.println(tree.agg());
   
        Account alice = new Account("Alice", 1);
        Account bob = new Account("Bob", 2);
        
        Deposit exampleDeposit = new Deposit("CORN", 100, alice, bob);
        exampleDeposit.signRecipient();
        exampleDeposit.signEmployee();
        System.out.println(exampleDeposit);
        System.out.println(exampleDeposit.isValid());    
        
        Withdrawal exampleWithdrawal = new Withdrawal("CORN", -50, alice, bob);
        exampleWithdrawal.signRecipient();
        exampleWithdrawal.signEmployee();
        System.out.println(exampleWithdrawal);
        System.out.println(exampleWithdrawal.isValid());  
        
        Transfer exampleTransfer = new Transfer("CORN", 50, alice, bob);
        exampleTransfer.signRecipient();
        exampleTransfer.signSender();
        System.out.println(exampleTransfer);
        System.out.println(exampleTransfer.isValid());    
        
    }
    
    
}
