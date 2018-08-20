package com.example.crimson.crimson;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;

import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;

import utils.SampleSQLiteDBHelper;
import models.CrimsonWallet;


/**
 * A simple {@link Fragment} subclass.
 */
public class SendFragment extends Fragment {
    private SampleSQLiteDBHelper helper;
    SharedPreferences sharedPref;

    public static SendFragment newInstance() {
        SendFragment fragment = new SendFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        String scannedValue = sharedPref.getString(MainActivity.SCANNED_ADDRESS, null);

        View myFragmentView = inflater.inflate(R.layout.fragment_send, container, false);
        helper = new SampleSQLiteDBHelper(getActivity().getApplicationContext());

        Button button = myFragmentView.findViewById((R.id.scan_to_send));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ScanToSendActivity.class);
                startActivity(intent);
            }
        });

        MaterialSpinner dynamicSpinner = myFragmentView.findViewById(R.id.send_from_wallets);

        ArrayList items = new ArrayList<String>();
        for(CrimsonWallet wallet: helper.getAllWallets()) {
            items.add(wallet.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, items);

        dynamicSpinner.setAdapter(adapter);

        dynamicSpinner.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener<String>() {
            @Override public void onItemSelected(MaterialSpinner view, int position, long id, String item) {
                Snackbar.make(view, "Clicked " + item, Snackbar.LENGTH_LONG).show();
            }
        });

        if(scannedValue != null) {
            Snackbar.make(getView(), scannedValue + " scanned", Snackbar.LENGTH_LONG).show();
        }

        return myFragmentView;
    }


}
