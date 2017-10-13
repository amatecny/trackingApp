package com.amatecny.android.icantrackyou.tracking.map;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.support.test.InstrumentationRegistry;

import com.amatecny.android.icantrackyou.ImmediateSchedulersRule;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.patloew.rxlocation.FusedLocation;
import com.patloew.rxlocation.Geocoding;
import com.patloew.rxlocation.RxLocation;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import io.reactivex.Maybe;
import io.reactivex.Observable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.StrictStubs.class)
public class TrackingMapPresenterTest {

    @Mock
    TrackingMapContract.View mockView;

    @Mock
    RxLocation mockLocation;

    @Mock
    SharedPreferences mockPreferences;

    @Rule
    public final ImmediateSchedulersRule schedulers = new ImmediateSchedulersRule();
    private TrackingMapPresenter uut;

    @Before
    public void setup() {
        uut = new TrackingMapPresenter( mockView, mockLocation, mockPreferences );
    }

    @Test
    public void testViewCreatedRequestPermissionSuccessFirstLocationUpdate() {

        Context mockContext = mock( Context.class );
        when( mockContext.checkPermission( anyString(), anyInt(), anyInt() ) ).thenReturn( PackageManager.PERMISSION_GRANTED );
        when( mockContext.getResources() ).thenReturn( InstrumentationRegistry.getTargetContext().getResources() );
        when( mockView.getContext() ).thenReturn( mockContext );

        FusedLocation mockFusedLocation = mock( FusedLocation.class );


        Location mockLocation = mock( Location.class );
        double testLatitude = 50.0;
        double testLongitude = 50.0;
        when( mockLocation.getLatitude() ).thenReturn( testLatitude );
        when( mockLocation.getLongitude() ).thenReturn( testLongitude );
        when( mockLocation.hasBearing() ).thenReturn( false );

        when( mockFusedLocation.updates( any() ) ).thenReturn( Observable.just( mockLocation ) );
        when( this.mockLocation.location() ).thenReturn( mockFusedLocation );

        uut.viewCreated();

        verify( mockView, never() ).requestLocationPermission();

        //trigger async calls
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        //but first location was supposed to be displayed
        ArgumentCaptor<LatLng> latlngCaptor = ArgumentCaptor.forClass( LatLng.class );
        verify( mockView ).displayMarker( latlngCaptor.capture(), anyInt() );

        LatLng capturedLatLng = latlngCaptor.getValue();
        assertThat( capturedLatLng.latitude ).isEqualTo( testLatitude );
        assertThat( capturedLatLng.longitude ).isEqualTo( testLongitude );

        ArgumentCaptor<CameraPosition> cameraPositionCaptor = ArgumentCaptor.forClass( CameraPosition.class );
        verify( mockView ).updateCamera( cameraPositionCaptor.capture() );

        LatLng cameraLatLng = cameraPositionCaptor.getValue().target;
        assertThat( cameraLatLng.latitude ).isEqualTo( testLatitude );
        assertThat( cameraLatLng.longitude ).isEqualTo( testLongitude );

    }

    @Test
    public void testViewCreatedRequestPermissionFailure() {

        Context mock = mock( Context.class );
        when( mock.checkPermission( anyString(), anyInt(), anyInt() ) ).thenReturn( PackageManager.PERMISSION_DENIED );
        when( mockView.getContext() ).thenReturn( mock );

        uut.viewCreated();

        verify( mockView ).requestLocationPermission();
    }

    @Test
    public void testPermissionGranted() {

        when( mockView.getContext() ).thenReturn( InstrumentationRegistry.getTargetContext() );

        FusedLocation mockFusedLocation = mock( FusedLocation.class );
        Location mockLocation = mock( Location.class );
        double testLatitude = 50.0;
        double testLongitude = 50.0;
        when( mockLocation.getLatitude() ).thenReturn( testLatitude );
        when( mockLocation.getLongitude() ).thenReturn( testLongitude );
        when( mockLocation.hasBearing() ).thenReturn( false );

        //noinspection MissingPermission - really? i thought you could do better
        when( mockFusedLocation.updates( any() ) ).thenReturn( Observable.just( mockLocation ) );
        when( this.mockLocation.location() ).thenReturn( mockFusedLocation );

        uut.locationPermissionGranted();

        //wait for async calls
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        //but first location was supposed to be displayed
        ArgumentCaptor<LatLng> latlngCaptor = ArgumentCaptor.forClass( LatLng.class );
        verify( mockView ).displayMarker( latlngCaptor.capture(), anyInt() );

        LatLng capturedLatLng = latlngCaptor.getValue();
        assertThat( capturedLatLng.latitude ).isEqualTo( testLatitude );
        assertThat( capturedLatLng.longitude ).isEqualTo( testLongitude );

        ArgumentCaptor<CameraPosition> cameraPositionCaptor = ArgumentCaptor.forClass( CameraPosition.class );
        verify( mockView ).updateCamera( cameraPositionCaptor.capture() );

        LatLng cameraLatLng = cameraPositionCaptor.getValue().target;
        assertThat( cameraLatLng.latitude ).isEqualTo( testLatitude );
        assertThat( cameraLatLng.longitude ).isEqualTo( testLongitude );

    }

