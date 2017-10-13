package com.amatecny.android.icantrackyou.tracking.activity.di;

import com.amatecny.android.icantrackyou.tracking.map.TrackingMapFragment;
import com.amatecny.android.icantrackyou.tracking.map.TrackingMapModule;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;

/**
 * Created by amatecny on 11/10/2017
 */
@Module
public abstract class TrackingFragmentBuilderModule {

    @ContributesAndroidInjector(modules = TrackingMapModule.class)
    abstract TrackingMapFragment bindMapFragment();
}
