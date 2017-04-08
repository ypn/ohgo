package ohgo.vptech.smarttraffic.main.reporter;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ohgo.vptech.smarttraffic.main.modules.background.BackgroundService;

/**
 * Created by ypn on 10/14/2016.
 */
public class BootBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, BackgroundService.class));
    }
}
