package ohgo.vptech.smarttraffic.main.modules.jobscheduler;

import android.app.NotificationManager;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

import java.net.URISyntaxException;

import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;

/**
 * Created by ypn on 11/8/2016.
 */
public class JobServiceBackground extends JobService implements OnReportListener {

    private Socket mSocket;
    private NewReportHandler reportHandler;
    private static int ID_NOTICE = 99;

    {
        try {
            mSocket = IO.socket(Constant.SERVER_REALTIME_URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private Emitter.Listener handlerNewReportMessage = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
           reportHandler.commingMessageHandler((JSONObject)args[0]);
        }
    };

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e("START SERVICE","SUCCESSFUL");
        mSocket.connect();
        reportHandler = new NewReportHandler(this,this);
        new JobServiceTask().execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e("STOP SERVICE","Service was stopped by:" + params.toString());
        return false;
    }

    @Override
    public void onReceiveReportSuccess(NotificationCompat.Builder builder) {
        NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(ID_NOTICE,builder.build());
    }

    private class JobServiceTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            mSocket.on(Constant.TAG_LISTEN_REPORT,handlerNewReportMessage);
            Log.e("SocketIO","start listening...");
            return null;
        }
    }
}
