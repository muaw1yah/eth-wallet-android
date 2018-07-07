package com.example.crimson.crimson;


import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;

import utils.SampleSQLiteDBHelper;
import wallet.CrimsonWallet;


/**
 * A simple {@link Fragment} subclass.
 */
public class SendFragment extends Fragment {
    private SampleSQLiteDBHelper helper;

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

        View myFragmentView = inflater.inflate(R.layout.fragment_send, container, false);
        helper = new SampleSQLiteDBHelper(getActivity().getApplicationContext());

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

        return myFragmentView;
    }

}
