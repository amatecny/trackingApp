package com.amatecny.android.icantrackyou.mvp.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.amatecny.android.icantrackyou.mvp.presenter.BaseMvpPresenter;
import com.amatecny.android.icantrackyou.mvp.presenter.MvpPresenter;
import com.google.android.gms.maps.MapView;

/**
 * Extension to {@link MvpFragment}, that adds support for {@link com.google.android.gms.maps.MapView}
 *
 * In order to make sure that this fragment works correctly, child classes have to add {@link com.google.android.gms.maps.MapView}
 * into their view hierarchy and bind it prior {@link #onViewCreated(View, Bundle)}
 *
 * @see BaseMvpPresenter for basic presenter implementation
 * Created by amatecny on 31/01/2017.
 */
public abstract class MvpMapFragment<P extends MvpPresenter> extends MvpFragment<P> implements MvpView {

    protected MapView mapView;

    @Override
    public void onViewCreated( View view, @Nullable Bundle savedInstanceState ) {
        super.onViewCreated( view, savedInstanceState );
        mapView.onCreate( savedInstanceState );
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState( Bundle outState ) {
        super.onSaveInstanceState( outState );
        mapView.onSaveInstanceState( outState );
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
