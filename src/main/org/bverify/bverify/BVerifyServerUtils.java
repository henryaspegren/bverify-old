package org.bverify.bverify;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.Transaction;
import org.bverify.aggregators.CryptographicRecordAggregator;
import org.bverify.aggregators.RecordAggregation;
import org.bverify.proofs.AggregationProof;
import org.bverify.proofs.CategoricalQueryProof;
import org.bverify.proofs.ConsistencyProof;
import org.bverify.proofs.RecordProof;
import org.bverify.records.CategoricalAttributes;
import org.bverify.records.Record;
import org.catena.server.CatenaServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.rice.historytree.HistoryTree;
import edu.rice.historytree.AggWithChildren;
import edu.rice.historytree.ProofError;
import edu.rice.historytree.storage.ArrayStore;

/**
 * Implementations of the server state as well
 * as various methods. These will eventually need to  
 * be written out as RPC/RMI calls.
 * 
 * Records are indexed [0, ... , totalRecords - 1] 
 * Commitments are indexed [0, ..., totalCommitments - 1]
 * 
 * 
 * @author henryaspegren
 *
 */
public class BVerifyServerUtils {

	private transient CatenaServer bitcoinTxPublisher;
	private CryptographicRecordAggregator aggregator;
	private ArrayStore<RecordAggregation, Record> store;
	private HistoryTree<RecordAggregation, Record> histtree;
	
	/**
	 * Total records are the number of records -- committed 
	 * and uncommitted -- stored by Bverify
	 */
	private int totalRecords;
	
	/**
	 * Committed records are records for which a commitment transaction
	 * has been issued on the Blockchain
	 */
	private int totalCommittedRecords;
	private int totalCommitments;
	
	/**
	 * In some situations we may not want to 
	 * commit to the bitcoin blockchain (e.g. for testing 
	 * and benchmarking throughput).
	 */
	private boolean commitToBitcoin;
	
	// TODO:  optimize this using an indexing scheme
	// and reduce redundancy 
	private HashMap<ByteBuffer, Integer> commitmentHashToVersion;
	private HashMap<ByteBuffer, Integer> commitmentHashToCommitmentNumber;
	private HashMap<Integer, Integer> commitmentNumberToVersionNumber;
	
    private static final Logger log = LoggerFactory.getLogger(BVerifyServerUtils.class);
	
	/**
	 * Commit a set of records to the Blockchain by
	 * publishing a commitment hash when a total of 
	 * COMMIT_INTERVAL records are outstanding. 
	 * This is a parameter that we can optimize
	 */
	private static final int DEFAULT_COMMIT_INTERVAL = 3;
	private final int commitInterval;
	
	/**
	 * We can also support parallelization by using a 
	 * Read|Write lock. This allows proofs to be generated 
	 * concurrently.
	 */
	private final ReadWriteLock readWriteLock;
	
	public BVerifyServerUtils(CatenaServer srvr, boolean commitToBitcoin, int commitInterval) {
        this.aggregator = new CryptographicRecordAggregator();
		this.store = new ArrayStore<RecordAggregation,Record>();    
		this.histtree = new HistoryTree<RecordAggregation, Record>(aggregator, store);
		this.bitcoinTxPublisher = srvr;
		this.totalRecords = 0;
		this.totalCommittedRecords = 0;
		this.totalCommitments = 0;
		this.commitmentHashToVersion = new HashMap<ByteBuffer, Integer>();
		this.commitmentHashToCommitmentNumber = new HashMap<ByteBuffer, Integer>();
		this.commitmentNumberToVersionNumber = new HashMap<Integer, Integer>();
		this.commitToBitcoin = commitToBitcoin;
		this.commitInterval = commitInterval;
		this.readWriteLock = new ReentrantReadWriteLock();
	}
	
	
	public BVerifyServerUtils(CatenaServer srvr) {
		this(srvr, true, DEFAULT_COMMIT_INTERVAL);
	}
	
	public void addRecord(Record r) throws InsufficientMoneyException {
		// write lock needed!
		this.readWriteLock.writeLock().lock();
		this.histtree.append(r);
		this.totalRecords++;
		int outstanding_records = totalRecords - totalCommittedRecords;
		
		assert outstanding_records <= this.commitInterval;
				
		if(outstanding_records == this.commitInterval) {
			RecordAggregation currentAgg = this.histtree.agg();
			byte[] hashAgg = currentAgg.getHash();
			if(commitToBitcoin) {
				Transaction tx = this.bitcoinTxPublisher.appendStatement(hashAgg);
				BVerifyServerUtils.log.info("Committing BVerify log with {} records to blockchain in txn {}",
						this.totalRecords, tx.getHashAsString());
			}
			int currentVersion = this.histtree.version();
			this.totalCommitments++;
			// commitments are zero indexed
			int currentCommitmentNumber =  this.getTotalNumberOfCommitments()-1;
			this.commitmentHashToVersion.put(ByteBuffer.wrap(hashAgg), currentVersion);
			this.commitmentHashToCommitmentNumber.put(ByteBuffer.wrap(hashAgg), currentCommitmentNumber);
			this.commitmentNumberToVersionNumber.put(currentCommitmentNumber, currentVersion);
			this.totalCommittedRecords = this.totalRecords;
		}
		this.readWriteLock.writeLock().unlock();
	}
	
