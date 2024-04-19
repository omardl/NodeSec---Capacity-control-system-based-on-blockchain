package com.example.demo;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NodoMain {

	public static byte[] ClavePublicaNodo;

	public static void main(String[] args) throws NoSuchProviderException, NoSuchAlgorithmException {
		
		SpringApplication.run(NodoMain.class, args);
		System.out.println("NODO INICIADO");
		
	}
}