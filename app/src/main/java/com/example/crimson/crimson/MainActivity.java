package com.example.crimson.crimson;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.FragmentTransaction;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import android.app.Fragment;
import android.widget.Button;

import models.CrimsonWallet;

public class MainActivity extends AppCompatActivity  {
    private int current;
    private Button scanToSendBtn;
    public static final String SCANNED_ADDRESS = "scanned-address";

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            Fragment selectedFragment = null;
            if(current == item.getItemId()) {
                return false;
            }
            current = item.getItemId();
            switch (item.getItemId()) {
                case R.id.navigation_wallet:
                    selectedFragment = WalletFragment.newInstance();
                    break;
                case R.id.navigation_send:
                    selectedFragment = SendFragment.newInstance();
                    break;
                case R.id.navigation_settings:
                    selectedFragment = SettingsFragment.newInstance();
                    break;
            }
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, selectedFragment);
            transaction.commit();
            return true;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        new CheckBalance().execute();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_wallet);
        current = R.id.navigation;
    }

    private class CheckBalance extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                CrimsonWallet.connectEthereum();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
