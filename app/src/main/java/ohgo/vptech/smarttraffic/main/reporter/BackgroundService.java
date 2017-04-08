package ohgo.vptech.smarttraffic.main.reporter;

import android.Manifest;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import ohgo.vptech.smarttraffic.main.utilities.constants.ReportField;
import ohgo.vptech.smarttraffic.main.entities.ShortUsers;

import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.adapters.MarkerReport;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import ohgo.vptech.smarttraffic.main.utilities.constants.UserField;
import android.widget.ImageView;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;


import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.net.URISyntaxException;

/**
 * Created by ypn on 9/23/2016.
 */
public class BackgroundService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private Context mContext;
    private Socket mSocket;
    private ShortUsers shortUsers;

    private Location crrLoc ;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private SharedPreferences setting;
    private String android_id ;


    private final String KEY_NOTICE_NEW_MESSAGE_RINGTONE = "notifications_new_message_ringtone";
    private final String KEY_NOTICE_NEW_MESSAGE = "notifications_new_message";
    private final String KEY_ENABLE_VIBRATE = "notifications_new_message_vibrate";
    private final String DEFAULT_RING_NOTICE = "content://settings/system/notification_sound";

    {
        try {
            mSocket = IO.socket(Constant.SERVER_REALTIME_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY ;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.off(Constant.TAG_LISTEN_REPORT);
        mSocket.close();
        mSocket.disconnect();
        googleApiClient.disconnect();

    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        buildGoogleApiClient();
        googleApiClient.connect();

        mContext = this;
        android_id = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        mSocket.connect();
        setting = getSharedPreferences(Constant.CODE_PREFERENCE_SETTING,0);

        shortUsers = new ShortUsers();
        try {
            JSONObject currentUser = new JSONObject(setting.getString("user",null));
            if(currentUser!=null){
                shortUsers.set_id(currentUser.getInt(UserField.ID));
                shortUsers.setFirstName(currentUser.getString(UserField.FIRST_NAME));
                shortUsers.setLastName("");
                shortUsers.setUrlImageProfile(new JSONObject(currentUser.getString(UserField.IMAGES)).getString(UserField.AVATAR_URL));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
               ){
            crrLoc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient,locationRequest,this);

            mSocket.on(Constant.TAG_LISTEN_REPORT,handlerNewReportMessage);
        }
    }


    @Override
    public void onConnectionSuspended(int  i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        crrLoc = location;
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private Emitter.Listener handlerNewReportMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            if(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(KEY_NOTICE_NEW_MESSAGE,true) && crrLoc!=null){
                int range = setting.getInt(Constant.CODE_RANGE,5000);

                final RequestQueue queue = Volley.newRequestQueue(mContext);
                final NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
                mBuilder.setSmallIcon(R.mipmap.ic_launcher);
                mBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher));

                Intent resultIntent = new Intent(BackgroundService.this, ReportDetailActivity.class);
                Uri uri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(mContext).
                        getString(KEY_NOTICE_NEW_MESSAGE_RINGTONE, DEFAULT_RING_NOTICE));
                mBuilder.setSound(uri);
                mBuilder.setAutoCancel(true);

                if(PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean(KEY_ENABLE_VIBRATE,true)){
                    mBuilder.setVibrate(new long[] { 0 ,1000,1000 });
                }

                JSONObject data = (JSONObject)args[0];

                try{
                    Double latitue = data.getDouble(ReportField.RP_LATTITUE);
                    Double longtitue = data.getDouble(ReportField.RP_LONGTITUE);
                    String socket_id = data.has(ReportField.RP_MARKER_ID)?data.getString(ReportField.RP_MARKER_ID):"";

                    float[] result = new float[1];
                    Location.distanceBetween(
                            crrLoc.getLatitude(),
                            crrLoc.getLongitude(),
                            latitue,
                            longtitue,
                            result
                    );

                    if(result[0] < range && !android_id.equals(socket_id)){
                        int state_code = data.getInt(ReportField.RP_STATE);
                        final String address = data.has(ReportField.RP_ADDRESS)?data.getString(ReportField.RP_ADDRESS):"unknown address";
                        final String imgurl = data.getString(ReportField.RP_IMG_URL);
                        final String state = Utilities.getStateReport(mContext,state_code);

                        mBuilder.setContentTitle(state);
                        mBuilder.setContentText(address);


                        MarkerReport markerReport = new MarkerReport(
                                latitue,
                                longtitue,
                                state,
                                data.toString(),R.drawable.location
                        );

                        String reportData,jsonCurrentUser;
                        Gson gson = new Gson();


                        Type type = new TypeToken<MarkerReport>(){}.getType();
                        reportData = gson.toJson(markerReport,type);
                        resultIntent.putExtra("REPORT_DATA",reportData);

                        Type type1 = new TypeToken<ShortUsers>(){}.getType();
                        jsonCurrentUser = gson.toJson(shortUsers,type1);

                        resultIntent.putExtra("currentUser",jsonCurrentUser);
                        resultIntent.putExtra("ypn_service","notice");

                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(BackgroundService.this);

                        stackBuilder.addParentStack(ReportDetailActivity.class);

                        stackBuilder.addNextIntent(resultIntent);
                        PendingIntent resultPendingIntent =
                                stackBuilder.getPendingIntent(
                                        0,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );

                        mBuilder.setContentIntent(resultPendingIntent);

                        if(!imgurl.equals("")){
                            ImageRequest evidence = new ImageRequest(Constant.SERVER_REALTIME_URL + imgurl ,
                                    new Response.Listener<Bitmap>() {
                                        @Override
                                        public void onResponse(Bitmap response) {
                                            mBuilder.setStyle(new NotificationCompat.BigPictureStyle()
                                                    .bigPicture(response)
                                                    .setSummaryText(address)
                                                    .setBigContentTitle(state)
                                            );

                                            NotificationManager mNotificationManager =
                                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            mNotificationManager.notify(1000, mBuilder.build());
                                        }
                                    }, 1000, 1000, ImageView.ScaleType.CENTER, Bitmap.Config.ALPHA_8,
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            NotificationManager mNotificationManager =
                                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                            mNotificationManager.notify(1000, mBuilder.build());
                                        }
                                    });

                            queue.add(evidence);
                        }

                        else{
                            NotificationManager mNotificationManager =
                                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            mNotificationManager.notify(1000, mBuilder.build());
                        }

                    }

                }catch (JSONException ex){
                    ex.printStackTrace();
                }

            }

        }
    };

}
