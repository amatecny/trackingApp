package com.amatecny.android.icantrackyou.tracking.activity;

import android.os.Bundle;

import com.amatecny.android.icantrackyou.R;
import com.amatecny.android.icantrackyou.tracking.map.TrackingMapFragment;
import com.amatecny.android.icantrackyou.tracking.map.TrackingSettingsFragment;

import dagger.android.support.DaggerAppCompatActivity;

/**
 * Created by amatecny on 11/10/2017
 */
public class TrackingActivity extends DaggerAppCompatActivity {

    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_tracking );

        //only if there is no activity restart, add starting fragment
        if ( savedInstanceState == null ) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add( R.id.content_frame, new TrackingMapFragment() )
                    .commit();
        }
    }

    /**
     * Called from child fragments whenever their logical lifecycle is over,
     * Activity decides what to do, either displaying another fragment, finish, etc.
     * <p>
     * In this particular case, just finish
     */
    public void delegateFinish() {
        finish();
    }

    /**
     * Called by child fragments, when they would like to delegate the request to display settings
     */
    public void delegateDisplaySettings() {
        getSupportFragmentManager().beginTransaction()
                .replace( R.id.content_frame, new TrackingSettingsFragment() )
                .addToBackStack( null )
                .commit();
    }
}
