package com.namadi.crimson.fragments;


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
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.namadi.crimson.activities.MainActivity;
import com.namadi.crimson.activities.ScanToSendActivity;
import com.namadi.crimson.R;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
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

import com.namadi.crimson.models.Token;
import com.namadi.crimson.models.Wallet;

import static com.namadi.crimson.utils.Constants.ROPSTEN_URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class SendFragment extends Fragment {
    private List<Wallet> wallets;
    private List<Token> tokens;
    private Wallet selectedWallet;
    private Token selectedToken;
    private TextInputEditText toAddressInput, amountInput;
    private LinearLayout sendLayout;
    private Button sendBtn;

    private MaterialDialog.Builder builder;
    private MaterialDialog dialog;

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

        builder = new MaterialDialog.Builder(getActivity());
        // Inflate the layout for this fragment
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        //Log.i("SEND SCAN", sharedPref.getString(SCANNED_TO_SEND_ADDRESS, "NO MSG"));

        wallets = MainActivity.walletBox.getAll();
        tokens = MainActivity.tokenBox.getAll();

        View myFragmentView = inflater.inflate(R.layout.fragment_send, container, false);

        toAddressInput = myFragmentView.findViewById(R.id.address_to_send);
        amountInput = myFragmentView.findViewById(R.id.amount_to_send);
        sendLayout = myFragmentView.findViewById(R.id.send_layout);

        Button button = myFragmentView.findViewById((R.id.scan_to_send));
        sendBtn = myFragmentView.findViewById((R.id.send));
        button.setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), ScanToSendActivity.class);
            startActivity(intent);
        });

        sendBtn.setOnClickListener(view -> {
            builder
                    .content(R.string.sending_transaction)
                    .progress(true, 0);
            dialog = builder.build();
            dialog.show();

            try {
                sendEther(toSendFrom, addressToSend, amountToSend);
            } catch (InterruptedException e) {
                dialog.dismiss();
                e.printStackTrace();
                Log.i("SEND-INT", e.getMessage());
                Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            } catch (TransactionException e) {
                dialog.dismiss();
                Log.i("SEND-TRAN", e.getMessage());
                Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            } catch (IOException e) {
                dialog.dismiss();
                Log.i("SEND-IO", e.getMessage());
                Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            } catch (ExecutionException e) {
                dialog.dismiss();
                Log.i("SEND-EX", e.getMessage());
                Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
            } finally {
                dialog.dismiss();
            }
        });

        MaterialSpinner dynamicSpinner = myFragmentView.findViewById(R.id.send_from_wallets);
        MaterialSpinner tokenSpinner = myFragmentView.findViewById(R.id.send_select_token);


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
            if(wallets.size() == 1) {
                tokenSpinner.setVisibility(View.VISIBLE);
                selectedWallet = wallets.get(0);
                toSendFrom = selectedWallet;
                return;
            }

            if(position != 0) {
                Log.i("SEND", "show box");
                tokenSpinner.setVisibility(View.VISIBLE);
                selectedWallet = walletMap.get(item + position);
                toSendFrom = selectedWallet;
            } else {
                tokenSpinner.setVisibility(View.GONE);
            }
        });

        int i = 1;
        ArrayList tokenItems = new ArrayList<String>();
        HashMap<String, Token> tokenMap = new HashMap<>();
        tokenItems.add("Send Token To Send");

        for(Token t: tokens) {
            Log.i("SEND", t.getName());
            tokenItems.add(t.getName());
            tokenMap.put(t.getName() + index, t);
            i++;
        }

        ArrayAdapter<String> tokenAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, tokenItems);

        tokenSpinner.setAdapter(tokenAdapter);

        tokenSpinner.setOnItemSelectedListener((MaterialSpinner.OnItemSelectedListener<String>) (view, position, id, item) -> {
            if(tokens.size() == 1) {
                sendLayout.setVisibility(View.VISIBLE);
                selectedToken = tokens.get(0);
                return;
            }
            if(position != 0) {
                sendLayout.setVisibility(View.VISIBLE);
                selectedToken = tokenMap.get(item + position);
            } else {
                sendLayout.setVisibility(View.GONE);
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
                    sendLayout.setVisibility(View.GONE);
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
                if(charSequence.length() < 1) {
                    return;
                }
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

        Credentials credentials = Credentials.create(wallet.getKey());

        TransactionReceipt transactionReceipt = Transfer.sendFunds(web3j,
                credentials,
                toAccount,
                BigDecimal.valueOf(amount),
                Convert.Unit.ETHER).sendAsync().get();

        System.out.println("Tx Hash:"+transactionReceipt.getTransactionHash());
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

        System.out.println("Tx Hash:"+ethSendTransaction.getTransactionHash());
        Log.i("SEND", ethSendTransaction.getTransactionHash());
    }
}
