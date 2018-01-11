package org.bverify.accounts;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;

public class Account implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/** Details */
	private final String name;
	private final long id;
	
	/** For signing - we use ECDSA with SHA 256 */
	private KeyPair ecdsaKey; 
	
	// should not be serailized
	private transient Signature ecdsaSignature;
	
	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	
	public Account(String name, long id) {
		this.name = name;
		this.id = id;
		
		// TODO: this should later be decopuled and moved into a utility 
		// 			module that abstracts aways signing details
		// 			for now we leave it 
		try {
			KeyPairGenerator ecdsaGen = KeyPairGenerator.getInstance("ECDSA", "BC");
	        ecdsaGen.initialize(256, new SecureRandom());
	        this.ecdsaKey = ecdsaGen.genKeyPair();
	        this.ecdsaSignature = Signature.getInstance("SHA256withECDSA", "BC");
		} catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeException("No SHA256 Provider");
		}
	}
	
	public PublicKey getPubKey() {
		return this.ecdsaKey.getPublic();
	}

	public byte[] sign(byte[] message) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
		this.ensureSignatureLoaded();
		// use new randomness for each signature 
		this.ecdsaSignature.initSign(this.ecdsaKey.getPrivate(), new SecureRandom());
        this.ecdsaSignature.update(message);
        byte[] ecdsaSignatureBytes = this.ecdsaSignature.sign();
        return ecdsaSignatureBytes;
	}
	
	public boolean checkSignature(byte[] message, byte[] signature) throws SignatureException, InvalidKeyException {
		this.ensureSignatureLoaded();
		this.ecdsaSignature.initVerify(this.ecdsaKey.getPublic());
		this.ecdsaSignature.update(message);
		return this.ecdsaSignature.verify(signature);
	}

	public String getName() {
		return name;
	}

	public long getId() {
		return id;
	}
	
	/**
	 * When accounts are serialized we do not serialize the Signature 
	 * (because this must be loaded fresh from a provider each time).
	 * Thus we may need to setup a new Signature
	 */
	private void ensureSignatureLoaded() {
		if(this.ecdsaSignature == null) {
			try {
				this.ecdsaSignature = Signature.getInstance("SHA256withECDSA", "BC");
			} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
				e.printStackTrace();
				throw new RuntimeException("No SHA256 Provider");
			}
		}
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(arg0 instanceof Account){
			Account ar = (Account) arg0;
			if(
					ar.id == this.id && 
					ar.name.equals(this.name) &&
					ar.ecdsaKey.getPublic().equals(this.ecdsaKey.getPublic())
				) {
				return true;
			}
		}
		return false;
	}
		
}
