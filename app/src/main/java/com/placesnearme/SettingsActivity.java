package com.placesnearme;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.placesnearme.fragments.SettingFragment;

public class SettingsActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content,new SettingFragment()).commit();
    }
}