	public ConsistencyProof constructConsistencyProof(int startingCommitNumber, int endingCommitNumber) 
			throws ProofError{
		this.readWriteLock.readLock().lock();
		List<Integer> cmtRecordNumbers = new ArrayList<>();
		for(int cmtNumber = startingCommitNumber; cmtNumber <= endingCommitNumber; cmtNumber++) {
			int cmtRecordNumber = this.commitmentNumberToRecordNumber(cmtNumber);
			cmtRecordNumbers.add(cmtRecordNumber);
		}
		ConsistencyProof proof = new ConsistencyProof(startingCommitNumber, cmtRecordNumbers, this.histtree);
		this.readWriteLock.readLock().unlock();
		return proof;
	}
	
	public RecordProof constructRecordProof(int recordNumber, int commitmentNumber) throws ProofError {
		this.readWriteLock.readLock().lock();
		if(this.totalCommittedRecords <= recordNumber) {
			throw new ProofError(String.format("Record #{} has not been commited yet. So far only commited up to "
					+ "Record #{}", 
					recordNumber, this.getTotalNumberOfCommitments()-1));
		}
		int commitmentRecordNumber = this.commitmentNumberToRecordNumber(commitmentNumber);
		RecordProof proof = new RecordProof(recordNumber, commitmentNumber, commitmentRecordNumber, this.histtree);
		this.readWriteLock.readLock().unlock();
		return proof;
	}
	
	/**
	 * Construct a proof of the aggregation corresponding to the 
	 * input commit number 
	 * @param commitNumber - The commitment number (1 indexed) to construct an aggregation 
	 * proof for
	 * @return Returns the aggregation along with the pre-image (children) from which the 
	 * hash can be recalculated and verified 
	 */
	public AggregationProof constructAggregationProof(int commitNumber) {
		this.readWriteLock.readLock().lock();
		int versionNumber = this.commitmentNumberToRecordNumber(commitNumber);
		AggWithChildren<RecordAggregation> aggPlusChildren = this.histtree.aggVWithChildren(versionNumber);
		AggregationProof aggProof = new AggregationProof(aggPlusChildren.getMain(),
				aggPlusChildren.getLeft().getHash(), aggPlusChildren.getRight().getHash(),
				commitNumber);
		this.readWriteLock.readLock().unlock();
		return aggProof;
		
	}
	
	/**
	 * Query records using the filter and 
	 * construct a proof that the response is correct.
	 * @param filter - Categorical Attribute filter - find records that have 
	 * at least these attributes.
	 * @return
	 * @throws ProofError
	 */
	public CategoricalQueryProof queryRecordsByFilter(CategoricalAttributes filter) throws ProofError
	{
		this.readWriteLock.readLock().lock();
		CategoricalQueryProof proof = new CategoricalQueryProof(filter, this.histtree, this.getCurrentCommitmentNumber(),
					this.commitmentNumberToRecordNumber(this.getCurrentCommitmentNumber()));
		this.readWriteLock.readLock().unlock();
		return proof;
	}
	
	public int commitmentHashToVersion(byte[] commitHash) {
		return this.commitmentHashToVersion.get(ByteBuffer.wrap(commitHash));
	}
	
	public int commitmentNumberToRecordNumber(int commitNumber) {
		return this.commitmentNumberToVersionNumber.get(commitNumber);
	}
		
	public int getTotalNumberOfCommitments() {
		return this.totalCommitments;
	}
	
	public int getCurrentCommitmentNumber() {
		return this.totalCommitments - 1;
	}
	
	public int getTotalNumberOfRecords() {
		return this.totalRecords;
	}
	
	public int getTotalNumberOfCommittedRecords() {
		return this.totalCommittedRecords;
	}
	
	public byte[] getCommitment(int commitmentNumber) {
		int version = this.commitmentNumberToRecordNumber(commitmentNumber);
		RecordAggregation agg = this.histtree.aggV(version);
		return agg.getHash();
	}
	
	public byte[] getCurrentCommitment() {
		int currentCommitmentNumber = this.getCurrentCommitmentNumber();
		return this.getCommitment(currentCommitmentNumber);
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
	 * @param recordNumber - the record to be replaced in [0, 1, ..., total_records-1]
	 * @param newRecord - the new record to be put in its place
	 */
	public void changeRecord(int recordNumber, Record newRecord) {
		this.readWriteLock.writeLock().lock();
        CryptographicRecordAggregator newAggregator = new CryptographicRecordAggregator();
		ArrayStore<RecordAggregation, Record> newStore = new ArrayStore<RecordAggregation,Record>();    
		HistoryTree<RecordAggregation, Record> newHisttree = new HistoryTree<RecordAggregation, Record>(newAggregator, newStore);
		// this algorithm recomputes the entire tree, rather than just the necessary hashes, 
		// but since for testing use only this is not a big problem
		for(int i = 0; i <= this.histtree.version(); i++) {
			Record r;
			if( i == (recordNumber)) {
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
		this.readWriteLock.writeLock().unlock();

	}
		
}
