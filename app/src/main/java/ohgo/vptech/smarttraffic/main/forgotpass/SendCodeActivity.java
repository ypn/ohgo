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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.SignInUpActivity;
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

public class SendCodeActivity extends Original {
    private Button btn_send_code;
    private Toolbar toolbar_send_code;
    private EditText send_code_email_forgot,send_code_code_forgot,send_code_new_pass,send_code_new_confirm_pass;
    private RequestQueue queue;
    private Context mContext;
    private String email="";
    private ProgressDialog progressDialog;
    private Dialog dialog;
    private ViewGroup viewGroup;
    private Button continue_log_in;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_code);

        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                new IntentFilter("log_in_success"));

        btn_send_code = (Button)findViewById(R.id.btn_send_code);
        send_code_email_forgot = (EditText)findViewById(R.id.send_code_email_forgot);
        send_code_code_forgot = (EditText)findViewById(R.id.send_code_code_forgot);
        send_code_new_pass = (EditText)findViewById(R.id.send_code_new_pass);
        send_code_new_confirm_pass = (EditText)findViewById(R.id.send_code_new_confirm_pass);
        mContext = this;

        viewGroup = (ViewGroup) getWindow().getDecorView().getRootView();

        dialog = new Dialog(this,R.style.full_screen_dialog);
        dialog.setContentView(R.layout.reset_pass_success);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

        continue_log_in = (Button)dialog.findViewById(R.id.continue_log_in);

        progressDialog = new ProgressDialog(this,R.style.MyAlertDialog);
        progressDialog.setMessage(getString(R.string.waiting));
        progressDialog.setTitle(getString(R.string.send_email_dialog));
        if(getIntent().hasExtra(Constant.EXTRA_FORGOT_EMAIL)){
            email = getIntent().getStringExtra(Constant.EXTRA_FORGOT_EMAIL);
            send_code_email_forgot.setText(email);
            send_code_email_forgot.setFocusable(false);
        }

        queue = Volley.newRequestQueue(this);

        toolbar_send_code = (Toolbar)findViewById(R.id.toolbar_send_code);
        toolbar_send_code.setTitle("");

        setSupportActionBar(toolbar_send_code);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btn_send_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = send_code_email_forgot.getText().toString();
                final String code = send_code_code_forgot.getText().toString();
                final String password = send_code_new_pass.getText().toString();
                final String confirm_pass = send_code_new_confirm_pass.getText().toString();


                boolean valid_email = Utilities.checkEmail(send_code_email_forgot,getString(R.string.email_invalid));
                boolean valid_code = Utilities.checkNullInput(send_code_code_forgot, getResources().getString(R.string.must_entry_code), Constant.DEFAULT_LENGTH_INPUT, Constant.MAX_INPUT_LENGTH);

                String pass_not_match = getResources().getString(R.string.pass_not_match);
                String pattern_password = getResources().getString(R.string.pattern_password);

                Boolean validate_password = Utilities.checkPassword(send_code_new_pass, send_code_new_confirm_pass, pass_not_match, pattern_password);

                if (valid_email && valid_code && validate_password) {
                    btn_send_code.setEnabled(false);
                    progressDialog.show();
                    final StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.SERVER_GET_API + "resetPasswordsubmit", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            btn_send_code.setEnabled(true);
                            progressDialog.dismiss();
                            switch (response) {
                                case "2":
                                    Toast.makeText(mContext, getResources().getString(R.string.must_entry_email), Toast.LENGTH_SHORT).show();
                                    break;
                                case "3":
                                    Toast.makeText(mContext, getResources().getString(R.string.must_entry_code), Toast.LENGTH_SHORT).show();
                                    break;
                                case "4":
                                    Toast.makeText(mContext,getResources().getString(R.string.sign_in_password), Toast.LENGTH_SHORT).show();
                                    break;
                                case "5":
                                    Toast.makeText(mContext, getResources().getString(R.string.pattern_password), Toast.LENGTH_SHORT).show();
                                    break;
                                case "6":
                                    Toast.makeText(mContext, getResources().getString(R.string.pass_not_match) ,Toast.LENGTH_SHORT).show();
                                    break;
                                case "7":
                                    Toast.makeText(mContext, getResources().getString(R.string.email_not_available), Toast.LENGTH_SHORT).show();
                                    break;
                                case "8":
                                    Toast.makeText(mContext, getResources().getString(R.string.code_not_exist), Toast.LENGTH_SHORT).show();
                                    break;
                                case "9":
                                    Toast.makeText(mContext, getResources().getString(R.string.email_out_of_date), Toast.LENGTH_SHORT).show();
                                    break;
                                case "0":
                                   Blurry.with(SendCodeActivity.this).radius(15).sampling(2).onto(viewGroup);
                                    dialog.show();
                                    break;

                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            btn_send_code.setEnabled(true);
                            progressDialog.dismiss();
                            Toast.makeText(mContext,getResources().getString(R.string.cannot_connect_server),Toast.LENGTH_SHORT).show();
                        }
                    }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<>();
                            params.put("email", email);
                            params.put("code", code);
                            params.put("password", password);
                            params.put("retype_password", confirm_pass);
                            return params;
                        }
                    };

                    Handler h  = new Handler();
                    h.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            queue.add(stringRequest);
                        }
                    },500);
                }
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Blurry.delete(viewGroup);
            }
        });

        toolbar_send_code.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        continue_log_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            startActivity(new Intent
                    (SendCodeActivity.this, SignInUpActivity.class)
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