    @Test
    public void testPermissionDenied() {
        uut.locationPermissionDenied();
        verify( mockView ).finishNoPermissions();

    }

    @Test
    public void testSettingRequested() {
        uut.settingsRequested();
        verify( mockView ).displaySettings();

    }

    @Test
    public void testMarkerClicked() {

        when( mockView.getContext() ).thenReturn( InstrumentationRegistry.getTargetContext() );

        //preparation
        Location mockLocation = mock( Location.class );
        double testLatitude = 50.0;
        double testLongitude = 50.0;
        float testBearing = 10.0f;
        float testAcc = 25;
        when( mockLocation.getLatitude() ).thenReturn( testLatitude );
        when( mockLocation.getLongitude() ).thenReturn( testLongitude );
        when( mockLocation.getAccuracy() ).thenReturn( testAcc );
        when( mockLocation.getTime() ).thenReturn( 1507927232000L );
        when( mockLocation.hasBearing() ).thenReturn( true );
        when( mockLocation.getBearing() ).thenReturn( testBearing );

        uut.trackedLocations.add( mockLocation );


        //mock the chain of rxlocation's calls
        Geocoding mockGeo = mock( Geocoding.class );

        Address mockAddress = mock( Address.class );
        String testAddress = "First address line, testCity, testCountry";
        when( mockAddress.getAddressLine( 0 ) ).thenReturn( testAddress );
        Maybe<Address> mockAddressMaybe = Maybe.just( mockAddress );
        when( mockGeo.fromLocation( any() ) ).thenReturn( mockAddressMaybe );
        when( this.mockLocation.geocoding() ).thenReturn( mockGeo );

        //tested call
        uut.onMarkerCLicked( 0 );

        //verification
        ArgumentCaptor<CameraPosition> cameraPositionCaptor = ArgumentCaptor.forClass( CameraPosition.class );
        verify( mockView ).updateCamera( cameraPositionCaptor.capture() );

        //verify the position matches
        CameraPosition camera = cameraPositionCaptor.getValue();
        assertThat( camera.target.latitude ).isEqualTo( testLatitude );
        assertThat( camera.target.longitude ).isEqualTo( testLongitude );
        assertThat( camera.bearing ).isEqualTo( testBearing );


        //wait for async calls
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        ArgumentCaptor<String> addressCaptor = ArgumentCaptor.forClass( String.class );
        ArgumentCaptor<String> accCaptor = ArgumentCaptor.forClass( String.class );

        //do not verify each field, as we'd just copy the implementation, which is pointless
        verify( mockView ).displayMarkerDetails( addressCaptor.capture(), anyString(), anyString(), accCaptor.capture(), anyString(), any() );

        assertThat( addressCaptor.getValue() ).isEqualTo( testAddress );
        assertThat( accCaptor.getValue() ).containsIgnoringCase( String.valueOf( testAcc ) );

        //continue with other calls
        verify( mockView ).removeTemporaryMarkerAccuracyDrawings();

        ArgumentCaptor<CircleOptions> tempMarkerAccCaptor = ArgumentCaptor.forClass( CircleOptions.class );
        verify( mockView ).drawTempMarkerAccuracy( tempMarkerAccCaptor.capture() );

        CircleOptions capturedCircleOptions = tempMarkerAccCaptor.getValue();
        assertThat( capturedCircleOptions.getCenter().latitude ).isEqualTo( testLatitude );
        assertThat( capturedCircleOptions.getCenter().longitude ).isEqualTo( testLongitude );
        assertThat( capturedCircleOptions.getRadius() ).isEqualTo( testAcc );
    }

    @Test
    public void testMarkerRemoved() {
        uut.markerDetailsRemoved();
        verify( mockView ).removeTemporaryMarkerAccuracyDrawings();

    }

