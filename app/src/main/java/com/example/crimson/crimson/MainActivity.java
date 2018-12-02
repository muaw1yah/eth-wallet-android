package com.example.crimson.crimson;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.app.FragmentTransaction;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import android.app.Fragment;
import android.widget.Button;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.kenai.jffi.Main;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import models.Balance;
import models.CrimsonWallet;
import models.MyObjectBox;
import models.Token;
import models.Wallet;

import static utils.Constants.CURRENT_CHANNEL;
import static utils.Constants.MAINNET_CHANNEL;
import static utils.Constants.SCANNED_TO_WATCH_ADDRESS;

public class MainActivity extends AppCompatActivity  {
    private int current;
    public static BoxStore boxStore;
    public static Box<Wallet> walletBox;
    public static Box<Token> tokenBox;
    public static Box<Balance> balanceBox;
    public static RequestQueue queue;

    public static SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

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
        //new CheckBalance().execute();

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_wallet);
        current = R.id.navigation;

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        if(sharedPref.getString(CURRENT_CHANNEL, null) == null) {
            editor.putString(CURRENT_CHANNEL, MAINNET_CHANNEL);
            editor.commit();
        }

        if(boxStore == null) {
            boxStore = MyObjectBox.builder()
                    .androidContext(MainActivity.this).build();
        }

        if(queue == null) queue = Volley.newRequestQueue(MainActivity.this);
        if(walletBox == null) walletBox = MainActivity.boxStore.boxFor(Wallet.class);
        if(tokenBox == null) tokenBox = MainActivity.boxStore.boxFor(Token.class);
        if(balanceBox == null) balanceBox = MainActivity.boxStore.boxFor(Balance.class);

//        walletBox.removeAll();
//        balanceBox.removeAll();

    }

//    private class CheckBalance extends AsyncTask<Void, Void, Void> {
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            try {
//                CrimsonWallet.connectEthereum();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//    }
}
