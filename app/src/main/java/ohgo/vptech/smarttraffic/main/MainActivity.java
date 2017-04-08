package ohgo.vptech.smarttraffic.main;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.style.UpdateLayout;
import android.util.Log;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ohgo.vptech.smarttraffic.main.cores.AnalyticsApplication;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;

public class MainActivity extends AppCompatActivity {
    private SharedPreferences setting, default_setting ;
    private Tracker mTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen_activity);
        default_setting = PreferenceManager.getDefaultSharedPreferences(this);

        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();
        mTracker.setScreenName(this.getLocalClassName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        if(default_setting.contains(Constant.KEY_LIST_LANGS)){
            String lang = default_setting.getString(Constant.KEY_LIST_LANGS,"VI");
            Utilities.setLocale(lang,this);
        }

        setting = getSharedPreferences(Constant.CODE_PREFERENCE_SETTING,0);

        final boolean first = setting.getBoolean(Constant.CODE_FIRST_TIME,false);
        Handler h = new Handler();

        h.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!first) {
                    Intent act_lang = new Intent(MainActivity.this, SelectLanguageActivity.class);
                    startActivity(act_lang);
                }else{
                    startActivity(new Intent(MainActivity.this,MapsActivity.class));
                }

                finish();
            }
        },500);

    }

}
