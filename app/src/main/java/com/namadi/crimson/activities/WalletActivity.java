package com.namadi.crimson.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableRow;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.namadi.crimson.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.stream.Stream;

import com.namadi.crimson.models.Token;
import com.namadi.crimson.models.Tx;
import com.namadi.crimson.models.Tx_;
import com.namadi.crimson.models.Wallet;

import static com.namadi.crimson.activities.MainActivity.sharedPref;
import static com.namadi.crimson.utils.Constants.CURRENT_CHANNEL;
import static com.namadi.crimson.utils.Constants.MAINNET_CHANNEL;
import static com.namadi.crimson.utils.Constants.MAINNET_TOKEN_BALANCE;
import static com.namadi.crimson.utils.Constants.MAINNET_URL;
import static com.namadi.crimson.utils.Constants.RINKEBY_CHANNEL;
import static com.namadi.crimson.utils.Constants.RINKEBY_TOKEN_BALANCE;
import static com.namadi.crimson.utils.Constants.RINKEBY_URL;
import static com.namadi.crimson.utils.Constants.ROPSTEN_CHANNEL;
import static com.namadi.crimson.utils.Constants.ROPSTEN_REQUEST_ETH;
import static com.namadi.crimson.utils.Constants.ROPSTEN_TOKEN_BALANCE;
import static com.namadi.crimson.utils.Constants.ROPSTEN_URL;
import static com.namadi.crimson.utils.Constants.WEI2ETH;

public class WalletActivity extends AppCompatActivity {

    private MaterialDialog.Builder builder, qrCodeBuilder, reqBuilder;
    private MaterialDialog reqDialog, qrDialog;

    private MaterialDialog changeNameDialog, receiveDialog;
    private TextView walletName, walletAddress;
    private Button changeName, send, share, requestEth;
    private Wallet wallet;
    String currentChannel = MainActivity.currentChannel;
    View contextView, changeWalletName, shareDialog;
    private TextInputEditText walletNameInput;

