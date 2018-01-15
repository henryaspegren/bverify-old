package org.bverify.bverify;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import org.bverify.records.Deposit;
import org.bverify.records.Transfer;
import org.bverify.records.Withdrawal;
import org.catena.common.ClientServerTest;
import org.junit.Assert;
import org.junit.BeforeClass;

public class BVerifyClientServerTest extends ClientServerTest {
	
	public static Deposit deposit;
	public static Withdrawal withdrawal;
	public static Transfer transfer;
	
	
	@BeforeClass
	public static void setup() {
		try {
	        ObjectInputStream oosDeposit = new ObjectInputStream(new FileInputStream("/home/henryaspegren/Documents/DCI_CODE/bverify/saved_objects/deposit"));
			deposit = (Deposit) oosDeposit.readObject();
	        oosDeposit.close();
	        
	        ObjectInputStream oosWithdrawal = new ObjectInputStream(new FileInputStream("/home/henryaspegren/Documents/DCI_CODE/bverify/saved_objects/withdrawal"));
	        withdrawal = (Withdrawal) oosWithdrawal.readObject();
	        oosWithdrawal.close();
	        
	        ObjectInputStream oosTransfer = new ObjectInputStream(new FileInputStream("/home/henryaspegren/Documents/DCI_CODE/bverify/saved_objects/transfer"));
	        transfer = (Transfer) oosTransfer.readObject();
	        oosTransfer.close();

        
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			Assert.fail();
		}
		
		
	}
	

}
