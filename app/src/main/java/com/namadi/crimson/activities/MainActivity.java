package com.namadi.crimson.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.view.MenuItemCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.orangegangsters.lollipin.lib.PinActivity;
import com.github.orangegangsters.lollipin.lib.managers.LockManager;
import com.namadi.crimson.R;
import com.namadi.crimson.activities.pin.CustomPinActivity;
import com.namadi.crimson.fragments.SendFragment;
import com.namadi.crimson.fragments.SettingsFragment;
import com.namadi.crimson.fragments.WalletFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Stream;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;

import com.namadi.crimson.models.Balance;
import com.namadi.crimson.models.Balance_;
import com.namadi.crimson.models.MyObjectBox;
import com.namadi.crimson.models.Token;
import com.namadi.crimson.models.Token_;
import com.namadi.crimson.models.Tx;
import com.namadi.crimson.models.Wallet;

import static com.namadi.crimson.utils.Constants.CURRENT_CHANNEL;
import static com.namadi.crimson.utils.Constants.MAINNET_CHANNEL;
import static com.namadi.crimson.utils.Constants.MAINNET_TOKEN_BALANCE;
import static com.namadi.crimson.utils.Constants.MAINNET_URL;
import static com.namadi.crimson.utils.Constants.RINKEBY_CHANNEL;
import static com.namadi.crimson.utils.Constants.RINKEBY_TOKEN_BALANCE;
import static com.namadi.crimson.utils.Constants.RINKEBY_URL;
import static com.namadi.crimson.utils.Constants.ROPSTEN_CHANNEL;
import static com.namadi.crimson.utils.Constants.ROPSTEN_TOKEN_BALANCE;
import static com.namadi.crimson.utils.Constants.ROPSTEN_URL;
import static com.namadi.crimson.utils.Constants.WEI2ETH;

public class MainActivity extends PinActivity {
    private int current;
    public static BoxStore boxStore;
    public static Box<Wallet> walletBox;
    public static Box<Token> tokenBox;
    public static Box<Balance> balanceBox;
    public static Box<Tx> txBox;
    public static RequestQueue queue;

    public static SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public static String currentChannel;
    private Fragment selectedFragment = null;
    public static Spinner spinner;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            if (current == item.getItemId()) {
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

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (resultCode == PinListener.CANCELLED) {
//            finish();
//        }
//
//        if (resultCode == PinListener.FORGOT) {
//            builder = new MaterialDialog.Builder(this);
//            builder
//                    .title("Wipe App Data?")
//                    .content("Are you sure you want to delete all app data, which includes (Wallet(s), Token(s), Tx(s)?")
//                    .negativeText("No")
//                    .positiveText("Yes")
//                    .cancelable(false)
//                    .onPositive((dialog, which) -> {
//                        sharedPref
//                                .edit()
//                                .clear()
//                                .apply();
//                        walletBox.removeAll();
//                        tokenBox.removeAll();
//                        balanceBox.removeAll();
//                        dialog.dismiss();
//                        finish();
//                    })
//                    .onNegative((dialog, which) -> {
//                        dialog.dismiss();
//                        finish();
//                        startActivity(getIntent());
//                    })
//                    .onAny((dialog, which) -> {
//                        dialog.dismiss();
//                        finish();
//                        startActivity(getIntent());
//                    });
//
//
//            dialog = builder.build();
//            dialog.show();
//        }
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.networks, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.post(() -> spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedChannel = MAINNET_CHANNEL;
                if (i == 1) {
                    selectedChannel = RINKEBY_CHANNEL;
                } else if (i == 2) {
                    selectedChannel = ROPSTEN_CHANNEL;
                }

                if (currentChannel == selectedChannel) {
                    return;
                }

                currentChannel = selectedChannel;
                editor.putString(CURRENT_CHANNEL, selectedChannel);
                editor.commit();

                Snackbar.make(findViewById(R.id.container), "Network Switched", Snackbar.LENGTH_SHORT).show();
                syncBalance();

                Fragment fragment = selectedFragment;
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.detach(fragment);
                transaction.attach(fragment);
                transaction.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        }));

        if (currentChannel == RINKEBY_CHANNEL) {
            spinner.setSelection(1);
        } else if (currentChannel == ROPSTEN_CHANNEL) {
            spinner.setSelection(2);
        }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navigation.setSelectedItemId(R.id.navigation_wallet);
        current = R.id.navigation;

