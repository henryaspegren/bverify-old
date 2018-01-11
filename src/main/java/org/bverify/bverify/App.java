package org.bverify.bverify;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Arrays;
import java.util.Iterator;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.RegTestParams;
import org.bverify.accounts.Account;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Deposit;
import org.bverify.records.Record;
import org.bverify.records.Transfer;
import org.bverify.records.Withdrawal;
import org.catena.client.CatenaClient;
import org.catena.client.ClientApp;
import org.catena.client.ClientWallet;
import org.catena.common.CatenaApp;
import org.catena.common.CatenaStatement;
import org.catena.common.CatenaUtils;
import org.catena.server.CatenaServer;

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
   
        
        ObjectInputStream oosAlice = new ObjectInputStream(new FileInputStream("/home/henryaspegren/Documents/DCI_CODE/bverify/saved_objects/alice"));
        Account alice = (Account) oosAlice.readObject();
        oosAlice.close();
        
        ObjectInputStream oosBob = new ObjectInputStream(new FileInputStream("/home/henryaspegren/Documents/DCI_CODE/bverify/saved_objects/bob"));
        Account bob  = (Account) oosBob.readObject();
        oosBob.close();
        	
        
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
		histtree.append(exampleDeposit);
		histtree.append(exampleDeposit);
		histtree.append(exampleDeposit);
		histtree.append(exampleDeposit);
		histtree.append(exampleTransfer);
		histtree.append(exampleWithdrawal);
	
		
		RecordAggregation agg = histtree.agg();
		byte[] hashForCatena = agg.getHash();
		
		histtree.serializeTree();
		
		
		
		/**
		NetworkParameters regtest  = RegTestParams.get();
		
		String hextxid = "8fe004f11f736a0156a457cc18e90e632b267ccd2d224f4c02a9621a0ae7a254";
		String hexChainAddr = "mpnDfLUFKStTmsiqPricEB9xKL7eH7fKrZ";
		String secretKey = "cPM8zZD2g6eVCyHjYj1H6xj45WQoooAPPZjHPtry1WDdjKoJXL1s";
		String directoryNameClient = "/home/henryaspegren/Documents/DCI_CODE/bverify/client";
		String directoryNameServer = "/home/henryaspegren/Documents/DCI_CODE/bverify/server";
		
		
		Sha256Hash txid = Sha256Hash.wrap(hextxid);
		Address chainAddr = Address.fromBase58(regtest, hexChainAddr);
        ECKey chainKey = DumpedPrivateKey.fromBase58(regtest, secretKey).getKey(); 

		File directoryClient = new File(directoryNameClient);
		File directoryServer = new File(directoryNameServer);
				
	
		CatenaServer catenaServer = new CatenaServer(regtest,
				directoryServer, chainKey, txid);
		
		catenaServer.connectToLocalHost();
		catenaServer.startAsync();
		catenaServer.awaitRunning();

		Transaction txn = catenaServer.appendStatement(hashForCatena);
        CatenaUtils.generateBlockRegtest();

        Transaction prevTx = CatenaUtils.getPrevCatenaTx(catenaServer.getCatenaWallet(), txn.getHash());

        System.out.printf("Created tx '%s' with statement '%s' (prev tx is '%s')\n", txn.getHash(),
        		hashForCatena.toString(), prevTx.getHash());
	

		
		
		CatenaClient catenaClient = new CatenaClient(regtest, 
				directoryClient, txid, chainAddr, null);
		
		catenaClient.connectToLocalHost();
		catenaClient.startAsync();
		catenaClient.awaitRunning();
		
		ClientWallet wallet = catenaClient.getCatenaWallet();
		
		Iterator<CatenaStatement> it = wallet.statementIterator(true);

		int c = 1; 
		while(it.hasNext()) {
			CatenaStatement s = it.next();
            Transaction prevTx = CatenaUtils.getPrevCatenaTx(wallet, s.getTxHash());
            System.out.printf("Statement #%d: %s (tx %s, prev %s)\n", c, s.getAsString(), s.getTxHash(), prevTx.getHash());
            if(c == 4) {
            	boolean matches = Arrays.equals(hashForCatena, s.getData());
            	System.out.println(new String(s.getData()));
            	System.out.println(new String(hashForCatena));
            	System.out.printf("Matches previous has %b", matches);
            }
            
            c = c +1;
        
		}
		*/
	
		
    }
    
    
    
    
}
