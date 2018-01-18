package org.bverify.proofs;

/**
 * A dedicated Exception to represent incorrect or  
 * invalid BVerify Proofs 
 * @author henryaspegren
 *
 */
public class InvalidProof extends Exception {

	private static final long serialVersionUID = 1L;
	
	private final String reason;
	private final Proof offendingProof;
	
	public InvalidProof(String reasonProofIsInvalid, Proof offendingProof) {
		this.reason = reasonProofIsInvalid;
		this.offendingProof = offendingProof;
	}
	
	public Proof getInvalidProof() {
		return this.offendingProof;
	}

	@Override
	public String toString() {
		String errorMessage = "InvalidProof! - Reason: "+reason;
		return errorMessage;
	}
		
}
