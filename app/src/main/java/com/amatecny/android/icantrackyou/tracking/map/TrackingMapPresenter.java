package com.amatecny.android.icantrackyou.tracking.map;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.amatecny.android.icantrackyou.R;
import com.amatecny.android.icantrackyou.mvp.presenter.BaseMvpPresenter;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.patloew.rxlocation.RxLocation;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import hu.akarnokd.rxjava2.math.MathObservable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Holds the core logic of the tracking map fragment
 *
 * @see TrackingMapFragment
 * <p>
 * Created by amatecny on 11/10/2017
 */
class TrackingMapPresenter extends BaseMvpPresenter<TrackingMapContract.View> implements TrackingMapContract.Presenter {

    private static final int SMALLEST_DISPLACEMENT_METERS = 50;
    private static final int ACCURACY_CIRCLE_STROKE_WIDTH = 8;

    private final RxLocation rxLocation;
    private SharedPreferences settingsPreferences;

    @VisibleForTesting
    final List<Location> trackedLocations;

    //use default locale for date formatting
    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance( DateFormat.LONG, DateFormat.MEDIUM );

    TrackingMapPresenter( @NonNull TrackingMapContract.View view, RxLocation rxLocation, SharedPreferences settingsPreferences ) {
        super( view );

        this.rxLocation = rxLocation;
        this.settingsPreferences = settingsPreferences;
        trackedLocations = new ArrayList<>();
    }

