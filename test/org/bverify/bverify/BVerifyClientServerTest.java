package org.bverify.bverify;

import org.bverify.accounts.Account;
import org.bverify.records.Deposit;
import org.bverify.records.Transfer;
import org.bverify.records.Withdrawal;
import org.catena.common.ClientServerTest;
import org.junit.BeforeClass;

public class BVerifyClientServerTest extends ClientServerTest {
	
	private static Account alice = new Account("Alice", 1);
	private static Account bob = new Account("Bob", 2);
	private static Account charlie = new Account("Charlie", 3);
	
	public static Deposit deposit;
	public static Withdrawal withdrawal;
	public static Transfer transfer;
	
	private static String goodCorn = "CORN";
	
	
	@BeforeClass
	public static void setup() {
			deposit = new Deposit(goodCorn, 100, alice, bob);	        
			withdrawal = new Withdrawal(goodCorn, 100, alice, bob);      
			transfer = new Transfer(goodCorn, 50, alice, charlie);		
	}
	

}
