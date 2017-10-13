package com.amatecny.android.icantrackyou.application;

import android.app.Activity;
import android.app.Application;

import com.amatecny.android.icantrackyou.application.di.DaggerTrackingApplicationComponent;

import javax.inject.Inject;

import dagger.android.DispatchingAndroidInjector;
import dagger.android.HasActivityInjector;
import timber.log.Timber;

/**
 * Contains necessary initializations on an App level
 * <p>
 * Created by amatecny on 11/10/2017
 */
public class TrackingApplication extends Application implements HasActivityInjector {

    @Inject
    DispatchingAndroidInjector<Activity> dispatchingAndroidInjector;

    @Override
    public DispatchingAndroidInjector<Activity> activityInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //di
        DaggerTrackingApplicationComponent.builder()
                .application( this )
                .build()
                .inject( this );

        //logging
        Timber.plant( new Timber.DebugTree() );

    }
}
