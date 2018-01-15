package org.bverify.bverify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;
import org.catena.client.CatenaClient;
import org.catena.common.CatenaStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;

public class BVerifyClient {
	
	
	private CatenaClient  bitcoinTxReader; 
	private BVerifyServer bverifyserver;
	
	private int total_records;
	private int verified_records;
	
	private ArrayList<byte[]> commitmentHashes;
	private ArrayList<Boolean> verifiedCommitmentHashes;
	private int currentCommitmentNumber;
	
	private static final Logger log = LoggerFactory.getLogger(BVerifyClient.class);
	
	public BVerifyClient(CatenaClient client, BVerifyServer bverifyserver) {
		this.bitcoinTxReader = client;
		this.bverifyserver = bverifyserver;
		this.total_records = 0;
		this.verified_records = 0;
		this.commitmentHashes = new ArrayList<byte[]>();
		this.verifiedCommitmentHashes = new ArrayList<Boolean>();
		// must have at least one commitment 
		this.currentCommitmentNumber = 1;
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
		int endIndex = this.commitmentHashes.size();
		int startIndex;
		// if only one commitment then nothing to compare it to 
		// (for now, may implement some sort of version / statement as first commit)
		if(this.currentCommitmentNumber == 1) {
			startIndex = 1;
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

}
