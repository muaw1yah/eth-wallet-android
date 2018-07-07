package com.example.crimson.crimson;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import utils.SampleSQLiteDBHelper;
import wallet.CrimsonWallet;
import wallet.WalletLIstAdapter;


/**
 */
public class WalletFragment extends Fragment {
    private TextView mTextMessage;
    private RequestQueue queue;
    private MaterialDialog.Builder builder;
    private MaterialDialog dialog;
    private SampleSQLiteDBHelper helper;
    private ArrayList wallet_list;
    private WalletLIstAdapter walletAdapter;
    private FloatingActionButton addWalletBtn;


    public static WalletFragment newInstance() {
        WalletFragment fragment = new WalletFragment();
        return fragment;
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

        helper = new SampleSQLiteDBHelper(getActivity().getApplicationContext());
        queue = Volley.newRequestQueue(getActivity().getApplicationContext());

        wallet_list = helper.getAllWallets();
        walletAdapter = new WalletLIstAdapter(getActivity(), R.layout.single_wallet, wallet_list);

        if(wallet_list.size() > 0) {
            mTextMessage.setVisibility(View.GONE);
        }

        if(wallet_list.size() > 4) {
            addWalletBtn.setVisibility(View.GONE);
        }
        //helper.onUpgrade(helper.getWritableDatabase(), 1, 1);

        ListView walletListAdapter = myFragmentView.findViewById(R.id.wallet_list);

        walletListAdapter.setAdapter(walletAdapter);

        addWalletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewWallet();
            }
        });

        return myFragmentView;
    }

    public void createNewWallet() {
        if(wallet_list.size() > 4) {
            //Toast.makeText(getActivity().getApplicationContext(), "Cannot add more than 5 wallets", Toast.LENGTH_SHORT).show();
            Snackbar.make(getView(), "Cannot add more than 5 wallets", Snackbar.LENGTH_LONG).show();
            return;
        }
        builder
                .content(R.string.creating_wallet_loading)
                .progress(true, 0);

        dialog = builder.build();
        dialog.show();

        String url = "https://pure-chamber-22089.herokuapp.com/wallet/";
        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        try {
                            JSONObject parentObject = new JSONObject(response);
                            String address = parentObject.getString("publicAddress");
                            String key = parentObject.getString("privateKey");
                            String name = "Wallet " + (wallet_list.size() + 1);
                            CrimsonWallet wallet = helper.saveToDB(new CrimsonWallet(name, address, key, 0));
                            Log.i("ID", "Wallet Id: " + wallet.getId());
                            if(mTextMessage.getVisibility() == View.VISIBLE) {
                                mTextMessage.setVisibility(View.GONE);
                            }
                            wallet_list.add(wallet);
                            walletAdapter.notifyDataSetChanged();
                            Snackbar.make(getView(), "New wallet has been added ", Snackbar.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Snackbar.make(getView(), "Cannot add wallet", Snackbar.LENGTH_LONG).show();
                        }
                        dialog.dismiss();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Snackbar.make(getView(), "Cannot add wallet", Snackbar.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                }
        );
        queue.add(postRequest);
    }


}