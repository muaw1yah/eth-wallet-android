package com.example.crimson.crimson;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.jaredrummler.materialspinner.MaterialSpinner;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import models.Wallet;
import utils.SampleSQLiteDBHelper;
import models.CrimsonWallet;

import static utils.Constants.MAINNET_URL;
import static utils.Constants.ROPSTEN_URL;
import static utils.Constants.SCANNED_TO_SEND_ADDRESS;
import static utils.Constants.SCANNED_TO_WATCH_ADDRESS;


/**
 * A simple {@link Fragment} subclass.
 */
public class SendFragment extends Fragment {
    private List<Wallet> wallets;
    private Wallet selectedWallet;
    private TextInputEditText toAddressInput, amountInput;
    private Button sendBtn;

    Double amountToSend;
    String addressToSend;
    Wallet toSendFrom;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;



    public static SendFragment newInstance() {
        SendFragment fragment = new SendFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(ScanToSendActivity.scannedValue != null) {
            if(toAddressInput != null) {
                addressToSend = ScanToSendActivity.scannedValue;
                toAddressInput.setText(addressToSend);
                ScanToSendActivity.scannedValue = null;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        //Log.i("SEND SCAN", sharedPref.getString(SCANNED_TO_SEND_ADDRESS, "NO MSG"));

        wallets = MainActivity.walletBox.getAll();

        View myFragmentView = inflater.inflate(R.layout.fragment_send, container, false);

        toAddressInput = myFragmentView.findViewById(R.id.address_to_send);
        amountInput = myFragmentView.findViewById(R.id.amount_to_send);

        Button button = myFragmentView.findViewById((R.id.scan_to_send));
        sendBtn = myFragmentView.findViewById((R.id.send));
        button.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ScanToSendActivity.class);
            startActivity(intent);
        });

        sendBtn.setOnClickListener(view -> {
            try {
                sendEther(toSendFrom, addressToSend, amountToSend);
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.i("SEND-INT", e.getMessage());
            } catch (TransactionException e) {
                Log.i("SEND-TRAN", e.getMessage());
            } catch (IOException e) {
                Log.i("SEND-IO", e.getMessage());
            } catch (ExecutionException e) {
                e.printStackTrace();
                Log.i("SEND-EX", e.getMessage());
            }
        });

        MaterialSpinner dynamicSpinner = myFragmentView.findViewById(R.id.send_from_wallets);

        int index = 1;
        ArrayList items = new ArrayList<String>();
        HashMap<String, Wallet> walletMap = new HashMap<String, Wallet>();
        items.add("Select Wallet To send From");

        for(Wallet wallet: wallets) {
            items.add(wallet.getName());
            walletMap.put(wallet.getName() + index, wallet);
            index++;
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, items);

        dynamicSpinner.setAdapter(adapter);

        dynamicSpinner.setOnItemSelectedListener((MaterialSpinner.OnItemSelectedListener<String>) (view, position, id, item) -> {
            Log.i("SEND - POSITION", String.valueOf(position));
            if(position != 0) {
                amountInput.setVisibility(View.VISIBLE);
                selectedWallet = walletMap.get(item + position);
                toSendFrom = selectedWallet;
            }
        });


        toAddressInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().length() > 20) {
                    dynamicSpinner.setVisibility(View.VISIBLE);
                    addressToSend = charSequence.toString();
                } else {
                    dynamicSpinner.setVisibility(View.INVISIBLE);
                    amountInput.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        amountInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                amountToSend = Double.parseDouble(charSequence.toString());
                if(amountToSend > 0) {
                    sendBtn.setVisibility(View.VISIBLE);
                } else {
                    sendBtn.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        return myFragmentView;
    }

    private void sendEther(Wallet wallet, String toAccount, double amount) throws InterruptedException, TransactionException, IOException, ExecutionException {
        Web3j web3j = Web3jFactory.build(new HttpService(ROPSTEN_URL));

//        Credentials credentials = Credentials.create(wallet.getKey(), wallet.getAddress());
        Credentials credentials = Credentials.create(wallet.getKey());

        TransactionReceipt transactionReceipt = Transfer.sendFunds(web3j,
                credentials,
                toAccount,
                BigDecimal.ONE,
                Convert.Unit.ETHER).sendAsync().get();

        System.out.println("Transaction Hash:"+transactionReceipt.getTransactionHash());
        Log.i("SEND", transactionReceipt.getTransactionHash());
    }

    private void sendToken(Wallet wallet, String toAccount, double amount) throws Exception {
        Web3j web3j = Web3jFactory.build(new HttpService());

        Credentials credentials = Credentials.create(wallet.getKey(), wallet.getAddress());

        //get next available nonce
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                wallet.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
        BigInteger nonce = ethGetTransactionCount.getTransactionCount();

        //create transaction
        RawTransaction rawTransaction  = RawTransaction.createEtherTransaction(
                nonce, BigInteger.ONE, BigInteger.ONE, toAccount, BigInteger.ONE);

        // sign transaction
        byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
        String hexValue = Hex.toHexString(signedMessage);

        // send transaction
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

        System.out.println("Transaction Hash:"+ethSendTransaction.getTransactionHash());
        Log.i("SEND", ethSendTransaction.getTransactionHash());
    }
}
