package com.namadi.crimson.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import com.namadi.crimson.models.Balance;
import com.namadi.crimson.models.Token;
import com.namadi.crimson.models.Wallet;

import static com.namadi.crimson.utils.Constants.CURRENT_CHANNEL;
import static com.namadi.crimson.utils.Constants.MAINNET_CHANNEL;
import static com.namadi.crimson.utils.Constants.MAINNET_TOKEN_BALANCE;
import static com.namadi.crimson.utils.Constants.RINKEBY_CHANNEL;
import static com.namadi.crimson.utils.Constants.RINKEBY_TOKEN_BALANCE;
import static com.namadi.crimson.utils.Constants.ROPSTEN_CHANNEL;
import static com.namadi.crimson.utils.Constants.ROPSTEN_REQUEST_ETH;
import static com.namadi.crimson.utils.Constants.ROPSTEN_TOKEN_BALANCE;

public class WalletActivity extends AppCompatActivity {

    private MaterialDialog.Builder builder, reqBuilder;
    private MaterialDialog reqDialog;
    private MaterialDialog changeNameDialog, receiveDialog;
    private TextView walletName, walletAddress;
    private Button changeName, send, share, requestEth;
    private Wallet wallet;
    String currentChannel = MainActivity.sharedPref.getString(CURRENT_CHANNEL, null);
    View contextView, changeWalletName, shareDialog;
    private TextInputEditText walletNameInput;

    public final static int QRcodeWidth = 500 ;
    private static final String IMAGE_DIRECTORY = "/QRcodeDemonuts";
    Bitmap bitmap ;
    private EditText etqr;
    private ImageView iv;
    private Button btn;


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
            requestFreeEth(wallet);
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
            try {
                bitmap = TextToImageEncode(address);
                builder = new MaterialDialog.Builder(this);
                builder
                        .customView(R.layout.share_dialog, true)
                        .negativeText("Close")
                        .positiveText("Export QR Code")
                        .onPositive((dialog, which) -> {
                            String path = saveImage(bitmap);  //give read write permission
                            Snackbar.make(contextView, "qr code saved " + path, Snackbar.LENGTH_LONG).show();
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
                receiveDialog.show();

            } catch (WriterException e) {
                e.printStackTrace();
            }
        });
    }

    private void displayBalanceUi() {
        int count = 1;
        for(Balance b : MainActivity.balanceBox.getAll()) {
            if(b.getWalletToken().contains(wallet.getAddress())) {
                TableRow currentRow = findViewById(getId("balance_" + count));
                if(currentRow == null) { continue; }
                currentRow.setVisibility(View.VISIBLE);


                TextView amount = findViewById(getId("balance_" + count + "_amount"));
                amount.setText(b.getBalance().toString());

                TextView token = findViewById(getId("balance_" + count + "_symbol"));
                token.setText(b.getToken());

                Log.i("BALANCE", b.getBalance() + " " + b.getToken());
            }
            count++;
        }
    }

    private void updateBalance() {
        for(Token token : MainActivity.tokenBox.getAll()) {
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

            URL = String.format(URL, token.getAddress(), wallet.getAddress());

            StringRequest postRequest = new StringRequest(Request.Method.GET, URL,
                    response -> {
                        try {
                            JSONObject parentObject = new JSONObject(response);
                            String name = parentObject.getString("name");
                            String symbol = parentObject.getString("symbol");
                            Integer decimals = parentObject.getInt("decimals");
                            Double balance = parentObject.getDouble("balance");

                            TableRow row = new TableRow(this);
                            TextView amount = new TextView(this);
                            TextView tv = new TextView(this);
                            amount.setText(balance.toString());
                            tv.setText(symbol);

                            row.addView(amount);
                            row.addView(tv);
                            Log.i("BALANCE", name + "---" + amount);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Snackbar.make(contextView, "Cannot retrieve com.namadi.crimson.wallet balance", Snackbar.LENGTH_LONG).show();
                            Log.i("TOKEN", e.getMessage());
                        }
                    },
                    error -> {
                        Snackbar.make(contextView, "Cannot retrieve com.namadi.crimson.wallet balance", Snackbar.LENGTH_LONG).show();
                        Log.i("TOKEN", error.toString());
                    }
            );
            MainActivity.queue.add(postRequest);
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
                        Log.i("TOKEN", parentObject.toString());
                        String address = parentObject.getString("address");
                        String txHash = parentObject.getString("txHash");
                        Integer amount = parentObject.getInt("amount");
                        Snackbar.make(contextView, "Request Successful", Snackbar.LENGTH_LONG).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Snackbar.make(contextView, "Cannot send request at the moment", Snackbar.LENGTH_LONG).show();
                        Log.i("TOKEN", e.getMessage());
                    }
                    reqDialog.dismiss();
                },
                error -> {
                    Snackbar.make(contextView, error.getMessage(), Snackbar.LENGTH_LONG).show();
                    reqDialog.dismiss();
                    Log.i("TOKEN", error.toString());
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
            Log.d("dirrrrrr", "" + wallpaperDirectory.mkdirs());
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
            Log.d("TAG", "File Saved::--->" + f.getAbsolutePath());

            return f.getAbsolutePath();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return "";

    }

}
