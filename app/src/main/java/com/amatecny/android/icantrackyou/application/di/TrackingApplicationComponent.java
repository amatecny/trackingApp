package com.amatecny.android.icantrackyou.application.di;

import com.amatecny.android.icantrackyou.application.TrackingApplication;

import javax.inject.Singleton;

import dagger.BindsInstance;
import dagger.Component;
import dagger.android.support.AndroidSupportInjectionModule;

/**
 * Created by amatecny on 11/10/2017
 */
@Singleton
@Component(modules = {
        AndroidSupportInjectionModule.class,
        TrackingApplicationModule.class,
        ActivityBuilderModule.class
})
public interface TrackingApplicationComponent {

    void inject( TrackingApplication application );

    @Component.Builder
    interface Builder {

        @BindsInstance
        Builder application( TrackingApplication application );

        TrackingApplicationComponent build();
    }
}
