package org.bverify.bverify;

import java.util.ArrayList;
import java.util.Iterator;

import org.bverify.aggregators.RecordAggregation;
import org.bverify.proofs.AggregationProof;
import org.bverify.proofs.ConsistencyProof;
import org.bverify.proofs.RecordProof;
import org.bverify.records.Record;
import org.catena.client.CatenaClient;
import org.catena.common.CatenaStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.historytree.ProofError;

/**
 * For now this class consists of various 
 * functions that the client will want to perform 
 * that will need to be split off into RPC/RMI calls.
 * Currently these have only been implemented locally
 * @author henryaspegren
 *
 */
public class BVerifyClientUtils {
	
	private CatenaClient  bitcoinTxReader; 
	private BVerifyServerUtils bverifyserver;
		
	private ArrayList<byte[]> commitmentHashes;
	private ArrayList<Boolean> verifiedCommitmentHashes;
	private int currentCommitmentNumber;
		
	private static final Logger log = LoggerFactory.getLogger(BVerifyClientUtils.class);
	
	public BVerifyClientUtils(CatenaClient client, BVerifyServerUtils bverifyserver) {
		this.bitcoinTxReader = client;
		this.bverifyserver = bverifyserver;
		this.commitmentHashes = new ArrayList<byte[]>();
		this.verifiedCommitmentHashes = new ArrayList<Boolean>();
		this.currentCommitmentNumber = -1;
	}
	
	public void loadStatements() {
		Iterator<CatenaStatement> reader = 
				this.bitcoinTxReader.getCatenaWallet().statementIterator(true);
		// commitment numbers are zero indexed
		int commitmentNumber = 0;
		while(reader.hasNext()) {
			CatenaStatement st = reader.next();
			if(commitmentNumber >= this.getTotalCommitments()) {
				this.commitmentHashes.add(st.getData());
				this.verifiedCommitmentHashes.add(false);
				String statementString = new String(st.getData());
				log.info("BVERIFY COMMITMENT HASH #{} : {}", commitmentNumber, statementString);
			}
			commitmentNumber++;
		}
		log.info("TOTAL OF {} BVERIFY COMMITMENTS FOUND", this.getTotalCommitments());;

	}
	
	public void verifyConsistency() throws ProofError {
		assert this.commitmentHashes.size() == this.verifiedCommitmentHashes.size();
		// if there are no outstanding unverified commitments or there are no 
		// commitments period than we don't have anything to check
		if(this.commitmentHashes.size() == (this.currentCommitmentNumber+1) 
				|| this.commitmentHashes.size() == 0) {
			return;
		}
		int endIndex = this.commitmentHashes.size()-1;
		int startIndex;
		// first commitment
		if(this.currentCommitmentNumber == -1) {
			startIndex = 0;
		}
		else {
			// otherwise we need to start at current commitment
			startIndex = this.currentCommitmentNumber;
		}
		ConsistencyProof proof = this.bverifyserver.constructConsistencyProof(
				startIndex, endIndex);	
		boolean proofCorrect = proof.checkProof(commitmentHashes.subList(startIndex, endIndex+1));
		if(!proofCorrect) {
			throw new ProofError("Consistency Proof Invalid - Some Records Have Been Ommitted");
		}else {
			// if consistency proof has been verified
			for(int indx = startIndex; indx <= endIndex; indx++) {
				// mark all the commitments as verified
				this.verifiedCommitmentHashes.set(indx, true);
			}
			// update current commitment
			this.currentCommitmentNumber = endIndex;
		}
	}
	
	public int getTotalCommitments() {
		assert this.commitmentHashes.size() == this.verifiedCommitmentHashes.size();
		return this.commitmentHashes.size();
	}
	
	public int currentCommitment() {
		return this.currentCommitmentNumber;
	}
	
	public byte[] getCommitment(int commitmentNumber) {
		return this.commitmentHashes.get(commitmentNumber);
	}
	
	public byte[] getCurrentCommitment() {
		return this.getCommitment(this.currentCommitmentNumber);
	}
	
	
	/**
	 * Requests for a specific record from the server along with a proof 
	 * that this record is authentic. 
	 * @param recordNumber - The record number (= version) in [0, 1, ... , (1 << this.current_commitment) -1]
	 * @return Record - The required record
	 * @throws ProofError - Throws a ProofError if the Record provided by the server is not authentic 
	 * 						/ if the proof is incorrect.
	 */
	public Record getAndVerifyRecord(int recordNumber) throws ProofError {
		RecordProof proof = 
				this.bverifyserver.constructRecordProof(recordNumber, this.currentCommitmentNumber);
		Record record = proof.getRecord();
		
		// look up the latest commitment hash 
		byte[] latestCommitmentHash = this.getCommitment(this.currentCommitmentNumber);
		
		boolean valid = proof.checkProof(latestCommitmentHash);
		
		if(!valid) {
			throw new ProofError("This Record is Inconsistent And Has Been Modified or Reordered");
		}
		return record;
		
	}
	
	public RecordAggregation getAndCheckAggregation(int commitmentNumber) throws ProofError {
		AggregationProof aggProof = 
				this.bverifyserver.constructAggregationProof(commitmentNumber);
		boolean proofCorrect =  aggProof.checkProof(getCommitment(commitmentNumber));
		if(!proofCorrect) {
			throw new ProofError("This Record Aggregation Is Invalid");
		}
		return aggProof.getAggregation();

	}
	

}





























