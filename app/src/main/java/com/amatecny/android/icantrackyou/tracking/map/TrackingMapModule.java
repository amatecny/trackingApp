package com.amatecny.android.icantrackyou.tracking.map;

import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.patloew.rxlocation.RxLocation;

import dagger.Module;
import dagger.Provides;

/**
 * Created by amatecny on 27/09/2017
 */
@Module
public class TrackingMapModule {

    @Provides
    static TrackingMapContract.View provideView( TrackingMapFragment fragment ) {
        return fragment;
    }

    @Provides
    static TrackingMapContract.Presenter providePresenter( TrackingMapContract.View view ) {
        SharedPreferences settingPrefs = PreferenceManager.getDefaultSharedPreferences( view.getContext() );
        return new TrackingMapPresenter( view, new RxLocation( view.getContext() ), settingPrefs );
    }
}
