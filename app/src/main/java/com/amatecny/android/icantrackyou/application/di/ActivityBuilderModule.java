package com.amatecny.android.icantrackyou.application.di;


import com.amatecny.android.icantrackyou.tracking.activity.TrackingActivity;
import com.amatecny.android.icantrackyou.tracking.activity.di.TrackingFragmentBuilderModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by amatecny on 11/10/2017
 */
@Module
public abstract class ActivityBuilderModule {

    @ContributesAndroidInjector(modules = {
            TrackingFragmentBuilderModule.class
    })
    abstract TrackingActivity bindTrackingActivity();

}
