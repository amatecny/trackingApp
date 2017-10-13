package com.amatecny.android.icantrackyou.application.di;

import android.content.Context;

import com.amatecny.android.icantrackyou.application.TrackingApplication;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by amatecny on 11/10/2017
 */
@Module
public class TrackingApplicationModule {

    @Provides
    @Singleton
    Context provideContext( TrackingApplication application ) {
        return application.getApplicationContext();
    }
}
