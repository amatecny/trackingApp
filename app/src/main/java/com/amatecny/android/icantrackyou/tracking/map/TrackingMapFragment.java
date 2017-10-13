package com.amatecny.android.icantrackyou.tracking.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.amatecny.android.icantrackyou.R;
import com.amatecny.android.icantrackyou.mvp.view.MvpMapFragment;
import com.amatecny.android.icantrackyou.tracking.activity.TrackingActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.jakewharton.rxbinding2.view.RxView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import timber.log.Timber;

/**
 * Main fragment handling the map events and controls, delegating the logic to {@link TrackingMapPresenter}
 * <p>
 * Created by amatecny on 11/10/2017
 */
public class TrackingMapFragment extends MvpMapFragment<TrackingMapContract.Presenter> implements TrackingMapContract.View, OnMapReadyCallback {

    public static final int LOCATION_PERMISSION_REQUEST_CODE = 8181;
    private GoogleMap googleMap;
    private Unbinder unbinder;

    @BindView(R.id.bottom_sheet_layout) ViewGroup bottomSheetLayout;
    private BottomSheetBehavior<ViewGroup> bottomSheetBehavior;

    @BindView(R.id.address_value) TextView addressTextView;
    @BindView(R.id.address_value_separator) View addressSeparatorView;
    @BindView(R.id.longitude_value) TextView longitudeTextView;
    @BindView(R.id.latitude_value) TextView latitudeTextView;
    @BindView(R.id.accuracy_value) TextView accuracyTextView;
    @BindView(R.id.time_value) TextView timeTextView;
    @BindView(R.id.source_value) TextView sourceTextView;

    //extreme points display control
    @BindView(R.id.btn_control_northernmost_point) View northPointBtn;
    @BindView(R.id.btn_control_westernhost_point) View westPointBtn;
    @BindView(R.id.btn_control_easternmost_point) View eastPointBtn;
    @BindView(R.id.btn_control_southernmost_point) View southPointBtn;

    private Circle tempMarkerCircle;

    @Override
    public void onCreate( Bundle bundle ) {
        super.onCreate( bundle );
        setRetainInstance( true );
        setHasOptionsMenu( true );
    }

    @Override
    public View onCreateView( LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle ) {
        View root = layoutInflater.inflate( R.layout.fragment_map_layout, viewGroup, false );
        unbinder = ButterKnife.bind( this, root );

        mapView = root.findViewById( R.id.map );
        return root;
    }

    @Override
    public void onViewCreated( View view, @Nullable Bundle savedInstanceState ) {
        super.onViewCreated( view, savedInstanceState );
        //obtain the google map
        mapView.getMapAsync( this );

        bottomSheetBehavior = BottomSheetBehavior.from( bottomSheetLayout );
        bottomSheetBehavior.setState( BottomSheetBehavior.STATE_HIDDEN );

        bottomSheetBehavior.setBottomSheetCallback( new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged( @NonNull View bottomSheet, int newState ) {
                Timber.d( "Slide:" + newState );
                if ( newState == BottomSheetBehavior.STATE_HIDDEN ) {
                    presenter.markerDetailsRemoved();
                }
            }

            @Override
            public void onSlide( @NonNull View bottomSheet, float slideOffset ) {
                //do nothing
            }
        } );

        //extreme point selection controls
        RxView.clicks( northPointBtn )
                .compose( storeDisposable() )
                .subscribe( click -> presenter.northernmostPointLocationRequested() );

        RxView.clicks( southPointBtn )
                .compose( storeDisposable() )
                .subscribe( click -> presenter.southernmostPointLocationRequested() );

        RxView.clicks( westPointBtn )
                .compose( storeDisposable() )
                .subscribe( click -> presenter.westernmostPointLocationRequested() );

        RxView.clicks( eastPointBtn )
                .compose( storeDisposable() )
                .subscribe( click -> presenter.easternmostPointLocationRequested() );
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater ) {
        inflater.inflate( R.menu.tracking_map_fragment_menu, menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.settings:
                presenter.settingsRequested();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onMapReady( GoogleMap googleMap ) {
        this.googleMap = googleMap;
        this.googleMap.setOnMarkerClickListener( marker -> {
            presenter.onMarkerCLicked( ( int ) marker.getTag() );
            //consume the event
            return true;
        } );

        this.googleMap.setOnMapClickListener( latLng -> {
            //hide bottom sheet if possible, no logic behind this, pure view UX
            if ( bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN ) {
                bottomSheetBehavior.setState( BottomSheetBehavior.STATE_HIDDEN );
            }
        } );
        presenter.mapReady();
    }

    @Override
    public void displayMarker( @NonNull LatLng markerPosition, int markerId ) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position( markerPosition );

        //add the marker and set the tag
        Marker marker = googleMap.addMarker( markerOptions );
        marker.setTag( markerId );
    }

    @Override
    public void updateCamera( @NonNull CameraPosition cameraPosition ) {
        //it'd be better to display it off-centre

        googleMap.animateCamera( CameraUpdateFactory.newCameraPosition( cameraPosition ) );
    }

    @Override
    public void requestLocationPermission() {
        requestPermissions(
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE );
    }

    @Override
    public void finishNoPermissions() {
        ( ( TrackingActivity ) getActivity() ).delegateFinish();
    }

    @Override
    public void displayPath( @NonNull PolylineOptions options ) {
        googleMap.addPolyline( options );
    }

    @Override
    public void clearMarkersAndPath() {
        googleMap.clear();
    }

    @Override
    public void displayMarkerDetails( @Nullable String address, @NonNull String longitude, @NonNull String latitude,
                                      @NonNull String accuracy, @NonNull String dateTimeCurrentTimeZone, @Nullable String provider ) {
        if ( address != null ) {
            addressTextView.setText( address );
            addressTextView.setVisibility( View.VISIBLE );
            addressSeparatorView.setVisibility( View.VISIBLE );
        } else {
            addressTextView.setVisibility( View.GONE );
            addressSeparatorView.setVisibility( View.GONE );
        }

        addressTextView.setText( address );
        longitudeTextView.setText( longitude );
        latitudeTextView.setText( latitude );
        accuracyTextView.setText( accuracy );
        timeTextView.setText( dateTimeCurrentTimeZone );

        if ( provider != null ) {
            sourceTextView.setText( provider );
            sourceTextView.setVisibility( View.VISIBLE );
        } else {
            sourceTextView.setVisibility( View.GONE );
        }

        bottomSheetBehavior.setState( BottomSheetBehavior.STATE_EXPANDED );
    }

    @Override
    public void drawTempMarkerAccuracy( @NonNull CircleOptions circleOptions ) {
        tempMarkerCircle = googleMap.addCircle( circleOptions );
    }

    @Override
    public void removeTemporaryMarkerAccuracyDrawings() {
        //remove the temp drawing and clear the ref
        if ( tempMarkerCircle != null ) {
            tempMarkerCircle.remove();
            tempMarkerCircle = null;
        }
    }

    @Override
    public void displaySettings() {
        ((TrackingActivity)getActivity()).delegateDisplaySettings();
    }

    @Override
    public void onRequestPermissionsResult( int requestCode, @NonNull String permissions[], @NonNull int[] grantResults ) {
        switch ( requestCode ) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty. But for this app, we'd like to have both permissions
                if ( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ) {
                    presenter.locationPermissionGranted();
                } else {
                    // permission denied :(
                    presenter.locationPermissionDenied();
                }
            }
        }
    }
}
