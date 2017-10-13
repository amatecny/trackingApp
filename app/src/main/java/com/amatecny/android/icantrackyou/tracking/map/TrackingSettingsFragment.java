package com.amatecny.android.icantrackyou.tracking.map;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.amatecny.android.icantrackyou.R;

/**
 * Simple preference fragment handling the properties of the main fragment
 * {@link com.amatecny.android.icantrackyou.tracking.map.TrackingMapFragment}
 * <p>
 * Created by amatecny on 13/10/2017
 */
public class TrackingSettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences( Bundle savedInstanceState, String rootKey ) {
        setPreferencesFromResource( R.xml.tracking_settings_preference, rootKey );
    }

}
