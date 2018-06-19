package wallet;

import android.content.ClipData;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.crimson.crimson.R;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import org.w3c.dom.Text;

import java.util.List;

import utils.SampleSQLiteDBHelper;

/**
 * Created by crimson on 01/06/2018.
 */

public class WalletLIstAdapter extends ArrayAdapter<CrimsonWallet> {
    private MaterialDialog.Builder builder;
    private MaterialDialog dialog;
    private TextView walletNameView;
    private TextView walletAddressView;
    private TextView walletBalanceView;
    private TextInputEditText walletNameInput;
    private SampleSQLiteDBHelper helper;


//    public WalletLIstAdapter(Context context, int textViewResourceId) {
//        super(context, textViewResourceId);
//    }

    public WalletLIstAdapter(Context context, int resource, List<CrimsonWallet> items) {
        super(context, resource, items);
        helper = new SampleSQLiteDBHelper(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.single_wallet, null);
        }

        final CrimsonWallet wallet = getItem(position);

        if (wallet != null) {
            TextView tt1 = v.findViewById(R.id.list_item_type1_text_view);


            if (tt1 != null) {
                tt1.setText(wallet.getName());
            }
        }

        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                builder = new MaterialDialog.Builder(getContext());
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
                                    long result = helper.updateWalletDB(wallet);
                                    Snackbar.make(view, "updated with id " + result, Snackbar.LENGTH_LONG).show();
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
                walletBalanceView = dialogView.findViewById(R.id.wallet_balance_view);
                walletNameView = dialogView.findViewById(R.id.wallet_name_view);
                walletNameInput = dialogView.findViewById(R.id.wallet_name_input);

                walletAddressView.setText("Address : " + wallet.getAddress());
                walletBalanceView.setText("Balance : " + wallet.getBalace());
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

        return v;
    }

}