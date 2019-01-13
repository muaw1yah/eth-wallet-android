package com.namadi.crimson.wallet;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.namadi.crimson.activities.MainActivity;
import com.namadi.crimson.R;
import com.namadi.crimson.activities.WalletActivity;

import java.util.HashMap;
import java.util.List;

import io.objectbox.query.Query;
import com.namadi.crimson.models.Balance;
import com.namadi.crimson.models.Balance_;
import com.namadi.crimson.models.Wallet;

import static com.namadi.crimson.utils.Constants.CURRENT_CHANNEL;

/**
 * Created by crimson on 01/06/2018.
 */

public class WalletLIstAdapter extends ArrayAdapter<Wallet> {
    private MaterialDialog.Builder builder, reqBuilder;
    private MaterialDialog dialog, reqDialog;

    private TextView walletNameView;
    private TextView walletAddressView;
    private TextView walletBalanceView;
    private Button walletReqEthBtn;
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
            Intent intent = new Intent(getContext(), WalletActivity.class);
            intent.putExtra("WalletID", wallet.getId());
            getContext().startActivity(intent);
        });

        return v;
    }



}