    @Override
    public void viewCreated() {
        //user may revoke the permissions anytime, rather check it every time then
        if ( ActivityCompat.checkSelfPermission( getView().getContext(), Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            view.requestLocationPermission();
        } else {
            //now subscribe to new location updates
            subscribeToLocationUpdates();
        }
    }

    @Override
    public void mapReady() {
    }

    @Override
    public void locationPermissionGranted() {
        subscribeToLocationUpdates();
    }

    @Override
    public void locationPermissionDenied() {
        //for the purposes of this app, a denial prevents the core functionality of the app, therefore call for finish
        view.finishNoPermissions();
    }

    @Override
    public void onMarkerCLicked( int markerId ) {
        //retrieve the location associated with this marker
        Location location = trackedLocations.get( markerId );
        focusPositionAtLocation( location );
    }

    @Override
    public void markerDetailsRemoved() {

        //hide the accuracy drawing, that was requested to be diplayed on marker click
        view.removeTemporaryMarkerAccuracyDrawings();
    }

    @Override
    public void northernmostPointLocationRequested() {
        MathObservable.max( Observable.fromIterable( trackedLocations ),
                ( o1, o2 ) -> Double.compare( o1.getLatitude(), o2.getLatitude() ) )
                .subscribeOn( Schedulers.computation() )
                .observeOn( AndroidSchedulers.mainThread() )
                .compose( storeDisposable() )
                .subscribe( this::focusPositionAtLocation );
    }

    @Override
    public void southernmostPointLocationRequested() {
        MathObservable.min( Observable.fromIterable( trackedLocations ),
                ( o1, o2 ) -> Double.compare( o1.getLatitude(), o2.getLatitude() ) )
                .subscribeOn( Schedulers.computation() )
                .observeOn( AndroidSchedulers.mainThread() )
                .compose( storeDisposable() )
                .subscribe( this::focusPositionAtLocation );
    }

    @Override
    public void westernmostPointLocationRequested() {
        MathObservable.min( Observable.fromIterable( trackedLocations ),
                ( o1, o2 ) -> Double.compare( o1.getLongitude(), o2.getLongitude() ) )
                .subscribeOn( Schedulers.computation() )
                .observeOn( AndroidSchedulers.mainThread() )
                .compose( storeDisposable() )
                .subscribe( this::focusPositionAtLocation );
    }

    @Override
    public void easternmostPointLocationRequested() {
        MathObservable.max( Observable.fromIterable( trackedLocations ),
                ( o1, o2 ) -> Double.compare( o1.getLongitude(), o2.getLongitude() ) )
                .subscribeOn( Schedulers.computation() )
                .observeOn( AndroidSchedulers.mainThread() )
                .compose( storeDisposable() )
                .subscribe( this::focusPositionAtLocation );
    }

    @Override
    public void settingsRequested() {
        view.displaySettings();
    }

    private void subscribeToLocationUpdates() {
        //check if there are any already tracked locations
        //here it starts to be interesting as we need to wait for the map to be prepared for any ops, therefore go async
        Observable.just( trackedLocations )
                .delay( 1, TimeUnit.SECONDS )//hmm, i'd use countdownlatch,but it is not a friend with tests
                .observeOn( AndroidSchedulers.mainThread() )
                .doOnNext( locations -> view.clearMarkersAndPath() )//ultimately, clear any drawings from the previous occasions
                .doOnNext( locations -> {
                    for ( int i = 0; i < locations.size(); i++ ) {
                        Location locationAtIndex = locations.get( i );
                        displayMarker( locationAtIndex, i );//display markers immediately

                        //also move the camera to the last position
                        if ( i == locations.size() - 1 ) {
                            animateCamera( locationAtIndex );
                        }
                    }
                } )
                .flatMap( Observable::fromIterable )
                .map( location -> new LatLng( location.getLatitude(), location.getLongitude() ) )
                .toList()
                .doOnSuccess( latLngs -> {
                    //continue only if there is >=2 of positions == a PATH
                    if ( latLngs.size() >= 2 ) {
                        //we have an actual path, display it
                        PolylineOptions path = getStyledPolylinePathOptions();
                        path.addAll( latLngs );
                        view.displayPath( path );
                    }
                } )
                .compose( storeSingleDisposable() )
                .subscribe();


        //prepare location request
        String interval = settingsPreferences.getString( view.getContext().getString( R.string.location_interval_pref_key ), null );

        int intervalNum = view.getContext().getResources().getInteger( R.integer.integer_default_location_tracking_interval );
        if ( interval != null ) {
            try {
                intervalNum = Integer.parseInt( interval );
            } catch ( NumberFormatException ignored ) {
            }
        }

        LocationRequest locationRequest = LocationRequest.create()
                .setPriority( LocationRequest.PRIORITY_HIGH_ACCURACY )
                .setSmallestDisplacement( SMALLEST_DISPLACEMENT_METERS )//50 meters
                .setInterval( intervalNum * 1000 );//convert to millis

        //start listening to location updates
        //noinspection MissingPermission - permissions are already checked, outside of this method
        rxLocation.location().updates( locationRequest )
                .doOnNext( location -> {
                    displayMarker( location, trackedLocations.size() );//just display it immediately, position would be at the end of the list
                    animateCamera( location );
                } )
                .compose( storeDisposable() )
                .subscribe( newestLocation -> {
                    //grab the last position and designate a path to current position
                    if ( !trackedLocations.isEmpty() ) {
                        PolylineOptions options = getStyledPolylinePathOptions();
                        Location lastLocation = trackedLocations.get( trackedLocations.size() - 1 );
                        //previous location - start
                        options.add( new LatLng( lastLocation.getLatitude(), lastLocation.getLongitude() ) );

                        //TODO SphericalUtil.interpolate
                        //or maybe a cubic bezier?

                        //new location - end
                        options.add( new LatLng( newestLocation.getLatitude(), newestLocation.getLongitude() ) );
                        //and now display it
                        view.displayPath( options );
                    }

                    //save the latest location
                    trackedLocations.add( newestLocation );
                } );
    }

    private void focusPositionAtLocation( Location location ) {
        //move to the marker
        animateCamera( location );
        //display the details
        rxLocation.geocoding().fromLocation( location ).toObservable()
                .subscribeOn( Schedulers.io() )
                .observeOn( AndroidSchedulers.mainThread() )
                .compose( storeDisposable() )
                .subscribe( address -> {
                    String addressLine = address.getAddressLine( 0 );
                    if ( addressLine == null ) {
                        //this seems to work every time so far, note: add checks
                        addressLine = String.format( view.getContext().getString( R.string.address_on_display ), address.getLocality(), address.getAdminArea() );
                    }

                    view.displayMarkerDetails(
                            addressLine,
                            Location.convert( location.getLongitude(), Location.FORMAT_DEGREES ),
                            Location.convert( location.getLatitude(), Location.FORMAT_DEGREES ),
                            String.format( view.getContext().getString( R.string.accuracy_in_meters ), location.getAccuracy() ),//yeah some locales might prefer imperial units, but consider it out of the scope here
                            getDateTimeCurrentTimeZone( location.getTime() ),
                            location.getProvider() );

                    //also draw accuracy circle around the marker
                    //but firstly remove any previous drawings
                    view.removeTemporaryMarkerAccuracyDrawings();
                    view.drawTempMarkerAccuracy( new CircleOptions()
                            .center( new LatLng( location.getLatitude(), location.getLongitude() ) )
                            .radius( location.getAccuracy() )
                            .strokeWidth( ACCURACY_CIRCLE_STROKE_WIDTH )
                            .strokeColor( ContextCompat.getColor( view.getContext(), R.color.colorMarkerAccuracyStroke ) )
                            .fillColor( ContextCompat.getColor( view.getContext(), R.color.colorMarkerAccuracySolid ) ) );

                } );
    }


    //private helpers

    private void displayMarker( Location location, int locationPosition ) {
        LatLng latLng = new LatLng( location.getLatitude(), location.getLongitude() );
        view.displayMarker( latLng, locationPosition );
    }

    private void animateCamera( Location location ) {
        //construct the the camera update
        CameraPosition.Builder positionBuilder = new CameraPosition.Builder()
                .target( new LatLng( location.getLatitude(), location.getLongitude() ) )
                .zoom( 17 );

        //if there is bearing associated with this update, send it in
        if ( location.hasBearing() ) {
            positionBuilder.bearing( location.getBearing() );
        }
        view.updateCamera( positionBuilder.build() );
    }

    @NonNull
    private PolylineOptions getStyledPolylinePathOptions() {

        //load the color
        String pathColorString = settingsPreferences.getString( view.getContext().getString( R.string.path_color_pref_key ), null );
        int color = ContextCompat.getColor( view.getContext(), R.color.defaultColorPath );

        if ( pathColorString != null ) {
            try {
                color = Color.parseColor( pathColorString );
            } catch ( IllegalArgumentException ignored ) {
            }
        }

        //and width from preferences
        String widthString = settingsPreferences.getString( view.getContext().getString( R.string.path_width_pref_key ), null );
        int width = view.getContext().getResources().getInteger( R.integer.integer_default_path_width );

        if ( widthString != null ) {
            try {
                width = Integer.parseInt( widthString );
            } catch ( NumberFormatException ignored ) {
            }
        }

        return new PolylineOptions()
                .color( color )
                .width( width )
                .visible( true );
    }

    /**
     * @param timestamp time in milliss since the start of the epoch
     * @return formatted string
     */
    private String getDateTimeCurrentTimeZone( long timestamp ) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis( timestamp );

        //add time zone offset, probably not needed here
        TimeZone tz = TimeZone.getDefault();
        calendar.add( Calendar.MILLISECOND, tz.getOffset( calendar.getTimeInMillis() ) );

        return DATE_FORMAT.format( calendar.getTime() );
    }
}
