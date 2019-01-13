package com.namadi.crimson.activities.pin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.namadi.crimson.R;
import com.namadi.crimson.activities.MainActivity;

public class ConfirmPinActivity extends com.manusunny.pinlock.ConfirmPinActivity {

    public static SharedPreferences sharedPref;

    @Override
    public boolean isPinCorrect(String pin) {
        sharedPref = MainActivity.sharedPref;
        String currentPin = sharedPref.getString("SET-PIN", null);
        return pin.equals(currentPin);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

    @Override
    public void onForgotPin() {

    }
}
