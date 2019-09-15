package com.namadi.crimson.fragments;


import android.app.Activity;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.github.orangegangsters.lollipin.lib.managers.AppLock;
import com.namadi.crimson.activities.AboutActivity;
import com.namadi.crimson.activities.MainActivity;
import com.namadi.crimson.activities.ScanToWatchActivity;
import com.namadi.crimson.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.objectbox.query.QueryBuilder;

import com.namadi.crimson.activities.pin.CustomPinActivity;
import com.namadi.crimson.models.Token;
import com.namadi.crimson.models.Token_;
import com.namadi.crimson.models.Wallet;

import static com.namadi.crimson.utils.Constants.CURRENT_CHANNEL;
import static com.namadi.crimson.utils.Constants.MAINNET_CHANNEL;
import static com.namadi.crimson.utils.Constants.MAINNET_TOKEN_BALANCE;
import static com.namadi.crimson.utils.Constants.REQUEST_CODE_ENABLE;
import static com.namadi.crimson.utils.Constants.RINKEBY_CHANNEL;
import static com.namadi.crimson.utils.Constants.RINKEBY_TOKEN_BALANCE;
import static com.namadi.crimson.utils.Constants.ROPSTEN_CHANNEL;
import static com.namadi.crimson.utils.Constants.ROPSTEN_TOKEN_BALANCE;
import static com.namadi.crimson.utils.Constants.SCANNED_TO_WATCH_ADDRESS;


