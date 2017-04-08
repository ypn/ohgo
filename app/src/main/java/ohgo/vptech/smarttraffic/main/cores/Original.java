package ohgo.vptech.smarttraffic.main.cores;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ohgo.vptech.smarttraffic.main.entities.Users;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;

import java.util.Locale;

/**
 * Created by ypn on 9/21/2016.
 */
public class Original extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    protected static Users currentUser ;
    private Tracker mTracker;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        restartActivity();
    }

    private void restartActivity() {
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }


    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName(this.getLocalClassName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("event_logout"));
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };


    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        String lang = PreferenceManager.getDefaultSharedPreferences(this).getString(Constant.KEY_LIST_LANGS, "VI");
        Configuration config = getResources().getConfiguration();
        config.locale = new Locale(lang);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }

}
