package models;

import android.content.Context;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import de.petendi.ethereum.android.EthereumAndroid;
import de.petendi.ethereum.android.EthereumAndroidCallback;
import de.petendi.ethereum.android.EthereumAndroidFactory;
import de.petendi.ethereum.android.EthereumNotInstalledException;
import de.petendi.ethereum.android.service.model.RpcCommand;
import de.petendi.ethereum.android.service.model.ServiceError;
import de.petendi.ethereum.android.service.model.WrappedRequest;
import de.petendi.ethereum.android.service.model.WrappedResponse;

/**
 * Created by mu'awiyah namadi on 19/05/2018.
 */

public class CrimsonWallet implements EthereumAndroidCallback {
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

    public static void connectEthereum() throws Exception {
        Web3j web3 = Web3jFactory.build(new HttpService("https://rinkeby.infura.io/itRXy2X4KtzI7YZTq9wL"));
        Web3ClientVersion web3ClientVersion = null;
        web3ClientVersion = web3.web3ClientVersion().send();
        String clientVersion = web3ClientVersion.getWeb3ClientVersion();
        Log.i("ETH", "Connected to " + clientVersion);
    }

    public long getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public double getBalance(Context context) throws EthereumNotInstalledException {
        EthereumAndroidFactory ethereumAndroidFactory = new EthereumAndroidFactory(context);
        EthereumAndroid ethereumAndroid = ethereumAndroidFactory.create(this);

        WrappedRequest wrappedRequest = new WrappedRequest();
        wrappedRequest.setCommand(RpcCommand.eth_getBalance.toString());
        wrappedRequest.setParameters(new String[]{this.getAddress(), "latest"});
        ethereumAndroid.sendAsync(wrappedRequest);

        return balance;
    }

    public void newWallet() {
        String password = "test";
        //String walletFileName = WalletUtils.generateFullNewWalletFile(password, new File("Keystore path"));
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.name = balance;
    }

    @Override
    public void handleResponse(int messageId, WrappedResponse response) {

    }

    @Override
    public void handleError(int messageId, ServiceError serviceError) {

    }
}
