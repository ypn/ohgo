package ohgo.vptech.smarttraffic.main.modules.background;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;

import ohgo.vptech.smarttraffic.main.modules.jobscheduler.NewReportHandler;
import ohgo.vptech.smarttraffic.main.modules.jobscheduler.OnReportListener;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;

/**
 * Created by ypn on 11/16/2016.
 */
public class BackgroundService extends Service implements OnReportListener {

    private NewReportHandler reportHandler;
    private final int ID_NOTICE = 99;
    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(Constant.SERVER_REALTIME_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off(Constant.TAG_LISTEN_REPORT);
        mSocket.close();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        reportHandler = new NewReportHandler(this,this);
        mSocket.connect();
        mSocket.on(Constant.TAG_LISTEN_REPORT,handlerNewReportMessage);
    }

    private Emitter.Listener handlerNewReportMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            reportHandler.commingMessageHandler((JSONObject)args[0]);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onReceiveReportSuccess(NotificationCompat.Builder builder) {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(ID_NOTICE,builder.build());
    }
}