/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends Fragment {
    private Button addCurrency;
    private Button watchTokenBtn;
    private MaterialDialog.Builder loadTokenProgress;
    private MaterialDialog.Builder builder;
    private MaterialDialog addTokenDialog;

    private MaterialDialog loadTokenDialog;
    private TextInputEditText tokenAddressInput;
    private Button watchTokenProceedBtn, addTokenBtn;
    private RadioGroup selectChannelBtn;
    private LinearLayout tokenForm, tokenPreview;
    private TextView tokenPreviewName, tokenPreviewSymbol, tokenPreviewDec, tokenErrorMsg;
    private String currentChannel;

    View addTokenDialogView, myFragmentView;
    List<Token> tokens;
    Token token;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;


    public static SettingsFragment newInstance() {
        SettingsFragment fragment = new SettingsFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(ScanToWatchActivity.scannedValue != null) {
            if(tokenAddressInput != null) {
                tokenAddressInput.setText(ScanToWatchActivity.scannedValue);
                ScanToWatchActivity.scannedValue = null;
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        myFragmentView = inflater.inflate(R.layout.fragment_settings, container, false);

        selectChannelBtn = myFragmentView.findViewById(R.id.select_channel);
        sharedPref = MainActivity.sharedPref;
        editor = sharedPref.edit();

        tokens = MainActivity.tokenBox.getAll();

        currentChannel = MainActivity.currentChannel;
        RadioButton radioButton;

        switch (currentChannel) {
            case ROPSTEN_CHANNEL:
                radioButton = myFragmentView.findViewById(R.id.ropsten_option);
                break;
            case RINKEBY_CHANNEL:
                radioButton = myFragmentView.findViewById(R.id.rinkeby_option);
                break;
            default:
                radioButton = myFragmentView.findViewById(R.id.mainnet_option);
                break;
        }

        radioButton.setChecked(true);

        loadTokens();


        myFragmentView.findViewById(R.id.change_pin).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), CustomPinActivity.class);
            intent.putExtra(AppLock.EXTRA_TYPE, AppLock.ENABLE_PINLOCK);
            startActivityForResult(intent, REQUEST_CODE_ENABLE);
        });

        selectChannelBtn.setOnCheckedChangeListener((radioGroup, checkedId) -> {
            Log.d("CHECKED", "id: " + checkedId);

            String selectedChannel = MAINNET_CHANNEL;
            if (checkedId == R.id.ropsten_option) {
                MainActivity.spinner.setSelection(2);
                selectedChannel = ROPSTEN_CHANNEL;
            } else if (checkedId == R.id.rinkeby_option) {
                MainActivity.spinner.setSelection(1);
                selectedChannel = RINKEBY_CHANNEL;
            } else {
                MainActivity.spinner.setSelection(0);
            }

            editor.putString(CURRENT_CHANNEL, selectedChannel);
            editor.commit();
        });

        String scannedValue = sharedPref.getString(SCANNED_TO_WATCH_ADDRESS, null);
        if(scannedValue != null && scannedValue.length() > 5) {
            if(tokenAddressInput != null) {
                tokenAddressInput.setText(scannedValue);
                editor.putString(SCANNED_TO_WATCH_ADDRESS, null);
                editor.commit();
            }
        }

        addCurrency = myFragmentView.findViewById(R.id.add_more_currency);
        addCurrency.setOnClickListener(view -> (new MaterialDialog.Builder(getActivity()))
                .title("Add More Currency")
                .items(R.array.currencies)
                .itemsCallbackMultiChoice(null, (dialog, which, text) -> {
                    /**
                     * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                     * returning false here won't allow the newly selected check box to actually be selected
                     * (or the newly unselected check box to be unchecked).
                     * See the limited multi choice dialog example in the sample project for details.
                     **/
                    return true;
                })
                .negativeText("Dismiss")
                .positiveText("Add")
                .onPositive((dialog, which) -> {
                    Snackbar.make(getView(), "Feature not available", Snackbar.LENGTH_LONG).show();
                })
                .onNegative((dialog, which) -> {
                    // TODO
                })
                .show());

        myFragmentView.findViewById(R.id.about_label).setOnClickListener(view -> {
            Intent intent = new Intent(getActivity(), AboutActivity.class);
            getActivity().startActivity(intent);
        });

        watchTokenBtn = myFragmentView.findViewById(R.id.add_token);
        watchTokenBtn.setOnClickListener(view -> {
            if(MainActivity.tokenBox.getAll().size() > 4) {
                Snackbar.make(getView(), "Can't add more than 5 tokens", Snackbar.LENGTH_LONG).show();
                return;
            }
            builder = new MaterialDialog.Builder(getActivity());
            builder
                    .title("Add Token")
                    .customView(R.layout.watch_token_dialog, true)
                    .negativeText("Dismiss")
                    .positiveText("Save")
                    .onPositive((dialog, which) -> {
                        watchToken();
                    })
                    .onNegative((dialog, which) -> {
                        // TODO
                    })
                    .onAny((dialog, which) -> {
                    });

            addTokenDialogView  = builder.build().getCustomView();

            watchTokenProceedBtn = addTokenDialogView.findViewById(R.id.load_token_info);
            addTokenBtn = addTokenDialogView.findViewById(R.id.save_token_btn);

            tokenForm = addTokenDialogView.findViewById(R.id.add_token_view);
            tokenPreview = addTokenDialogView.findViewById(R.id.token_preview);

            tokenErrorMsg = addTokenDialogView.findViewById(R.id.add_token_error);
            tokenPreviewName = addTokenDialogView.findViewById(R.id.token_preview_name);
            tokenPreviewSymbol = addTokenDialogView.findViewById(R.id.token_preview_symbol);
            tokenPreviewDec = addTokenDialogView.findViewById(R.id.preview_token_dec);

            tokenAddressInput = addTokenDialogView.findViewById(R.id.token_address_input);

            tokenAddressInput.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    tokenErrorMsg.setVisibility(View.GONE);
                    if(charSequence.toString().length() > 20) {
                        watchTokenProceedBtn.setVisibility(View.VISIBLE);
                    } else {
                        watchTokenProceedBtn.setVisibility(View.GONE);
                    }
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });

            watchTokenProceedBtn.setOnClickListener(view1 -> {
                Log.i("INPUT", tokenAddressInput.getText().toString());

                // check valid token address
                if(true) {
                    watchToken();
                }
                watchTokenProceedBtn.setVisibility(View.GONE);
            });

            addTokenBtn.setOnClickListener(view1 -> {
                MainActivity.tokenBox.put(token);
                loadTokens();
                addTokenDialog.dismiss();
            });


            Button button = addTokenDialogView.findViewById(R.id.scan_to_add);
            button.setOnClickListener((View v) -> {
                Intent intent = new Intent(getActivity(), ScanToWatchActivity.class);
                startActivity(intent);
            });

            addTokenDialog = builder.build();
            addTokenDialog.show();

        });

        return myFragmentView;
    }

    private void watchToken() {

        String tokenAddress = tokenAddressInput.getText().toString();
        if(MainActivity.walletBox.count() == 0) {
            Snackbar.make(getView(), "Must have Wallet to add token", Snackbar.LENGTH_LONG).show();
            return;
        }

        QueryBuilder<Token> builder = MainActivity.tokenBox.query();
        builder.equal(Token_.address, tokenAddress);
        if(builder.build().findUnique() != null) {
            Snackbar.make(getView(), "Token is already added", Snackbar.LENGTH_LONG).show();
            return;
        }

        loadTokenProgress = new MaterialDialog.Builder(getActivity());
        loadTokenProgress
                .content(R.string.adding_token_loading)
                .progress(true, 0);

        loadTokenDialog = loadTokenProgress.build();
        loadTokenDialog.show();

        Wallet firstWallet = MainActivity.walletBox.getAll().get(0);

        String URL = null;

        switch (currentChannel) {
            case MAINNET_CHANNEL:
                URL = MAINNET_TOKEN_BALANCE;
                break;
            case ROPSTEN_CHANNEL:
                URL = ROPSTEN_TOKEN_BALANCE;
                break;
            case RINKEBY_CHANNEL:
                URL = RINKEBY_TOKEN_BALANCE;
                break;
        }

        URL = String.format(URL, tokenAddress, firstWallet.getAddress());

        StringRequest postRequest = new StringRequest(Request.Method.GET, URL,
                response -> {
                    try {
                        JSONObject parentObject = new JSONObject(response);
                        String name = parentObject.getString("name");
                        String symbol = parentObject.getString("symbol");
                        Integer decimals = parentObject.getInt("decimals");

                        token = new Token();
                        token.setAddress(tokenAddress);
                        token.setDecimal(decimals);
                        token.setName(name);
                        token.setSymbol(symbol);
                        token.setChannel(currentChannel);

                        tokenPreviewName.setText(name);
                        tokenPreviewSymbol.setText(symbol);
                        tokenPreviewDec.setText(String.valueOf(decimals));

                        tokenForm.setVisibility(View.GONE);
                        tokenPreview.setVisibility(View.VISIBLE);


                        MainActivity.tokenBox.put(token);
                        Snackbar.make(getView(), name + " Token added", Snackbar.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(getView(), "Cannot add token", Snackbar.LENGTH_LONG).show();
                        Log.i("TOKEN", e.getMessage());
                        tokenErrorMsg.setVisibility(View.VISIBLE);
                    }
                    loadTokenDialog.dismiss();
                },
                error -> {
                    Snackbar.make(getView(), "Cannot add token", Snackbar.LENGTH_LONG).show();
                    loadTokenDialog.dismiss();
                    Log.i("TOKEN", error.toString());
                    tokenErrorMsg.setVisibility(View.VISIBLE);
                }
        );
        MainActivity.queue.add(postRequest);
    }

    private void loadTokens() {
        QueryBuilder<Token> builder = MainActivity.tokenBox.query();
        builder.equal(Token_.channel, currentChannel);

        tokens = builder.build().find();

        if(tokens.size() > 0) {
            RelativeLayout token1 = myFragmentView.findViewById(R.id.token1);
            TextView token1Name = myFragmentView.findViewById(R.id.token1_name);
            TextView token1Balance = myFragmentView.findViewById(R.id.token1_balance);

            Token tmpToken = tokens.get(0);
            token1.setVisibility(View.VISIBLE);

            token1Name.setText(tmpToken.getName());
            token1Balance.setText(tmpToken.getSymbol());
        }

        if(tokens.size() > 1) {
            RelativeLayout token2 = myFragmentView.findViewById(R.id.token2);
            TextView token2Name = myFragmentView.findViewById(R.id.token2_name);
            TextView token2Balance = myFragmentView.findViewById(R.id.token2_balance);

            Token tmpToken = tokens.get(1);
            token2.setVisibility(View.VISIBLE);

            token2Name.setText(tmpToken.getName());
            token2Balance.setText(tmpToken.getSymbol());
        }

        if(tokens.size() > 2) {
            RelativeLayout token3 = myFragmentView.findViewById(R.id.token3);
            TextView token3Name = myFragmentView.findViewById(R.id.token3_name);
            TextView token3Balance = myFragmentView.findViewById(R.id.token3_balance);

            Token tmpToken = tokens.get(2);
            token3.setVisibility(View.VISIBLE);

            token3Name.setText(tmpToken.getName());
            token3Balance.setText(tmpToken.getSymbol());
        }
    }
}
