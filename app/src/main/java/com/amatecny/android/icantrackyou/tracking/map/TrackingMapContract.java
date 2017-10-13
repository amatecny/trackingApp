package com.amatecny.android.icantrackyou.tracking.map;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.amatecny.android.icantrackyou.mvp.presenter.MvpPresenter;
import com.amatecny.android.icantrackyou.mvp.view.MvpView;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

/**
 * Contract defining the api between {@link TrackingMapFragment} and {@link TrackingMapPresenter}
 * <p>
 * Created by amatecny on 11/10/2017
 */
public interface TrackingMapContract {

    interface View extends MvpView {

        /**
         * Called to display a marker at a given position of a map
         *
         * @param markerPosition new marker possiton
         * @param markerId       identification of the marker / position in the list
         */
        void displayMarker( @NonNull LatLng markerPosition, int markerId );

        /**
         * Called to update the camera position to a desired {@link CameraPosition}
         *
         * @param cameraPosition new camera position
         */
        void updateCamera( @NonNull CameraPosition cameraPosition );

        /**
         * Called to request user for a necessary permissions
         */
        void requestLocationPermission();

        /**
         * Called to exit as there is nothing to do without location permissions
         */
        void finishNoPermissions();

        void displayPath( @NonNull PolylineOptions options );

        void clearMarkersAndPath();

        /**
         * Called to display details about marker
         *
         * @param address                 address of the marker
         * @param longitude               longitude of the marker
         * @param latitude                latitude of the marker
         * @param accuracy                accuracy fo the location fix
         * @param dateTimeCurrentTimeZone time of the location fix
         * @param provider                name of the provider of the fix
         */
        void displayMarkerDetails( @Nullable String address, @NonNull String longitude, @NonNull String latitude, @NonNull String accuracy, @NonNull String dateTimeCurrentTimeZone, @Nullable String provider );

        /**
         * Command to draw a circle representing the accuracy of the fix/marker's fix
         */
        void drawTempMarkerAccuracy( @NonNull CircleOptions circleOptions );

        /**
         * Command to remove the temporary drawing, such as accuracy circle
         */
        void removeTemporaryMarkerAccuracyDrawings();

        /**
         * Command to display settings screen
         */
        void displaySettings();
    }

    interface Presenter extends MvpPresenter<View> {

        /**
         * Called when underlying map is intialized and is ready to be drawn onto
         */
        void mapReady();

        /**
         * Location permission ahs been granted by user
         */
        void locationPermissionGranted();

        /**
         * Location permission ahs been denied by user
         */
        void locationPermissionDenied();

        /**
         * Marker was clicked on by user
         *
         * @param markerId id of the marker that was clicked on
         */
        void onMarkerCLicked( int markerId );

        /**
         * Event occuring when user dismisses the marker details, useful for removal of temporary elements
         */
        void markerDetailsRemoved();

        /**
         * User requests the position of the northernmost point to be displayed
         */
        void northernmostPointLocationRequested();

        /**
         * User requests the position of the southernmost point to be displayed
         */
        void southernmostPointLocationRequested();

        /**
         * User requests the position of the westernmost point to be displayed
         */
        void westernmostPointLocationRequested();

        /**
         * User requests the position of the easternmost point to be displayed
         */
        void easternmostPointLocationRequested();

        /**
         * Setting button was clicked on
         */
        void settingsRequested();
    }
}
