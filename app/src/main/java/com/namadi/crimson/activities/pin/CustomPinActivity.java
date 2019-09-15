package com.namadi.crimson.activities.pin;

import android.content.Intent;
import android.os.Bundle;

import com.afollestad.materialdialogs.MaterialDialog;
import com.github.orangegangsters.lollipin.lib.managers.AppLockActivity;
import android.view.View;

import com.namadi.crimson.R;
import com.namadi.crimson.activities.MainActivity;

public class CustomPinActivity extends AppLockActivity {

    private MaterialDialog.Builder builder;
    private MaterialDialog dialog;

    @Override
    public void showForgotDialog() {
        builder = new MaterialDialog.Builder(this);
        builder
                .title("Wipe App Data?")
                .content("Are you sure you want to delete all app data, which includes (Wallet(s), Token(s), Tx(s)?")
                .negativeText("No")
                .positiveText("Yes")
                .cancelable(false)
                .onPositive((dialog, which) -> {
                    MainActivity.sharedPref
                            .edit()
                            .clear()
                            .apply();
                    MainActivity.walletBox.removeAll();
                    MainActivity.tokenBox.removeAll();
                    MainActivity.balanceBox.removeAll();
                    dialog.dismiss();
                    finish();
                })
                .onNegative((dialog, which) -> {
                    dialog.dismiss();
                    finish();
                    startActivity(getIntent());
                })
                .onAny((dialog, which) -> {
                    dialog.dismiss();
                    finish();
                    startActivity(new Intent(this, MainActivity.class));
                });


        dialog = builder.build();
        dialog.show();
    }

    @Override
    public void onPinFailure(int attempts) {

    }

    @Override
    public void onPinSuccess(int attempts) {

    }
}
