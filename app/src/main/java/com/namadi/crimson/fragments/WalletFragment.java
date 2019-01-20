package com.namadi.crimson.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.namadi.crimson.activities.MainActivity;
import com.namadi.crimson.R;

import java.util.ArrayList;

import com.namadi.crimson.models.Token;
import com.namadi.crimson.models.Wallet;
import com.namadi.crimson.wallet.WalletLIstAdapter;

import static com.namadi.crimson.utils.Constants.MAINNET_CHANNEL;


/**
 */
public class WalletFragment extends Fragment {
    private TextView mTextMessage;
    private MaterialDialog.Builder builder;
    private MaterialDialog dialog;
    private ArrayList wallet_list;
    private WalletLIstAdapter walletAdapter;
    private FloatingActionButton addWalletBtn;

    public static WalletFragment newInstance() {
        return new WalletFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        builder = new MaterialDialog.Builder(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View myFragmentView = inflater.inflate(R.layout.fragment_wallet, container, false);
        mTextMessage = myFragmentView.findViewById(R.id.no_wallet_message);
        addWalletBtn = myFragmentView.findViewById(R.id.fab);

        wallet_list = new ArrayList<>(MainActivity.walletBox.getAll());
        //wallet_list = helper.getAllWallets();
        walletAdapter = new WalletLIstAdapter(getActivity(), R.layout.single_wallet, wallet_list);

        if(wallet_list.size() > 0) {
            mTextMessage.setVisibility(View.GONE);
        }

        if(wallet_list.size() > 4) {
            addWalletBtn.setVisibility(View.GONE);
        }

        ListView walletListAdapter = myFragmentView.findViewById(R.id.wallet_list);

        walletListAdapter.setAdapter(walletAdapter);

        addWalletBtn.setOnClickListener(view -> createNewWallet());

        return myFragmentView;
    }

    public void createNewWallet() {
        if(wallet_list.size() > 4) {
            Snackbar.make(getView(), "Cannot add more than 5 wallets", Snackbar.LENGTH_LONG).show();
            return;
        }
        builder
                .content(R.string.creating_wallet_loading)
                .cancelable(false)
                .progress(true, 0);

        dialog = builder.build();
        dialog.show();

        Wallet wallet = null;
        try {
            wallet = Wallet.WalletFactory();
            String name = "Wallet " + (wallet_list.size() + 1);
            wallet.setName(name);
            wallet = MainActivity.walletBox.get(MainActivity.walletBox.put(wallet));

            Log.i("ID", "Wallet Id: " + wallet.getId());
            if(mTextMessage.getVisibility() == View.VISIBLE) {
                Token token = new Token();
                token.setDecimal(18);
                token.setName("Binance");
                token.setAddress("0xB8c77482e45F1F44dE1745F52C74426C631bDD52");
                token.setSymbol("BNB");
                token.setChannel(MAINNET_CHANNEL);
                MainActivity.tokenBox.put(token);
                mTextMessage.setVisibility(View.GONE);
            }
            wallet_list.add(wallet);
            walletAdapter.notifyDataSetChanged();
            Snackbar.make(getView(), "New wallet has been added ", Snackbar.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("CREATE WALLET", e.getMessage());
        } finally {
            dialog.dismiss();
        }

        if(wallet != null) {
            MainActivity.updateBalance(wallet);
        }

//        StringRequest postRequest = new StringRequest(Request.Method.POST, GENERATE_WALLET,
//                response -> {
//                    try {
//                        JSONObject parentObject = new JSONObject(response);
//                        String address = parentObject.getString("publicAddress");
//                        String key = parentObject.getString("privateKey");
//                        String name = "Wallet " + (wallet_list.size() + 1);
//                        Wallet com.namadi.crimson.wallet = new Wallet();
//                        com.namadi.crimson.wallet.setName(name);
//                        com.namadi.crimson.wallet.setAddress(address);
//                        com.namadi.crimson.wallet.setKey(key);
//                        com.namadi.crimson.wallet = MainActivity.walletBox.get(MainActivity.walletBox.put(com.namadi.crimson.wallet));
//
//                        Log.i("ID", "Wallet Id: " + com.namadi.crimson.wallet.getId());
//                        if(mTextMessage.getVisibility() == View.VISIBLE) {
//                            mTextMessage.setVisibility(View.GONE);
//                        }
//                        wallet_list.add(com.namadi.crimson.wallet);
//                        walletAdapter.notifyDataSetChanged();
//                        Snackbar.make(getView(), "New com.namadi.crimson.wallet has been added ", Snackbar.LENGTH_LONG).show();
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                        Snackbar.make(getView(), "Cannot add com.namadi.crimson.wallet", Snackbar.LENGTH_LONG).show();
//                    }
//                    dialog.dismiss();
//                },
//                error -> {
//                    Snackbar.make(getView(), "Cannot add com.namadi.crimson.wallet", Snackbar.LENGTH_LONG).show();
//                    dialog.dismiss();
//                }
//        );
        //MainActivity.queue.add(postRequest);
    }


}