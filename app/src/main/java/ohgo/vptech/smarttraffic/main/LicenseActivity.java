package ohgo.vptech.smarttraffic.main;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;

import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class LicenseActivity extends AppCompatActivity {

    private Button btn_accept;
    private TextView tv_lisence;
    private RequestQueue queue;
    private CardView cardview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_license);
        getPrivary();

        ActionBar ab = getSupportActionBar();
        Utilities.createCustomTitleActionbar(this,getResources().getString(R.string.title_license),ab);

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        upArrow.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeAsUpIndicator(upArrow);

        btn_accept = (Button)findViewById(R.id.btn_accept);
        tv_lisence = (TextView)findViewById(R.id.tv_lisence);
        cardview = (CardView)findViewById(R.id.cardview);

        btn_accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LicenseActivity.this,SignInUpActivity.class);
                startActivity(intent);
            }
        });

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("log_in_success"));

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    private  void getPrivary(){
        queue = Volley.newRequestQueue(this);
        SharedPreferences setting = PreferenceManager.getDefaultSharedPreferences(this);

        String code_lang = setting.getString(Constant.KEY_LIST_LANGS,"");
        String lang ="eng";

        if(code_lang.equals(Constant.CODE_VI)){
            lang ="vi";
        }

        StringRequest stringRequest = new StringRequest(Request.Method.GET, Constant.SERVER_GET_API + "api_get_privacy"+ "/" + lang, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                tv_lisence.setText(Html.fromHtml(response));
                cardview.setVisibility(View.VISIBLE);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });

        queue.add(stringRequest);
    }


    @Override
    protected void onStop() {
        super.onStop();
        queue.stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            this.finish();
        }
        return true;
    }
}
