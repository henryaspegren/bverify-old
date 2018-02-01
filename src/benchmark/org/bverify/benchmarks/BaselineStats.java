package org.bverify.benchmarks;

public class BaselineStats {
	protected final int numberOfRecordsAll;
	protected final int numberOfRecordsMatching;
	protected final int sizeOfRecordsAll;
	protected final int sizeOfRecordsMatching;
	
	public BaselineStats(int numberOfRecordsAll, int numberOfRecordsMatching, 
			int sizeOfRecordsAll, int sizeOfRecordsMatching) {
		this.numberOfRecordsAll = numberOfRecordsAll;
		this.numberOfRecordsMatching = numberOfRecordsMatching;
		this.sizeOfRecordsAll = sizeOfRecordsAll;
		this.sizeOfRecordsMatching = sizeOfRecordsMatching;
	}
	
	@Override
	public String toString() {
		return "<--Baseline Stats--->"+"\nNum Records: "+this.numberOfRecordsAll+" (Size: "+this.sizeOfRecordsAll+")\n"+
				"Num Matching Records: "+this.numberOfRecordsMatching+ "(Size: "+this.sizeOfRecordsMatching+")";
	}
};
