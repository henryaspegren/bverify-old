package org.bverify.accounts;

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

public class Account {
	
	/** Details */
	private final String name;
	private final long id;
	
	/** For signing - we use ECDSA with SHA 256 */
	private KeyPair ecdsaKey; 
	private Signature ecdsaSignature;
	
	static {
		Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
	}
	
	public Account(String name, long id) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException {
		this.name = name;
		this.id = id;
		
		// TODO: this should later be decopuled and moved into a utility 
		// 			module that abstracts aways signing details
		// 			for now we leave it 
		KeyPairGenerator ecdsaGen = KeyPairGenerator.getInstance("ECDSA", "BC");
        ecdsaGen.initialize(256, new SecureRandom());
        this.ecdsaKey = ecdsaGen.genKeyPair();
        this.ecdsaSignature = Signature.getInstance("SHA256withECDSA", "BC");
	}
	
	public PublicKey getPubKey() {
		return this.ecdsaKey.getPublic();
	}

	public byte[] sign(byte[] message) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchProviderException, SignatureException {
        // use new randomness for each signature 
		this.ecdsaSignature.initSign(this.ecdsaKey.getPrivate(), new SecureRandom());
        this.ecdsaSignature.update(message);
        byte[] ecdsaSignatureBytes = this.ecdsaSignature.sign();
        return ecdsaSignatureBytes;
	}
	
	public boolean checkSignature(byte[] message, byte[] signature) throws SignatureException, InvalidKeyException {
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
		
}
