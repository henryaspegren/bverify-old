package org.bverify.records;

import java.util.Date;

import org.bverify.accounts.Account;

/**
 * Used for testing to modify records
 * @author henryaspegren
 *
 */
public class RecordUtils {

	public static Transfer modifyTransferAmount(Transfer transferobj, int newAmount) {
		String typeOfGood = transferobj.getTypeOfGood();
		Account sender = transferobj.getSender();
		Account recepient = transferobj.getRecepient();
		Date date = transferobj.dateCreated();
		Transfer newTransfer = new Transfer(typeOfGood, newAmount, recepient, sender);
		newTransfer.setDateCreated(date);
		newTransfer.signRecipient();
		newTransfer.signSender();
		assert !newTransfer.equals(transferobj);
		return newTransfer;

	}
	
	public static Deposit modifyDepositAmount(Deposit depobj, int newAmount) {
		String typeOfGood = depobj.getTypeOfGood();
		Account employee = depobj.getEmployeeAccount();
		Account recepient = depobj.getRecepientAccount();
		Date date = depobj.dateCreated;
		Deposit newdep = new Deposit(typeOfGood, newAmount, recepient, employee);
		newdep.setDateCreated(date);
		newdep.signEmployee();
		newdep.signRecipient();
		assert !newdep.equals(depobj);
		return newdep;
	}
	
	public static Withdrawal modifyWithdrawalAmount(Withdrawal widobj, int newAmount) {
		String typeOfGood = widobj.getTypeOfGood();
		Account employee = widobj.getEmployeeAccount();
		Account recepient = widobj.getRecepientAccount();
		Date date = widobj.dateCreated;
		Withdrawal newwdl = new Withdrawal(typeOfGood, newAmount, recepient, employee);
		newwdl.setDateCreated(date);
		newwdl.signEmployee();
		newwdl.signRecipient();
		assert !newwdl.equals(widobj);
		return newwdl;
	}
	
	
	
}
