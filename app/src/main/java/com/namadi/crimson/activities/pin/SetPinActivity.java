package com.namadi.crimson.activities.pin;


import android.content.Intent;
import android.content.SharedPreferences;

import com.namadi.crimson.activities.MainActivity;

public class SetPinActivity extends com.manusunny.pinlock.SetPinActivity {

    public static SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    public void onPinSet(String pin){
        editor = MainActivity.sharedPref.edit();

        //Save 'pin' as SharedPreference
        editor.putString("SET-PIN", pin);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
//        Intent intent = new Intent(this, MainActivity.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        startActivity(intent);
    }

}
