package ohgo.vptech.smarttraffic.main;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class SelectLanguageActivity extends AppCompatActivity {

    private ListView lv_language;
    private SharedPreferences settings;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_language);
        ActionBar ab = getSupportActionBar();
        Utilities.createCustomTitleActionbar(this,getResources().getString(R.string.title_select_langs),ab);

        lv_language = (ListView)findViewById(R.id.lv_language);
        List<String> langs = new ArrayList<>();
        langs.add(Constant.LANG_ENG);
        langs.add(Constant.LANG_VI);

        settings = PreferenceManager.getDefaultSharedPreferences(this);

        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_single_choice,langs);
        lv_language.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        lv_language.setAdapter(adapter);
        lv_language.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                SharedPreferences.Editor editor = settings.edit();
                switch (i){
                    case 0:
                        Utilities.setLocale(Constant.CODE_ENG,SelectLanguageActivity.this);
                        editor.putString(Constant.KEY_LIST_LANGS,Constant.CODE_ENG);
                        editor.commit();
                        break;
                    case 1:
                        Utilities.setLocale(Constant.CODE_VI,SelectLanguageActivity.this);
                        editor.putString(Constant.KEY_LIST_LANGS,Constant.CODE_VI);
                        editor.commit();
                        break;
                }

                Intent intent = new Intent(SelectLanguageActivity.this,LicenseActivity.class);
                startActivity(intent);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("log_in_success"));

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

}
