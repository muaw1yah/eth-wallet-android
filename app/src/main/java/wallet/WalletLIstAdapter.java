package wallet;

import android.annotation.SuppressLint;
import android.content.Context;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.Wallet;

/**
 * Created by crimson on 01/06/2018.
 */

public class WalletLIstAdapter extends ArrayAdapter<Wallet> {
    private MaterialDialog.Builder builder;
    private TextView walletNameView;
    private TextView walletAddressView;
    private TextInputEditText walletNameInput;

    public WalletLIstAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public WalletLIstAdapter(Context context, int resource, List<Wallet> items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.single_wallet, null);
        }

        final Wallet wallet = getItem(position);
        final TextView tt1 = v.findViewById(R.id.list_item_type1_text_view);

        if (wallet != null) {
            if (tt1 != null) {
                tt1.setText(wallet.getName());
            }
        }

        v.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(final View view) {
                builder = new MaterialDialog.Builder(getContext());
                Log.i("Hi", "form wallet id: " + wallet.getId());
                builder
                        .title(wallet.getName())
                        .customView(R.layout.wallet_dialog, true)
                        .negativeText("Dismiss")
                        .positiveText("Save")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                String newWalletName = walletNameInput.getText().toString();

                                if(!newWalletName.equals(wallet.getName()) && newWalletName.length() > 3) {
                                    wallet.setName(newWalletName);
                                    MainActivity.walletBox.put(wallet);
                                    tt1.setText(wallet.getName());
                                    Snackbar.make(view, "updated with id " + wallet.getId(), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                                // TODO
                            }
                        })
                        .onAny(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog dialog, DialogAction which) {
                            }
                        });

                View dialogView  = builder.build().getCustomView();

                walletAddressView = dialogView.findViewById(R.id.wallet_address_view);
                //walletBalanceView = dialogView.findViewById(R.id.wallet_balance_view);
                walletNameView = dialogView.findViewById(R.id.wallet_name_view);
                walletNameInput = dialogView.findViewById(R.id.wallet_name_input);

                walletAddressView.setText("Address : " + wallet.getAddress());
                Log.i("WALLET","Wallet Address " + wallet.getAddress());
                //walletBalanceView.setText("Balance : " + wallet.getBalace());
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

            }
        });

        String url = "https://mainnet.infura.io/itRXy2X4KtzI7YZTq9wL";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.i("BALANCE", response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("BALANCE", error.toString());
            }
        }) {
            @Override
            public byte[] getBody() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("jsonrpc","2.0");
                params.put("method","eth_getBalance");
                params.put("params", "[\"" + wallet.getAddress() + "\",\"latest\"]");
                params.put("id", "1");

                return new JSONObject(params).toString().getBytes();
            }

            @Override
            public String getBodyContentType() {
                return "application/json";
            }
        };

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        try {
                            JSONObject parentObject = new JSONObject(response);
                            Log.i("RESPONSE", parentObject.toString());
                            String balance = parentObject.getString("result");
                            Log.i("BALANCE", balance);

//                            String name = "Wallet " + (wallet_list.size() + 1);
//                            Wallet wallet = new Wallet();
//                            wallet.setName(name);
//                            wallet.setAddress(address);
//                            wallet.setKey(key);
//                            wallet = MainActivity.walletBox.get(MainActivity.walletBox.put(wallet));
//
//                            Log.i("ID", "Wallet Id: " + wallet.getId());
//                            if(mTextMessage.getVisibility() == View.VISIBLE) {
//                                mTextMessage.setVisibility(View.GONE);
//                            }
//                            wallet_list.add(wallet);
//                            walletAdapter.notifyDataSetChanged();
//                            Snackbar.make(getView(), "New wallet has been added ", Snackbar.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.i("ERROR", e.getMessage());
//                            Snackbar.make(getView(), "Cannot add wallet", Snackbar.LENGTH_LONG).show();
                        }
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i("ERROR", error.toString());
                        // error
//                        Snackbar.make(getView(), "Cannot add wallet", Snackbar.LENGTH_LONG).show();
                    }
                }
        )  {
            @Override
            protected Map<String,String> getParams(){
                Map<String,String> params = new HashMap<String, String>();
                params.put("jsonrpc","2.0");
                params.put("method","eth_getBalance");
                params.put("params", "[\"" + wallet.getAddress() + "\",\"latest\"]");
                params.put("id", "1");
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<String, String>();
                params.put("Content-Type","application/json");
                return params;
            }
        };

//        MainActivity.queue.add(postRequest);
        MainActivity.queue.add(request);

        return v;
    }

}