    @Test
    public void testNorthernmostPointLocationRequestedBothNorth() {
        //mock the chain of rxlocation's calls
        Geocoding mockGeo = mock( Geocoding.class );
        Maybe<Address> mockAddressMaybe = Maybe.empty();
        when( mockGeo.fromLocation( any() ) ).thenReturn( mockAddressMaybe );
        when( this.mockLocation.geocoding() ).thenReturn( mockGeo );


        double testLatitude = 50.0;
        double testLongitude = 50.0;

        Location northern = mock( Location.class );
        when( northern.getLatitude() ).thenReturn( testLatitude );
        when( northern.getLongitude() ).thenReturn( testLongitude );
        when( northern.hasBearing() ).thenReturn( false );

        Location southern = mock( Location.class );
        when( southern.getLatitude() ).thenReturn( 49.0 );
        uut.trackedLocations.add( northern );
        uut.trackedLocations.add( southern );

        uut.northernmostPointLocationRequested();
        //wait for async calls
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();


        //other callbacks to view are covered by other test, as the same function is doing them, just chceck if that function got correct data
        ArgumentCaptor<CameraPosition> cameraPositionCaptor = ArgumentCaptor.forClass( CameraPosition.class );
        verify( mockView ).updateCamera( cameraPositionCaptor.capture() );

        assertThat( cameraPositionCaptor.getValue().target.latitude ).isEqualTo( testLatitude );

    }

    @Test
    public void testNorthernmostPointLocationRequestedBothNorth2() {
        //mock the chain of rxlocation's calls
        Geocoding mockGeo = mock( Geocoding.class );
        Maybe<Address> mockAddressMaybe = Maybe.empty();
        when( mockGeo.fromLocation( any() ) ).thenReturn( mockAddressMaybe );
        when( this.mockLocation.geocoding() ).thenReturn( mockGeo );


        double testLatitude = 90.0;
        double testLongitude = 50.0;

        Location northern = mock( Location.class );
        when( northern.getLatitude() ).thenReturn( testLatitude );
        when( northern.getLongitude() ).thenReturn( testLongitude );
        when( northern.hasBearing() ).thenReturn( false );

        Location southern = mock( Location.class );
        when( southern.getLatitude() ).thenReturn( 91.0 );
        uut.trackedLocations.add( northern );
        uut.trackedLocations.add( southern );

        uut.northernmostPointLocationRequested();
        //wait for async calls
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();


        //other callbacks to view are covered by other test, as the same function is doing them, just chceck if that function got correct data
        ArgumentCaptor<CameraPosition> cameraPositionCaptor = ArgumentCaptor.forClass( CameraPosition.class );
        verify( mockView ).updateCamera( cameraPositionCaptor.capture() );

        assertThat( cameraPositionCaptor.getValue().target.latitude ).isEqualTo( testLatitude );

    }

    @Test
    public void testNorthernmostPointLocationRequestedOneNorthOneSouth() {
        //mock the chain of rxlocation's calls
        Geocoding mockGeo = mock( Geocoding.class );
        Maybe<Address> mockAddressMaybe = Maybe.empty();
        when( mockGeo.fromLocation( any() ) ).thenReturn( mockAddressMaybe );
        when( this.mockLocation.geocoding() ).thenReturn( mockGeo );


        double testLatitude = 10.0;
        double testLongitude = 50.0;

        Location northern = mock( Location.class );
        when( northern.getLatitude() ).thenReturn( testLatitude );
        when( northern.getLongitude() ).thenReturn( testLongitude );
        when( northern.hasBearing() ).thenReturn( false );

        Location southern = mock( Location.class );
        when( southern.getLatitude() ).thenReturn( -10.0 );
        uut.trackedLocations.add( northern );
        uut.trackedLocations.add( southern );

        uut.northernmostPointLocationRequested();
        //wait for async calls
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();


        //other callbacks to view are covered by other test, as the same function is doing them, just chceck if that function got correct data
        ArgumentCaptor<CameraPosition> cameraPositionCaptor = ArgumentCaptor.forClass( CameraPosition.class );
        verify( mockView ).updateCamera( cameraPositionCaptor.capture() );

        assertThat( cameraPositionCaptor.getValue().target.latitude ).isEqualTo( testLatitude );

    }

    @Test
    public void testNorthernmostPointLocationRequestedBothSouth() {
        //mock the chain of rxlocation's calls
        Geocoding mockGeo = mock( Geocoding.class );
        Maybe<Address> mockAddressMaybe = Maybe.empty();
        when( mockGeo.fromLocation( any() ) ).thenReturn( mockAddressMaybe );
        when( this.mockLocation.geocoding() ).thenReturn( mockGeo );


        double testLatitude = -10.0;
        double testLongitude = 50.0;

        Location northern = mock( Location.class );
        when( northern.getLatitude() ).thenReturn( testLatitude );
        when( northern.getLongitude() ).thenReturn( testLongitude );
        when( northern.hasBearing() ).thenReturn( false );

        Location southern = mock( Location.class );
        when( southern.getLatitude() ).thenReturn( -30.0 );
        uut.trackedLocations.add( northern );
        uut.trackedLocations.add( southern );

        uut.northernmostPointLocationRequested();
        //wait for async calls
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();


        //other callbacks to view are covered by other test, as the same function is doing them, just chceck if that function got correct data
        ArgumentCaptor<CameraPosition> cameraPositionCaptor = ArgumentCaptor.forClass( CameraPosition.class );
        verify( mockView ).updateCamera( cameraPositionCaptor.capture() );

        assertThat( cameraPositionCaptor.getValue().target.latitude ).isEqualTo( testLatitude );

    }

