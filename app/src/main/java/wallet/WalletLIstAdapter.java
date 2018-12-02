package wallet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.crimson.crimson.MainActivity;
import com.example.crimson.crimson.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import io.objectbox.query.Query;
import io.objectbox.query.QueryBuilder;
import models.Balance;
import models.Balance_;
import models.Wallet;

import static utils.Constants.CURRENT_CHANNEL;
import static utils.Constants.MAINNET_CHANNEL;
import static utils.Constants.MAINNET_URL;
import static utils.Constants.RINKEBY_CHANNEL;
import static utils.Constants.RINKEBY_URL;
import static utils.Constants.ROPSTEN_CHANNEL;
import static utils.Constants.ROPSTEN_URL;
import static utils.Constants.WEI2ETH;

/**
 * Created by crimson on 01/06/2018.
 */

public class WalletLIstAdapter extends ArrayAdapter<Wallet> {
    private MaterialDialog.Builder builder;
    private TextView walletNameView;
    private TextView walletAddressView;
    private TextView walletBalanceView;
    private TextInputEditText walletNameInput;
    private HashMap<String, Balance> balanceMap;
    private String currentChannel;

    public WalletLIstAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public WalletLIstAdapter(Context context, int resource, List<Wallet> items) {
        super(context, resource, items);
        balanceMap = new HashMap<String, Balance>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        currentChannel = MainActivity.sharedPref.getString(CURRENT_CHANNEL, null);

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.single_wallet, null);
        }

        final Wallet wallet = getItem(position);
        final TextView listWalletName = v.findViewById(R.id.list_wallet_name);
        final TextView listWalletBalance = v.findViewById(R.id.list_wallet_balance);

        if (wallet != null) {
            if (listWalletName != null) {
                listWalletName.setText(wallet.getName());
            }
        }

        Query<Balance> query = MainActivity.balanceBox.query()
                .equal(Balance_.walletToken, wallet.getAddress() + "-" + currentChannel).build();

        Balance balanceObj = query.findUnique();

        if(balanceObj != null) {
            balanceMap.put(wallet.getAddress() + "-" + currentChannel, balanceObj);
            if (listWalletBalance != null) {
                listWalletBalance.setText(String.format("%s ETH", String.valueOf(balanceObj.getBalance())));
            }
        }

        v.setOnClickListener(view -> {
            builder = new MaterialDialog.Builder(getContext());
            Log.i("Hi", "form wallet id: " + wallet.getId());
            builder
                    .title(wallet.getName())
                    .customView(R.layout.wallet_dialog, true)
                    .negativeText("Dismiss")
                    .positiveText("Save")
                    .onPositive((dialog, which) -> {
                        String newWalletName = walletNameInput.getText().toString();

                        if(!newWalletName.equals(wallet.getName()) && newWalletName.length() > 3) {
                            wallet.setName(newWalletName);
                            MainActivity.walletBox.put(wallet);
                            listWalletName.setText(wallet.getName());
                            Snackbar.make(view, "updated with id " + wallet.getId(), Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .onNegative((dialog, which) -> {
                        // TODO
                    })
                    .onAny((dialog, which) -> {
                    });

            View dialogView  = builder.build().getCustomView();

            walletAddressView = dialogView.findViewById(R.id.wallet_address_view);
            walletBalanceView = dialogView.findViewById(R.id.wallet_balance_view);
            walletNameView = dialogView.findViewById(R.id.wallet_name_view);
            walletNameInput = dialogView.findViewById(R.id.wallet_name_input);

            walletAddressView.setText("Address : " + wallet.getAddress());
            walletNameView.setText(wallet.getName());
            walletNameInput.setText(wallet.getName());

            walletNameView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    walletNameView.setVisibility(View.GONE);
                    walletNameInput.setVisibility(View.VISIBLE);
                }
            });

            builder.show();

        });

        String URL = null;

        switch (currentChannel) {
            case MAINNET_CHANNEL:
                URL = MAINNET_URL;
                break;
            case ROPSTEN_CHANNEL:
                URL = ROPSTEN_URL;
                break;
            case RINKEBY_CHANNEL:
                URL = RINKEBY_URL;
                break;
        }


        StringRequest request = new StringRequest(Request.Method.POST, URL, response -> {
            Log.i("WALLET", wallet.getAddress());

            try {
                JSONObject obj = new JSONObject(response);
                String result = obj.getString("result");
                Long wei = Long.parseLong(result.substring(2), 16);
                Double balance = Double.valueOf(wei) / Double.valueOf(WEI2ETH);

                if (wallet != null) {
                    if (listWalletBalance != null) {
                        listWalletBalance.setText(balance + " ETH");
                    }
                }

                Balance newBalance = balanceMap.get(wallet.getAddress() + "-" + currentChannel);

                if(newBalance != null) {
                    newBalance.setBalance(balance);
                } else {
                    newBalance = new Balance();
                    newBalance.setWalletToken(wallet.getAddress() + "-" + currentChannel);
                    newBalance.setBalance(balance);
                }

                MainActivity.balanceBox.put(newBalance);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Log.i("BALANCE", error.toString())) {
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
                    obj.put("jsonrpc","2.0");
                    obj.put("method","eth_getBalance");
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

        return v;
    }

}