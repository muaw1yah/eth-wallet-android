package wallet;

import android.util.Log;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Created by mu'awiyah namadi on 19/05/2018.
 */

public class CrimsonWallet {
    private String name;
    private String address;
    private String privateKey;
    private double balance;
    private long id;

    public CrimsonWallet() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CipherException {
        String seed = "namady";
        ECKeyPair ecKeyPair = Keys.createEcKeyPair();
        BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();

        String sPrivatekeyInHex = privateKeyInDec.toString(16);

        WalletFile aWallet = Wallet.createLight(seed, ecKeyPair);
        String sAddress = aWallet.getAddress();

        this.name = "";
        this.address = "0x"+sAddress;
        this.privateKey = sPrivatekeyInHex;
        this.balance = 0;
        this.id = -1;
    }

    public CrimsonWallet(String address, String privateKey) {
        this.name = "";
        this.address = address;
        this.privateKey = privateKey;
        this.balance = 0;
        this.id = -1;
    }

    public CrimsonWallet(String name, String address, String privateKey, double balance) {
        this.name = name;
        this.address = address;
        this.privateKey = privateKey;
        this.balance = balance;
        this.id = -1;
    }

    public CrimsonWallet(int id, String name, String address, String privateKey, double balance) {
        this.name = name;
        this.address = address;
        this.privateKey = privateKey;
        this.balance = balance;
        this.id = id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setBalance(String balance) {
        this.name = balance;
    }

    public String getBalace() {
        return this.balance + "";
    }

    public String getAddress() {
        return address;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public double getBalance() {
        return balance;
    }
}
