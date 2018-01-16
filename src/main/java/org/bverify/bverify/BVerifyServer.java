package org.bverify.bverify;

import java.nio.ByteBuffer;
import java.util.HashMap;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.records.Record;
import org.catena.server.CatenaServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

public class BVerifyServer {

	private transient CatenaServer bitcoinTxPublisher;
	private CryptographicRecordAggregator aggregator;
	private ArrayStore<RecordAggregation, Record> store;
	private HistoryTree<RecordAggregation, Record> histtree;
	
	private int total_records;
	private int committed_records;
	private int numberOfCommitments;
	
	// TODO:  optimize this using an indexing scheme
	// and reduce redundancy 
	private HashMap<ByteBuffer, Integer> commitmentHashToVersion;
	private HashMap<ByteBuffer, Integer> commitmentHashToCommitmentNumber;
	private HashMap<Integer, Integer> commitmentNumberToVersionNumber;
	
    private static final Logger log = LoggerFactory.getLogger(BVerifyServer.class);
	
	/**
	 * Commit a set of records to the blockchain by
	 * publishing a commitment hash when a total of 
	 * COMMIT_INTERVAL records are outstanding. 
	 * This is a parameter that we can optimize
	 */
	private static final int COMMIT_INTERVAL = 3;
	
	
	public BVerifyServer(CatenaServer srvr) {
		this.bitcoinTxPublisher = srvr;
        this.aggregator = new CryptographicRecordAggregator();
		this.store = new ArrayStore<RecordAggregation,Record>();    
		this.histtree = new HistoryTree<RecordAggregation, Record>(aggregator, store);
		this.total_records = 0;
		this.committed_records = 0;
		this.numberOfCommitments = 0;
		this.commitmentHashToVersion = new HashMap<ByteBuffer, Integer>();
		this.commitmentHashToCommitmentNumber = new HashMap<ByteBuffer, Integer>();
		this.commitmentNumberToVersionNumber = new HashMap<Integer, Integer>();
	}
	
	
	public void addRecord(Record r) throws InsufficientMoneyException {
		// TODO: should use multi-reader, single writer log 
		synchronized(this) {
			this.histtree.append(r);
			this.total_records++;
			int outstanding_records = total_records - committed_records;
			
			assert outstanding_records <= BVerifyServer.COMMIT_INTERVAL;
					
			if(outstanding_records == BVerifyServer.COMMIT_INTERVAL) {
				RecordAggregation currentAgg = this.histtree.agg();
				byte[] hashAgg = currentAgg.getHash();
				Transaction tx = this.bitcoinTxPublisher.appendStatement(hashAgg);
				BVerifyServer.log.info("Committing BVerify log with {} records to blockchain in txn {}",
						this.total_records, tx.getHashAsString());
				
				int currentVersion = this.histtree.version();
				this.numberOfCommitments++;
				this.commitmentHashToVersion.put(ByteBuffer.wrap(hashAgg), currentVersion);
				this.commitmentHashToCommitmentNumber.put(ByteBuffer.wrap(hashAgg),this.numberOfCommitments);
				this.commitmentNumberToVersionNumber.put(this.numberOfCommitments, currentVersion);
				this.committed_records = this.total_records;
			}		
		}
	}
	
	
	public HistoryTree<RecordAggregation, Record> constructConsistencyProof(byte[] oldHash,
			byte[] newHash) throws ProofError{
		return this.constructConsistencyProof(commitmentHashToCommitmentNumber(oldHash), 
				commitmentHashToCommitmentNumber(newHash));
	}
	
	public HistoryTree<RecordAggregation, Record> constructConsistencyProof(int startingCommitNumber, int endingCommitNumber) 
			throws ProofError{
		ArrayStore<RecordAggregation, Record> newdatastore = new ArrayStore<RecordAggregation, Record>();
		
		// TODO: method in this library needs to be rewritten
		HistoryTree<RecordAggregation, Record> proofTree = this.histtree.makePruned(newdatastore);
		
		// This will allow us to compute the 
		// required aggregations and prove consistency
		for(int commitNumber = startingCommitNumber; commitNumber <= endingCommitNumber; commitNumber++) {
			int versionNumber = this.commitmentNumberToVersionNumber(commitNumber);
			proofTree.copyV(histtree, versionNumber, false);
		}
		
		return proofTree;
	}
	
	
	public int commitmentHashToVersion(byte[] commitHash) {
		return this.commitmentHashToVersion.get(ByteBuffer.wrap(commitHash));
	}
	
	private int commitmentHashToCommitmentNumber(byte[] commitHash) {
		return this.commitmentHashToCommitmentNumber.get(ByteBuffer.wrap(commitHash));
	}
	
	public int commitmentNumberToVersionNumber(int commitNumber) {
		return this.commitmentNumberToVersionNumber.get(commitNumber);
	}
		
	public int getTotalNumberOfCommitments() {
		return this.numberOfCommitments;
	}
	
	public int getTotalNumberOfRecords() {
		return this.total_records;
	}
	
	public int getTotalNumberOfCommittedRecords() {
		return this.committed_records;
	}
	
	public void printTree() {
		System.out.println(this.histtree.toString());
	}
	
	/**
	 * This method modifies an existing record in log and recalculates 
	 * the aggregations, but does not change any previous commitments.
	 * NOTE THAT THIS METHOD IS FOR TESTING PURPOSES ONLY -- 
	 * modification of a record in the log 
	 * will result in the inconsistency being detected and rejected by 
	 * BVerifyClients 
	 * 
	 * @param recordNumber - the record to be replaced, indexed from 1
	 * @param newRecord - the new record to be put in its place
	 */
	public void changeRecord(int recordNumber, Record newRecord) {
        CryptographicRecordAggregator newAggregator = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> newStore = new ArrayStore<RecordAggregation,Record>();    
		HistoryTree<RecordAggregation, Record> newHisttree = new HistoryTree<RecordAggregation, Record>(newAggregator, newStore);
		// this algorithm recomputes the entire tree, rather than just the necessary hashes, 
		// but since for testing use only this is not a big problem
		for(int i = 0; i <= this.histtree.version(); i++) {
			Record r;
			if( i == (recordNumber-1)) {
				r = newRecord;
			}
			else {
				r = this.histtree.leaf(i).getVal();	
			}	
			newHisttree.append(r);
		}
		this.aggregator = newAggregator;
		this.store = newStore;
		this.histtree = newHisttree;

	}
	
	
	
}