    @Test
    public void testSouthernmostPointLocationRequested() {
        //mock the chain of rxlocation's calls
        Geocoding mockGeo = mock( Geocoding.class );
        Maybe<Address> mockAddressMaybe = Maybe.empty();
        when( mockGeo.fromLocation( any() ) ).thenReturn( mockAddressMaybe );
        when( this.mockLocation.geocoding() ).thenReturn( mockGeo );


        double testLatitude = 50.0;
        double testLongitude = 50.0;

        Location northern = mock( Location.class );
        when( northern.getLatitude() ).thenReturn( testLatitude );
        when( northern.getLongitude() ).thenReturn( testLongitude );
        when( northern.hasBearing() ).thenReturn( false );

        Location southern = mock( Location.class );
        when( southern.getLatitude() ).thenReturn( 60.0 );
        uut.trackedLocations.add( northern );
        uut.trackedLocations.add( southern );

        uut.southernmostPointLocationRequested();
        //wait for async calls
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();


        //other callbacks to view are covered by other test, as the same function is doing them, just chceck if that function got correct data
        ArgumentCaptor<CameraPosition> cameraPositionCaptor = ArgumentCaptor.forClass( CameraPosition.class );
        verify( mockView ).updateCamera( cameraPositionCaptor.capture() );

        assertThat( cameraPositionCaptor.getValue().target.latitude ).isEqualTo( testLatitude );

    }

    @Test
    public void testWesternmostPointLocationRequested() {
        //mock the chain of rxlocation's calls
        Geocoding mockGeo = mock( Geocoding.class );
        Maybe<Address> mockAddressMaybe = Maybe.empty();
        when( mockGeo.fromLocation( any() ) ).thenReturn( mockAddressMaybe );
        when( this.mockLocation.geocoding() ).thenReturn( mockGeo );


        double testLatitude = 50.0;
        double testLongitude = 50.0;

        Location western = mock( Location.class );
        when( western.getLatitude() ).thenReturn( testLatitude );
        when( western.getLongitude() ).thenReturn( testLongitude );
        when( western.hasBearing() ).thenReturn( false );

        Location eastern = mock( Location.class );
        when( eastern.getLongitude() ).thenReturn( 60.0 );
        uut.trackedLocations.add( western );
        uut.trackedLocations.add( eastern );

        uut.westernmostPointLocationRequested();
        //wait for async calls
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();


        //other callbacks to view are covered by other test, as the same function is doing them, just chceck if that function got correct data
        ArgumentCaptor<CameraPosition> cameraPositionCaptor = ArgumentCaptor.forClass( CameraPosition.class );
        verify( mockView ).updateCamera( cameraPositionCaptor.capture() );

        assertThat( cameraPositionCaptor.getValue().target.longitude ).isEqualTo( testLongitude );

    }

    @Test
    public void testEasternmostPointLocationRequested() {
        //mock the chain of rxlocation's calls
        Geocoding mockGeo = mock( Geocoding.class );
        Maybe<Address> mockAddressMaybe = Maybe.empty();
        when( mockGeo.fromLocation( any() ) ).thenReturn( mockAddressMaybe );
        when( this.mockLocation.geocoding() ).thenReturn( mockGeo );


        double testLatitude = 50.0;
        double testLongitude = 50.0;

        Location western = mock( Location.class );
        when( western.getLatitude() ).thenReturn( testLatitude );
        when( western.getLongitude() ).thenReturn( testLongitude );
        when( western.hasBearing() ).thenReturn( false );

        Location eastern = mock( Location.class );
        when( eastern.getLongitude() ).thenReturn( 10.0 );
        uut.trackedLocations.add( western );
        uut.trackedLocations.add( eastern );

        uut.easternmostPointLocationRequested();
        //wait for async calls
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();


        //other callbacks to view are covered by other tests, as the same function is doing them, just check if that function got correct data
        ArgumentCaptor<CameraPosition> cameraPositionCaptor = ArgumentCaptor.forClass( CameraPosition.class );
        verify( mockView ).updateCamera( cameraPositionCaptor.capture() );

        assertThat( cameraPositionCaptor.getValue().target.longitude ).isEqualTo( testLongitude );

    }

}