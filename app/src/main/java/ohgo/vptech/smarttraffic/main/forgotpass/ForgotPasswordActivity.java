package ohgo.vptech.smarttraffic.main.forgotpass;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.cores.Original;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

import jp.wasabeef.blurry.Blurry;

public class ForgotPasswordActivity extends Original {

    private Button btn_send_email;
    private ViewGroup  viewGroup;
    private Dialog dialog;
    private Toolbar mToolbar;
    private Button btn_send_user_email;
    private EditText edt_email_forgot;
    private RequestQueue queue;
    private String email;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("log_in_success"));

        queue = Volley.newRequestQueue(this);

        btn_send_email = (Button)findViewById(R.id.btn_send_email);
        viewGroup = (ViewGroup) getWindow().getDecorView().getRootView();
        dialog = new Dialog(ForgotPasswordActivity.this,R.style.full_screen_dialog);
        dialog.setContentView(R.layout.check_forgot_email);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        btn_send_user_email = (Button) dialog.findViewById(R.id.btn_send_user_email);
        edt_email_forgot = (EditText)findViewById(R.id.edt_email_forgot);
        progressDialog = new ProgressDialog(this,R.style.MyAlertDialog);
        progressDialog.setMessage(getString(R.string.waiting));
        progressDialog.setTitle(getString(R.string.send_email_dialog));


        mToolbar = (Toolbar)findViewById(R.id.forgot_pass_toolbar);
        mToolbar.setTitle("");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btn_send_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!edt_email_forgot.getText().toString().equals("")){
                    if(Utilities.checkEmail(edt_email_forgot,getString(R.string.email_invalid))){
                        btn_send_email.setEnabled(false);
                        email = edt_email_forgot.getText().toString();
                        progressDialog.show();
                        final StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.SERVER_GET_API + "resetPassword", new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                btn_send_email.setEnabled(true);
                                progressDialog.dismiss();
                                switch (response){
                                    case "0":
                                        dialog.show();
                                        Blurry.with(ForgotPasswordActivity.this).radius(15).sampling(2).onto(viewGroup);
                                        break;
                                    case "1":
                                        Toast.makeText(ForgotPasswordActivity.this,getResources().getString(R.string.must_entry_email),Toast.LENGTH_SHORT).show();
                                        break;
                                    case "2":
                                        Toast.makeText(ForgotPasswordActivity.this,getResources().getString(R.string.email_invalid),Toast.LENGTH_SHORT).show();
                                        break;
                                    case "3":
                                        Toast.makeText(ForgotPasswordActivity.this,getResources().getString(R.string.email_not_available),Toast.LENGTH_SHORT).show();
                                        break;
                                    case "4":
                                        Toast.makeText(ForgotPasswordActivity.this,getResources().getString(R.string.cannot_contact_admin),Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                btn_send_email.setEnabled(true);
                                progressDialog.dismiss();
                                Toast.makeText(ForgotPasswordActivity.this,getResources().getString(R.string.cannot_connect_server),Toast.LENGTH_SHORT).show();
                            }
                        }){
                            @Override
                            protected Map<String, String> getParams() throws AuthFailureError {
                                Map<String,String> params = new HashMap<>();
                                params.put("email",edt_email_forgot.getText().toString());
                                return params;
                            }
                        };

                        Handler h = new Handler();
                        h.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                queue.add(stringRequest);
                            }
                        },500);

                    }
                }

            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Blurry.delete(viewGroup);
            }
        });

        btn_send_user_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent
                        (ForgotPasswordActivity.this,SendCodeActivity.class)
                        .putExtra(Constant.EXTRA_FORGOT_EMAIL,email)
                );
            }
        });

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

}