    public final static int QRcodeWidth = 500 ;
    private static final String IMAGE_DIRECTORY = "/QRcodeDemonuts";
    Bitmap bitmap ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);

        MenuItem item = menu.findItem(R.id.spinner);
        Spinner spinner = (Spinner) MenuItemCompat.getActionView(item);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.networks, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
        spinner.post(() -> spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedChannel = MAINNET_CHANNEL;
                if (i == 1) {
                    selectedChannel = RINKEBY_CHANNEL;
                } else if (i == 2) {
                    selectedChannel = ROPSTEN_CHANNEL;
                }

                if(currentChannel == selectedChannel) {
                    return;
                }

                currentChannel = selectedChannel;
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString(CURRENT_CHANNEL, selectedChannel);
                editor.commit();

                Snackbar.make(findViewById(R.id.single_wallet_layout), "network channel changed", Snackbar.LENGTH_SHORT).show();
                updateBalance();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        }));

        if(currentChannel == RINKEBY_CHANNEL) {spinner.setSelection(1); }
        else if (currentChannel == ROPSTEN_CHANNEL) { spinner.setSelection(2); }
        else { spinner.setSelection(0); }

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);
        Intent intent = getIntent();
        Long walletId = intent.getExtras().getLong("WalletID");
        wallet = MainActivity.walletBox.get(walletId);
        String address = wallet.getAddress();

        walletName = findViewById(R.id.wallet_name);
        walletAddress = findViewById(R.id.wallet_address);

        changeName = findViewById(R.id.change_name_btn);
        send = findViewById(R.id.wallet_send_btn);
        share = findViewById(R.id.wallet_receive_btn);
        requestEth = findViewById(R.id.request_eth);
        contextView = findViewById(R.id.single_wallet_layout);

        walletName.setText(wallet.getName());
        walletAddress.setText(address.substring(0, 10) + "..." + address.substring(address.length()-10));

        displayBalanceUi();
        updateBalance();

        requestEth.setOnClickListener(view -> {
            if(currentChannel != ROPSTEN_CHANNEL) {
                Snackbar.make(contextView, "Switch to Ropsten net first", Snackbar.LENGTH_LONG).show();
            } else {
                requestFreeEth(wallet);
            }
        });

        changeName.setOnClickListener(view -> {
            builder = new MaterialDialog.Builder(this);
            builder
                    .title("Change Wallet Name")
                    .customView(R.layout.change_wallet_name_dialog, true)
                    .negativeText("Dismiss")
                    .positiveText("Save")
                    .onPositive((dialog, which) -> {
                        String input = walletNameInput.getText().toString();
                        if(input != null && input.length() > 5) {
                            wallet.setName(input);
                            MainActivity.walletBox.put(wallet);
                            walletName.setText(wallet.getName());
                        } else {
                            Snackbar.make(contextView, "Please provide a valid com.namadi.crimson.wallet name", Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .onNegative((dialog, which) -> {
                        changeNameDialog.dismiss();
                    })
                    .onAny((dialog, which) -> {
                    });

            changeWalletName  = builder.build().getCustomView();
            walletNameInput = changeWalletName.findViewById(R.id.wallet_name_input);
            walletNameInput.setText(wallet.getName());


            changeNameDialog = builder.build();
            changeNameDialog.show();

        });

        send.setOnClickListener(view -> {
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("NAVIGATE", "SEND");
            startActivity(i);
            finish();
        });

        share.setOnClickListener(view -> {
            new GenerateGR().execute();
        });


        TextView txLabel = findViewById(R.id.transactions_list_label);

        ArrayList<String> tx_list = new ArrayList<>();
        ArrayList<Tx> txs = new ArrayList<>();

        for(Tx tx : MainActivity.txBox.query().equal(Tx_.fromWallet, wallet.getAddress()).build().find()) {
            txs.add(tx);
            String wallet = tx.getToWallet().substring(0,5) + "..." + tx.getToWallet().substring(tx.getToWallet().length() - 5);
            if(tx.getStatus().equals("FAILED")) {
                tx_list.add("To: " + wallet + "\tValue: " + tx.getValue() + "\tStatus: " + tx.getStatus());
            } else {
                tx_list.add("To: " + wallet + "\tValue: " + tx.getValue() + "\tView Transaction");
            }
        }

        ArrayAdapter<String> lIstAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, tx_list);

        if(tx_list.size() > 0) {
            txLabel.setText("transactions");
        } else {
            txLabel.setText("no transactions");
        }

        ListView txListAdapter = findViewById(R.id.tx_list);
        txListAdapter.setAdapter(lIstAdapter);
        txListAdapter.setOnItemClickListener((adapterView, view, i, l) -> {
            String item = tx_list.get(i);
            Tx tx = txs.get(i);
            if(item.contains("View Transaction")) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ropsten.etherscan.io/tx/" + tx.getTxHash()));
                startActivity(browserIntent);

            }
        });
    }

    private void displayBalanceUi() {
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

                TableRow currentRow = findViewById(getId("balance_1"));
                if(currentRow == null) { return; }
                currentRow.setVisibility(View.VISIBLE);

                TextView amount = findViewById(getId("balance_1_amount"));
                amount.setText(balance.toString());

                TextView tv = findViewById(getId("balance_1_symbol"));
                tv.setText("ETH");
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

    private void updateBalance() {
        int count = 2;
        for(Token token : MainActivity.tokenBox.getAll()) {
            if(token.getChannel() == null || !token.getChannel().equals(currentChannel)) {
                continue;
            }
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

            Log.d("TOKEN-BALANCE", "TOKEN: " + token.getSymbol());
            Log.d("TOKEN-BALANCE", "URL: " + URL);
            Log.d("TOKEN-BALANCE", "CHANNEL: " + currentChannel);

            URL = String.format(URL, token.getAddress(), wallet.getAddress());
            int finalCount = count;
            StringRequest postRequest = new StringRequest(Request.Method.GET, URL,
                    response -> {
                        try {
                            JSONObject parentObject = new JSONObject(response);
                            String name = parentObject.getString("name");
                            String symbol = parentObject.getString("symbol");
                            Double balance = parentObject.getDouble("balance");

                            TableRow currentRow = findViewById(getId("balance_" + finalCount));
                            if(currentRow == null) { return; }
                            currentRow.setVisibility(View.VISIBLE);

                            TextView amount = findViewById(getId("balance_" + finalCount + "_amount"));
                            amount.setText(balance.toString());

                            TextView tv = findViewById(getId("balance_" + finalCount + "_symbol"));
                            tv.setText(symbol);

                            Log.d("TOKEN-BALANCE", name + "---" + amount);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Snackbar.make(contextView, "Cannot retrieve wallet balance", Snackbar.LENGTH_LONG).show();
                            Log.d("TOKEN-BALANCE", e.getMessage());
                        }
                    },
                    error -> {
                        Snackbar.make(contextView, "Cannot retrieve wallet balance", Snackbar.LENGTH_LONG).show();
                        Log.d("TOKEN-BALANCE", error.toString());
                    }
            );
            MainActivity.queue.add(postRequest);
            count++;
        }
    }

    private void requestFreeEth(Wallet wallet) {
        reqBuilder = new MaterialDialog.Builder(this);
        reqBuilder
                .content(R.string.requesting_ether)
                .progress(true, 0);

        reqDialog = reqBuilder.build();
        reqDialog.show();

        String URL = String.format(ROPSTEN_REQUEST_ETH, wallet.getAddress());
        StringRequest postRequest = new StringRequest(Request.Method.GET, URL,
                response -> {
                    try {
                        JSONObject parentObject = new JSONObject(response);
                        Log.d("REQUEST-TOKEN", parentObject.toString());
                        String address = parentObject.getString("address");
                        String txHash = parentObject.getString("txHash");
                        Integer amount = parentObject.getInt("amount");
                        reqDialog.dismiss();
                        Snackbar.make(contextView, "Request Sent", Snackbar.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        reqDialog.dismiss();
                        Snackbar.make(contextView, "Request Sent", Snackbar.LENGTH_LONG).show();
                        Log.d("REQUEST-TOKEN", "JSONException: " + e.getMessage());
                    }
                },
                error -> {
                    reqDialog.dismiss();
                    Snackbar.make(contextView, "Error Sending Request, Try again later", Snackbar.LENGTH_LONG).show();
                    Log.d("REQUEST-TOKEN", "Error: " +  error.toString());
                }
        );
        MainActivity.queue.add(postRequest);
    }

    private int getId(String s) {
        return getResources().getIdentifier(s, "id", this.getPackageName());

    }

    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.DATA_MATRIX.QR_CODE,
                    QRcodeWidth, QRcodeWidth, null
            );

        } catch (IllegalArgumentException Illegalargumentexception) {

            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();

        int bitMatrixHeight = bitMatrix.getHeight();

        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;

            for (int x = 0; x < bitMatrixWidth; x++) {

                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.black):getResources().getColor(R.color.white);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);

        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    public String saveImage(Bitmap myBitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        myBitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
        File wallpaperDirectory = new File(
                Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY);
        // have the object build the directory structure, if needed.

        if (!wallpaperDirectory.exists()) {
            Log.d("DIRR", "" + wallpaperDirectory.mkdirs());
            wallpaperDirectory.mkdirs();
        }

        try {
            File f = new File(wallpaperDirectory, Calendar.getInstance()
                    .getTimeInMillis() + ".jpg");
            f.createNewFile();   //give read write permission
            FileOutputStream fo = new FileOutputStream(f);
            fo.write(bytes.toByteArray());
            MediaScannerConnection.scanFile(this,
                    new String[]{f.getPath()},
                    new String[]{"image/jpeg"}, null);
            fo.close();
            Log.d("DIRR", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            Log.d("DIRR", "exception: "+e1.getMessage());
            e1.printStackTrace();
        }
        return "";

    }

    private String saveBitmap(Bitmap bitmap){
        String path = Environment.getExternalStorageDirectory() + IMAGE_DIRECTORY;
        if(bitmap!=null){
            try {
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(path);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return path;
    }

    private class GenerateGR extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                bitmap = TextToImageEncode(wallet.getAddress());
            } catch (WriterException e) {
                e.printStackTrace();
            }
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {
            builder = new MaterialDialog.Builder(WalletActivity.this);
            builder
                    .customView(R.layout.share_dialog, true)
                    .negativeText("Close")
                    .positiveText("Export QR Code")
                    .onPositive((dialog, which) -> {
                        String path = saveBitmap(bitmap);  //give read write permission
                        Snackbar.make(contextView, "qr code saved at " + path, Snackbar.LENGTH_LONG).show();
                    })
                    .onNegative((dialog, which) -> {
                        receiveDialog.dismiss();
                    })
                    .onAny((dialog, which) -> {
                    });

            shareDialog  = builder.build().getCustomView();
            TextView walletName = shareDialog.findViewById(R.id.wallet_name_view);
            TextView walletAddressView = shareDialog.findViewById(R.id.wallet_address_view);
            ImageView qrImageView = shareDialog.findViewById(R.id.iv);

            walletName.setText(wallet.getName());
            walletAddressView.setText(wallet.getAddress());
            qrImageView.setImageBitmap(bitmap);

            walletAddressView.setOnClickListener(v -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("address", wallet.getAddress());
                clipboard.setPrimaryClip(clip);
                Snackbar.make(contextView, "address copied", Snackbar.LENGTH_LONG).show();
            });

            receiveDialog = builder.build();
            qrDialog.dismiss();
            receiveDialog.show();
        }

        @Override
        protected void onPreExecute() {
            qrCodeBuilder = new MaterialDialog.Builder(WalletActivity.this);
            qrCodeBuilder
                    .content(R.string.generating_qr_code)
                    .progress(true, 0);
            qrDialog = qrCodeBuilder.build();
            qrDialog.show();
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

}