        sharedPref = getPreferences(Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        LockManager<CustomPinActivity> lockManager = LockManager.getInstance();
        lockManager.enableAppLock(this, CustomPinActivity.class);

//        String pin = sharedPref.getString("SET-PIN", null);
//        if (pin == null) {
//            Intent intent = new Intent(this, CustomPinActivity.class);
//            startActivityForResult(intent, 1);
//        } else {
//            Intent intent = new Intent(this, ConfirmPinActivity.class);
//            startActivityForResult(intent, 1);
//        }

        if (sharedPref.getString(CURRENT_CHANNEL, null) == null) {
            editor.putString(CURRENT_CHANNEL, MAINNET_CHANNEL);
            editor.apply();
        }

        if (boxStore == null) {
            boxStore = MyObjectBox.builder()
                    .androidContext(MainActivity.this).build();
        }

        if (queue == null) queue = Volley.newRequestQueue(MainActivity.this);
        if (walletBox == null) walletBox = MainActivity.boxStore.boxFor(Wallet.class);
        if (tokenBox == null) tokenBox = MainActivity.boxStore.boxFor(Token.class);
        if (balanceBox == null) balanceBox = MainActivity.boxStore.boxFor(Balance.class);
        if (txBox == null) txBox = MainActivity.boxStore.boxFor(Tx.class);

        Intent intent = getIntent();
        String navigate = intent.getStringExtra("NAVIGATE");
        if (navigate != null && navigate.equals("SEND")) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, SendFragment.newInstance());
            transaction.commit();
        }

        currentChannel = sharedPref.getString(CURRENT_CHANNEL, MAINNET_CHANNEL);
        syncBalance();
    }

    public static void syncBalance() {
        for (Wallet wallet : walletBox.getAll()) {
            updateBalance(wallet);
            updateTokenBalance(wallet);
        }
    }

    public static void updateTokenBalance(Wallet wallet) {
        QueryBuilder<Token> builder = MainActivity.tokenBox.query();
        builder.equal(Token_.channel, currentChannel);
        List<Token> tokens = builder.build().find();

        for (Token t : tokens) {
            if (!t.getChannel().equals(currentChannel)) {
                continue;
            }
            String URL = MAINNET_TOKEN_BALANCE;
            if (currentChannel == ROPSTEN_CHANNEL) {
                URL = ROPSTEN_TOKEN_BALANCE;
            } else if (currentChannel == RINKEBY_CHANNEL) {
                URL = RINKEBY_TOKEN_BALANCE;
            }

            URL = String.format(URL, t.getAddress(), wallet.getAddress());

            StringRequest postRequest = new StringRequest(Request.Method.GET, URL,
                    response -> {
                        try {
                            JSONObject parentObject = new JSONObject(response);
                            Double balance = parentObject.getDouble("balance");

                            Query<Balance> query = MainActivity.balanceBox.query()
                                    .equal(Balance_.walletToken, wallet.getAddress() + "-" + t.getSymbol() + "-" + currentChannel).build();

                            Balance balanceObj = query.findUnique();

                            if (balanceObj != null) {
                                balanceObj.setBalance(balance);
                                balanceObj.setToken(t.getSymbol());
                                balanceObj.setChannel(currentChannel);
                            } else {
                                balanceObj = new Balance();
                                balanceObj.setWalletToken(wallet.getAddress() + "-" + t.getSymbol() + "-" + currentChannel);
                                balanceObj.setBalance(balance);
                                balanceObj.setToken(t.getSymbol());
                                balanceObj.setChannel(currentChannel);
                            }

                            MainActivity.balanceBox.put(balanceObj);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i("TOKEN", e.getMessage());
                        }
                    },
                    error -> {
                        Log.i("TOKEN", error.toString());
                    }
            );
            MainActivity.queue.add(postRequest);
        }
    }

    public static void updateBalance(Wallet wallet) {
        String URL = MAINNET_URL;
        if (currentChannel == ROPSTEN_CHANNEL) {
            URL = ROPSTEN_URL;
        } else if (currentChannel == RINKEBY_CHANNEL) {
            URL = RINKEBY_URL;
        }

        StringRequest request = new StringRequest(Request.Method.POST, URL, response -> {

            try {
                JSONObject obj = new JSONObject(response);
                String result = obj.getString("result");
                Long wei = Long.parseLong(result.substring(2), 16);
                Double balance = Double.valueOf(wei) / Double.valueOf(WEI2ETH);

                Query<Balance> query = MainActivity.balanceBox.query()
                        .equal(Balance_.walletToken, wallet.getAddress() + "-" + currentChannel).build();

                Balance balanceObj = query.findUnique();

                if (balanceObj != null) {
                    balanceObj.setBalance(balance);
                    balanceObj.setToken("ETH");
                    balanceObj.setChannel(currentChannel);
                } else {
                    balanceObj = new Balance();
                    balanceObj.setWalletToken(wallet.getAddress() + "-" + currentChannel);
                    balanceObj.setBalance(balance);
                    balanceObj.setToken("ETH");
                    balanceObj.setChannel(currentChannel);
                }

                MainActivity.balanceBox.put(balanceObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.i("BALANCE-ERROR", error.toString())) {
            @Override
            public byte[] getBody() {
                JSONObject obj = new JSONObject();

                JSONArray data = new JSONArray();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Stream.of(new String[]{wallet.getAddress(), "latest"})
                            .forEach(data::put);
                } else {
                    data.put(wallet.getAddress());
                    data.put("latest");
                }

                try {
                    obj.put("params", data);
                    obj.put("jsonrpc", "2.0");
                    obj.put("method", "eth_getBalance");
                    obj.put("id", "1");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return obj.toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        MainActivity.queue.add(request);
    }
}
