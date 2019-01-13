package com.namadi.crimson.models;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletFile;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;

import io.objectbox.annotation.Entity;
import io.objectbox.annotation.Id;
import io.objectbox.annotation.Unique;

@Entity
public class Wallet {
    @Id
    private long id;

    @Unique
    private String address;

    private String key;

    private String name;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Wallet(String address, String key) {
        this.address = address;
        this.key = key;
    }

    public static Wallet WalletFactory() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CipherException {

        SecureRandom rnd = new SecureRandom();
        byte[] token = new byte[256];
        rnd.nextBytes(token);
        String seed = token.toString();

            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();

            String sPrivatekeyInHex = privateKeyInDec.toString(16);

            WalletFile aWallet = org.web3j.crypto.Wallet.createLight("namadi", ecKeyPair);
            String sAddress = aWallet.getAddress();


            return new Wallet("0x"+sAddress, sPrivatekeyInHex);
    }

    public Wallet() {}
}
