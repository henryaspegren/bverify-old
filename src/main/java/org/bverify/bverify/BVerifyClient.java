package org.bverify.bverify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;
import org.catena.client.CatenaClient;
import org.catena.common.CatenaStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.historytree.AggWithChildren;
import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;

public class BVerifyClient {
	
	
	private CatenaClient  bitcoinTxReader; 
	private BVerifyServer bverifyserver;
	
	private int total_records;
	
	private ArrayList<byte[]> commitmentHashes;
	private ArrayList<Boolean> verifiedCommitmentHashes;
	private int currentCommitmentNumber;
	
	private CryptographicRecordAggregator aggregator;
	
	private static final Logger log = LoggerFactory.getLogger(BVerifyClient.class);
	
	public BVerifyClient(CatenaClient client, BVerifyServer bverifyserver) {
		this.bitcoinTxReader = client;
		this.bverifyserver = bverifyserver;
		this.total_records = 0;
		this.commitmentHashes = new ArrayList<byte[]>();
		this.verifiedCommitmentHashes = new ArrayList<Boolean>();
		// must have at least one commitment 
		this.currentCommitmentNumber = 1;
		this.aggregator = new CryptographicRecordAggregator();
	}
	
	public void loadStatements() {
		Iterator<CatenaStatement> reader = 
				this.bitcoinTxReader.getCatenaWallet().statementIterator(true);
		
		int commitmentNumber = 1;
		while(reader.hasNext()) {
			CatenaStatement st = reader.next();
			if(commitmentNumber > this.commitmentHashes.size()) {
				this.commitmentHashes.add(st.getData());
				this.verifiedCommitmentHashes.add(false);
				String statementString = new String(st.getData());
				log.info("BVERIFY COMMITMENT HASH #{} : {}", commitmentNumber, statementString);
			}
			commitmentNumber++;
		}
		log.info("TOTAL OF {} BVERIFY COMMITMENTS FOUND", this.commitmentHashes.size());;

	}
	
	public void verifyConsistency() throws ProofError {
		assert this.commitmentHashes.size() == this.verifiedCommitmentHashes.size();
		// if there are no outstanding unverified commitments 
		// then we don't have anything to check
		if(this.commitmentHashes.size() == this.currentCommitmentNumber) {
			return;
		}
		
		int endIndex = this.commitmentHashes.size();
		int startIndex;
		if(this.currentCommitmentNumber == 1) {
			startIndex = this.currentCommitmentNumber;
		}
		else {
			startIndex = this.currentCommitmentNumber - 1;
		}

		
		HistoryTree<RecordAggregation, Record> proof = this.bverifyserver.constructConsistencyProof(
				startIndex, endIndex);	

		for(int commitmentNumber = startIndex; commitmentNumber <= endIndex; 
				commitmentNumber++) {
			int commitmentNumberIdx = commitmentNumber-1;
			int versionNumber = this.bverifyserver.commitmentNumberToVersionNumber(commitmentNumber);
			this.total_records = versionNumber+1;
			RecordAggregation agg = proof.aggV(versionNumber);
			byte[] recordHash = agg.getHash();
			byte[] commitmentHash = this.commitmentHashes.get(commitmentNumberIdx);
			boolean match = Arrays.equals(recordHash, commitmentHash);
			this.verifiedCommitmentHashes.set(commitmentNumberIdx, match);
			if(!match) {
				throw new ProofError("Consistency Proof Invalid - Some Records Have Been Ommitted");
			}
		}
		this.currentCommitmentNumber = endIndex;
	}
	
	public int totalCommitments() {
		assert this.commitmentHashes.size() == this.verifiedCommitmentHashes.size();
		return this.commitmentHashes.size();
	}
	
	public int currentCommitment() {
		return this.currentCommitmentNumber;
	}
	
	public int getTotalRecord() {
		return this.total_records;
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
		HistoryTree<RecordAggregation, Record> proofTree = 
				this.bverifyserver.constructRecordProof(recordNumber);
		Record record = proofTree.leaf(recordNumber).getVal();
		int lastCommittedVersion = this.bverifyserver.commitmentNumberToVersionNumber(
				this.currentCommitmentNumber);
		RecordAggregation agg = proofTree.aggV(lastCommittedVersion);
		byte[] treeHash = agg.getHash();
		byte[] latestCommitmentHash = this.commitmentHashes.get(this.currentCommitmentNumber-1);
		boolean matches = Arrays.equals(treeHash, latestCommitmentHash);
		if(!matches) {
			throw new ProofError("This Record is Inconsistent And Has Been Modified or Reordered");
		}
		return record;
		
	}
	
	public RecordAggregation getAndCheckAggregation(int commitmentNumber) throws ProofError {
		AggWithChildren<RecordAggregation> aggProof = 
				this.bverifyserver.constructAggregationProof(commitmentNumber);
		boolean proofCorrect = this.checkRecordAggregationProof(aggProof, commitmentNumber);
		if(!proofCorrect) {
			throw new ProofError("This Record Aggregation Is Invalid");
		}
		return aggProof.getMain();

	}
	
	
	public boolean checkRecordAggregationProof(AggWithChildren<RecordAggregation> aggProof, int commitmentNumber) {
		RecordAggregation unverifiedRecordAgg = aggProof.getMain();
		RecordAggregation left = aggProof.getLeft();
		RecordAggregation right = aggProof.getRight();
				
		// STEP 1 - verify record hash is correctly calculated 
		
		// this recalculates the hash value on the client 
		RecordAggregation correctRecordAgg = this.aggregator.aggChildren(left, right);
		// .... and if the record is authentic then it will match what the
		// 		server gave to use (the equality checks that the hashes match)
		boolean hashCalculatedCorrectly = correctRecordAgg.equals(unverifiedRecordAgg);
		if(!hashCalculatedCorrectly) {
			return false;
		}
		assert Arrays.equals(correctRecordAgg.getHash(), unverifiedRecordAgg.getHash());
		
		// STEP 2 - check that record hash matches the latest commitment 
		int commitNumberIndx = commitmentNumber-1;
		byte[] latestCommitmentHash = this.commitmentHashes.get(commitNumberIndx);
		byte[] recordAggHash = correctRecordAgg.getHash();
		boolean hashMatchesCommitment = Arrays.equals(latestCommitmentHash, recordAggHash);
		
		return hashMatchesCommitment;
		
	}

}





























