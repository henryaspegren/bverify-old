package org.bverify.bverify;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
 */
public class App 
{
	
	
    public static void main( String[] args ) throws Exception
    {
        
 
        ObjectInputStream oosDeposit = new ObjectInputStream(new FileInputStream("/home/henryaspegren/Documents/DCI_CODE/bverify/saved_objects/deposit"));
        Deposit exampleDeposit = (Deposit) oosDeposit.readObject();
        oosDeposit.close();
        
        ObjectInputStream oosWithdrawal = new ObjectInputStream(new FileInputStream("/home/henryaspegren/Documents/DCI_CODE/bverify/saved_objects/withdrawal"));
        Withdrawal exampleWithdrawal = (Withdrawal) oosWithdrawal.readObject();
        oosWithdrawal.close();
        
        ObjectInputStream oosTransfer = new ObjectInputStream(new FileInputStream("/home/henryaspegren/Documents/DCI_CODE/bverify/saved_objects/transfer"));
        Transfer exampleTransfer = (Transfer) oosTransfer.readObject();
        oosTransfer.close();
              
        
        /*
		
		NetworkParameters regtest  = RegTestParams.get();
		
		String hextxid = "92f367157dcbaa9d3a3abeba9fefb26170b88c5e26f70c5e8759a1c4408324f0";
		String hexChainAddr = "mxDEti9pgXA4xp5YwdFKG8BWsm3keDjqrW";
		String secretKey = "cW49kgcB5ri1VTYBKVZyMh3HPMnSFqNGMMNEqgisUT74LXCd5UAW";
		String directoryNameClient = "/home/henryaspegren/Documents/DCI_CODE/bverify/client";
		String directoryNameServer = "/home/henryaspegren/Documents/DCI_CODE/bverify/server";
		
		
		Sha256Hash txid = Sha256Hash.wrap(hextxid);
		Address chainAddr = Address.fromBase58(regtest, hexChainAddr);
        ECKey chainKey = DumpedPrivateKey.fromBase58(regtest, secretKey).getKey(); 

		File directoryClient = new File(directoryNameClient);
		File directoryServer = new File(directoryNameServer);
		
		*/
	
		/*
		CatenaServer catenaServer = new CatenaServer(regtest, directoryServer,
				chainKey, txid);
		catenaServer.connectToLocalHost();
		catenaServer.startAsync();
		catenaServer.awaitRunning();
		
		BVerifyServer bverify = new BVerifyServer(catenaServer);
		bverify.addRecord(exampleDeposit); 			// 0 
		bverify.addRecord(exampleDeposit);			// 1
		bverify.addRecord(exampleTransfer);			// 2
		// 											-----
		bverify.addRecord(exampleDeposit);			// 3
		bverify.addRecord(exampleDeposit);			// 4
		bverify.addRecord(exampleWithdrawal); 		// 5
		// 											-----
		bverify.addRecord(exampleWithdrawal);		// 6
		bverify.addRecord(exampleDeposit); 			// 7
		bverify.addRecord(exampleTransfer); 		// 8
		//											-----
		bverify.addRecord(exampleTransfer); 		// 9
		bverify.addRecord(exampleTransfer); 		// 10
		*/

		
		
        /*
         
		CatenaClient catenaClient = new CatenaClient(regtest, 
				directoryClient, txid, chainAddr, null);
		
		catenaClient.connectToLocalHost();
		catenaClient.startAsync();
		catenaClient.awaitRunning();
				
		BVerifyClient bverify = new BVerifyClient(catenaClient);
		
		bverify.loadStatements();
	
	
		
		*/
	
		
    }
    
    
    
    
}
