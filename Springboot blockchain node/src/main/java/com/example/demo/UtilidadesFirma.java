package com.example.demo;


import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;


public abstract class UtilidadesFirma {

	/**
	 * Factoria de claves que se inicializa con el algoritmo para generar los pares
	 * de claves publica-privada
	 */
	private static KeyFactory keyFactory = null;

	static {
		try {
			keyFactory = KeyFactory.getInstance("DSA", "SUN");

		} catch (NoSuchAlgorithmException | NoSuchProviderException e) {
			e.printStackTrace();
		}
	}


	/**
	 * Generar un par de claves publica-privada
	 * 
	 * @return KeyPair par de claves
	 * @throws InvalidAlgorithmParameterException 
	 */
	public static KeyPair generarParClaves() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
	
		KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DSA", "SUN");
		SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
		keyGen.initialize(1024, random);
	     
		return keyGen.generateKeyPair();
	}


	/**
	 * Validar una firma para unos datos y clave publica dados
	 * 
	 * @param info         datos firmados y a ser verificados
	 * @param firma        a ser verificada
	 * @param clavePublica clave publica asociada a la clave privada con la que
	 *                     fueron firmados los datos
	 * @return true si la firma es valida para los datos y clave publica dados
	 * @throws InvalidKeySpecException 
	 * @throws NoSuchAlgorithmException 
	 * @throws NoSuchProviderException 
	 * @throws InvalidKeyException 
	 * @throws SignatureException 
	 */
	public static boolean validarFirma(byte[] info, byte[] firma, byte[] clavePublica) {

		try {
			System.out.println(clavePublica);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(clavePublica);
			PublicKey publicKeyObj = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(clavePublica));
			
			// Validar firma
			Signature sig = Signature.getInstance("SHA256withRSA");
			sig.initVerify(publicKeyObj);			
			sig.update(info);
			return sig.verify(firma);
			
		} catch(InvalidKeySpecException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		
		return false;			
	}


	/**
	 * Firmar unos datos con una clave privada dada
	 * 
	 * @param info         datos a ser firmados
	 * @param clavePrivada para firmar los datos
	 * @return firma de los datos y que puede ser verificada con los datos y la
	 *         clave publica
	 */
	public static byte[] firmar(byte[] info, byte[] clavePrivada) throws Exception {

		// Creacion del objeto PrivateKey con la clave dada
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(clavePrivada);
		PrivateKey privateKeyObj = keyFactory.generatePrivate(keySpec);

		// Firma
		Signature sig = getInstanciaSignature();
		sig.initSign(privateKeyObj);
		sig.update(info);
		return sig.sign();
	}

	private static Signature getInstanciaSignature() throws NoSuchProviderException, NoSuchAlgorithmException {
		return Signature.getInstance("SHA1withDSA","SUN"); 
	}
}