package ohgo.vptech.smarttraffic.main.modules.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;

/**
 * Created by ypn on 11/11/2016.
 * Request location by GoogleApiClient that will save battery
 */
public class RequestFulseLocation implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Context context;
    private Activity activity;
    private Location loc;
    private OnRequestLocationListener onRequestLocationListener;

    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    public RequestFulseLocation(Context context,OnRequestLocationListener onRequestLocationListener) {
        this.context = context;
        try{
            this.activity = (Activity)context;
        }catch (ClassCastException ex){
            Log.e("Cast context","Context from service");
        }

        this.onRequestLocationListener = onRequestLocationListener;
        buildGoogleApiClient();
        googleApiClient.connect();

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(60 * Constant.MILIS);
        locationRequest.setFastestInterval(60 * Constant.MILIS);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(activity!=null){
                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constant.CODE_REQUEST_FINE_LOC);
            }
            onRequestLocationListener.checkPermissionFails();
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        loc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if(loc!=null){
            onRequestLocationListener.getLocationSuccess(loc);
        }else{
            onRequestLocationListener.locationTurnOff();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        onRequestLocationListener.getLocationFails();
    }

    @Override
    public void onLocationChanged(Location location) {
        loc = location;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        onRequestLocationListener.getLocationFails();
    }

}
