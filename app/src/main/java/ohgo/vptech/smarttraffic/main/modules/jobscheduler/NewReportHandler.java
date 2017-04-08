package ohgo.vptech.smarttraffic.main.modules.jobscheduler;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;

import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.adapters.MarkerReport;
import ohgo.vptech.smarttraffic.main.entities.ShortUsers;
import ohgo.vptech.smarttraffic.main.modules.location.OnRequestLocationListener;
import ohgo.vptech.smarttraffic.main.modules.location.RequestFulseLocation;
import ohgo.vptech.smarttraffic.main.reporter.ReportDetailActivity;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import ohgo.vptech.smarttraffic.main.utilities.constants.ReportField;
import ohgo.vptech.smarttraffic.main.utilities.constants.UserField;

/**
 * Logic layer - present
 * Created by ypn on 11/11/2016.
 */
public class NewReportHandler implements OnRequestLocationListener {
    private OnReportListener onReportListener;
    private SharedPreferences df_setting,own_setting;
    private Context context;
    private RequestQueue queue;
    private ShortUsers shortUsers;

    private int rangeNotice;
    private String android_id;
    private Location deviceLoc;

    private final String KEY_NOTICE_NEW_MESSAGE_RINGTONE = "notifications_new_message_ringtone";
    private final String KEY_NOTICE_NEW_MESSAGE = "notifications_new_message";
    private final String KEY_ENABLE_VIBRATE = "notifications_new_message_vibrate";
    private final String DEFAULT_RING_NOTICE = "content://settings/system/notification_sound";
    private final int DEFAULT_RANGE = 5000;

    public  NewReportHandler(Context context,OnReportListener onReportListener){
        this.onReportListener = onReportListener;
        this.context = context;
        queue = Volley.newRequestQueue(context);
        own_setting = context.getSharedPreferences(Constant.CODE_PREFERENCE_SETTING,0);
        android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        new RequestFulseLocation(context,this);

        shortUsers = new ShortUsers();
        try {
            JSONObject currentUser = new JSONObject(own_setting.getString("user",null));
            if(currentUser!=null){
                shortUsers.set_id(currentUser.getInt(UserField.ID));
                shortUsers.setFirstName(currentUser.getString(UserField.FIRST_NAME));
                shortUsers.setLastName("");
                shortUsers.setUrlImageProfile(new JSONObject(currentUser.getString(UserField.IMAGES)).getString(UserField.AVATAR_URL));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void commingMessageHandler(JSONObject data){
        //when receive report and distance between report and private location is smaller than range in setting.
        df_setting = PreferenceManager.getDefaultSharedPreferences(context);
        try {
            Double lat = data.getDouble(ReportField.RP_LATTITUE);
            Double lng = data.getDouble(ReportField.RP_LONGTITUE);
            String device_id = data.has(ReportField.RP_MARKER_ID)?data.getString(ReportField.RP_MARKER_ID):"";
            if(device_id != android_id && df_setting.getBoolean(KEY_NOTICE_NEW_MESSAGE,true) /*&& isInRange(deviceLoc,lat,lng)*/){
                int state_code = data.getInt(ReportField.RP_STATE);
                final  String address = data.has(ReportField.RP_ADDRESS)?data.getString(ReportField.RP_ADDRESS):"unknown address";
                final String state = Utilities.getStateReport(context,state_code);
                String imgurl = data.getString(ReportField.RP_IMG_URL);

                MarkerReport markerReport = new MarkerReport(
                        lat,
                        lng,
                        state,
                        data.toString(),R.drawable.location
                );

                final NotificationCompat.Builder builder = new NotificationCompat.Builder(context);


                builder.setContentIntent(nextIntent(markerReport));

                builder.setSmallIcon(R.mipmap.ic_launcher);
                builder.setLargeIcon(BitmapFactory.decodeResource(context.getResources(),R.mipmap.ic_launcher));

                Uri uri = Uri.parse(PreferenceManager.getDefaultSharedPreferences(context).
                        getString(KEY_NOTICE_NEW_MESSAGE_RINGTONE, DEFAULT_RING_NOTICE));
                builder.setSound(uri);
                builder.setAutoCancel(true);

                if(PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY_ENABLE_VIBRATE,true)){
                    builder.setVibrate(new long[] { 0 ,1000,1000 });
                }

                builder.setContentTitle(state);
                builder.setContentText(address);

                if(!imgurl.equals("")){
                    ImageRequest evidence = new ImageRequest(Constant.SERVER_REALTIME_URL + imgurl ,
                            new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    builder.setStyle(new NotificationCompat.BigPictureStyle()
                                            .bigPicture(response)
                                            .setSummaryText(address)
                                            .setBigContentTitle(state)
                                    );

                                    onReportListener.onReceiveReportSuccess(builder);
                                }
                            }, 1000, 1000, ImageView.ScaleType.CENTER, Bitmap.Config.ALPHA_8,
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    onReportListener.onReceiveReportSuccess(builder);
                                }
                            });

                    queue.add(evidence);
                }
                else {
                    onReportListener.onReceiveReportSuccess(builder);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     *  Check report location is inside range receive report or not?
     *  auth:ypn
    */
    private boolean isInRange(Location src,Double lat_desc,double lng_desc){
        boolean _check = false;
        if(src != null){
            rangeNotice = df_setting.getInt(Constant.CODE_RANGE,DEFAULT_RANGE);
            float[] result = new float[1];
            Location.distanceBetween(
                    src.getLatitude(),
                    src.getLongitude(),
                    lat_desc,
                    lng_desc,
                    result
            );

            if(result[0] < rangeNotice) _check = true;
        }

        return  _check;

    }

    private PendingIntent nextIntent(MarkerReport markerReport){
        Intent resultIntent = new Intent(context, ReportDetailActivity.class);

        String reportData,jsonCurrentUser;
        Gson gson = new Gson();


        Type type = new TypeToken<MarkerReport>(){}.getType();
        reportData = gson.toJson(markerReport,type);
        resultIntent.putExtra("REPORT_DATA",reportData);

        Type type1 = new TypeToken<ShortUsers>(){}.getType();
        jsonCurrentUser = gson.toJson(shortUsers,type1);

        resultIntent.putExtra("currentUser",jsonCurrentUser);
        resultIntent.putExtra("ypn_service","notice");

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);

        stackBuilder.addParentStack(ReportDetailActivity.class);

        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        return  resultPendingIntent;
    }

    @Override
    public void getLocationSuccess(Location location) {
        Log.e("Request location","Success get location");
        deviceLoc = location;
    }

    @Override
    public void locationTurnOff() {
        Log.e("Request location","Location turn off");
    }

    @Override
    public void getLocationFails() {
        Log.e("Request location","false");
    }

    @Override
    public void checkPermissionFails() {
        Log.e("Request location","Check permission false");
    }
}
