package ohgo.vptech.smarttraffic.main.modules.jobscheduler;

import android.support.v4.app.NotificationCompat;

import org.json.JSONObject;

/**
 * Created by ypn on 11/11/2016.
 */
public interface OnReportListener {
    void onReceiveReportSuccess(NotificationCompat.Builder builder);
}
