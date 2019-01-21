package com.namadi.crimson.activities.pin;


import android.content.SharedPreferences;
import com.namadi.crimson.activities.MainActivity;

public class SetPinActivity extends com.manusunny.pinlock.SetPinActivity {


    public void onPinSet(String pin){
        SharedPreferences.Editor editor = MainActivity.sharedPref.edit();

        // save pin in shared pref
        editor.putString("SET-PIN", pin);
        editor.commit();
    }

    @Override
    public void onBackPressed() {
        // exit app if user go back
        int pid = android.os.Process.myPid();
        android.os.Process.killProcess(pid);
    }

}
