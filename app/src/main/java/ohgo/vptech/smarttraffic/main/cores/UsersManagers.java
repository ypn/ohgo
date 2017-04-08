package ohgo.vptech.smarttraffic.main.cores;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import ohgo.vptech.smarttraffic.main.entities.UserRegister;
import ohgo.vptech.smarttraffic.main.MapsActivity;
import ohgo.vptech.smarttraffic.main.R;

import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ypn on 7/29/2016.
 */
public  class UsersManagers extends Original{
    private Context mContext;
    private RequestQueue queue;
    private static final String URL_SERVER = "http://apitraffic.vptechgroup.com/";

    //Param form register and login post to server
    private static final String POST_PARAM_EMAIL = "email";
    private static final String POST_PARAM_FIRST_NAME ="first_name";
    private static final String POST_PARAM_LAST_NAME   ="last_name";
    private static final String POST_PARAM_PASSWORD ="password";
    private static final String POST_PARAM_RETYPE_PASSWORD ="retype_password";
    private static final String POST_PARAM_AVATAR ="avata";

    public UsersManagers(Context context){
        this.mContext = context;
        queue = Volley.newRequestQueue(mContext);
    }
    public void registerUser(final UserRegister userRegister, final Button signup){
        final ProgressDialog progressDialog = new ProgressDialog(mContext,R.style.MyAlertDialog);
        progressDialog.setTitle("Sign up");
        progressDialog.setMessage("Please wait...");
        progressDialog.show();

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_SERVER + "api_register", new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                progressDialog.dismiss();
                signup.setEnabled(true);

                switch (response.toString()){
                    case "1":
                        ViewPager viewPager = (ViewPager)((AppCompatActivity)mContext).findViewById(R.id.pager);
                        viewPager.setCurrentItem(0);
                        EditText ed = (EditText) ((AppCompatActivity)mContext).findViewById(R.id.input_user_name);
                        ed.setText(userRegister.getPhone());
                        Toast.makeText(mContext,mContext.getResources().getString(R.string.sign_up_success),Toast.LENGTH_LONG).show();
                        break;
                    case "3":
                        Toast.makeText(mContext,mContext.getResources().getString(R.string.phone_exist),Toast.LENGTH_LONG).show();
                        break;
                    default:
                        Toast.makeText(mContext,mContext.getResources().getString(R.string.reg_error),Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                signup.setEnabled(true);
                Toast.makeText(mContext,mContext.getResources().getString(R.string.check_network),Toast.LENGTH_LONG).show();

            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put(POST_PARAM_EMAIL,userRegister.getPhone());
                params.put(POST_PARAM_FIRST_NAME,userRegister.getFirst_name());
                params.put(POST_PARAM_PASSWORD,userRegister.getPassword());
                params.put(POST_PARAM_RETYPE_PASSWORD,userRegister.getRetype_password());
                params.put(POST_PARAM_AVATAR,userRegister.getEncodeImage());
                params.put(POST_PARAM_LAST_NAME,"");
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

    public void signin(final String username, final String password, final ProgressBar progressBar){
        StringRequest stringRequest = new StringRequest(Request.Method.POST,URL_SERVER + "api_login",new Response.Listener<String>(){
            @Override
            public void onResponse(String response) {
                progressBar.setVisibility(View.GONE);
                if(!response.equals("0")){
                    Intent intent = new Intent(mContext, MapsActivity.class);
                    intent.putExtra("user",response);
                    SharedPreferences settings = mContext.getSharedPreferences("LANGS",0);
                    SharedPreferences.Editor editor = settings.edit();
                    editor.putBoolean(Constant.CODE_FIRST_TIME,true);
                    editor.putString("user",response);
                    editor.commit();
                    mContext.startActivity(intent);

                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                    final String LOG_IN_SUCCESS = "log_in_success";
                    Intent intentK = new Intent(LOG_IN_SUCCESS);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intentK);

                }else{
                    Toast.makeText(mContext,mContext.getResources().getString(R.string.sign_in_failure),Toast.LENGTH_LONG).show();
                }
            }
            },new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(mContext,mContext.getResources().getString(R.string.check_network),Toast.LENGTH_LONG).show();
                }
            }){
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String,String> params  = new HashMap<>();
                    params.put("email",username);
                    params.put("password",password);
                    return  params;
                }
            };
            queue.add(stringRequest);

    }

    public void updateUser(final UserRegister userRegister){

        final ProgressDialog progressDialog = new ProgressDialog(mContext,R.style.MyAlertDialog);
        progressDialog.setTitle("Update information");
        progressDialog.setMessage(mContext.getString(R.string.waiting));
        progressDialog.show();

        final StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.SERVER_GET_API +"api_update_profile/" + userRegister.get_id(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                progressDialog.dismiss();
                switch (response){
                    case "0":
                        Toast.makeText(mContext,"Not found this user.",Toast.LENGTH_LONG).show();
                        break;
                    case "2":
                        Toast.makeText(mContext,"Empty email",Toast.LENGTH_LONG).show();
                        break;
                    case "3":
                        Toast.makeText(mContext,"Format email invalid",Toast.LENGTH_LONG).show();
                        break;
                    case "4":
                        Toast.makeText(mContext,"Require name",Toast.LENGTH_LONG).show();
                        break;
                    case "5":
                        Toast.makeText(mContext,"Your name is too long",Toast.LENGTH_LONG).show();
                        break;
                    case "6":
                        Toast.makeText(mContext,"Format name invalid",Toast.LENGTH_LONG).show();
                        break;
                    case "10":
                        Toast.makeText(mContext,"Require password",Toast.LENGTH_LONG).show();
                        break;
                    case "11":
                        Toast.makeText(mContext,"Password has to have at least 6 charaters",Toast.LENGTH_LONG).show();
                        break;
                    case "12":
                        Toast.makeText(mContext,"Confirm password not match.",Toast.LENGTH_LONG).show();
                        break;
                    default:
                        try {
                            //save user like cookie
                            SharedPreferences settings = mContext.getSharedPreferences("LANGS",0);
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean(Constant.CODE_FIRST_TIME,true);
                            editor.putString("user",response);
                            editor.commit();

                            JSONObject user = new JSONObject(response);
                            currentUser.set_firstName(user.getString("first_name"));
                            currentUser.set_phoneNumber(user.getString("phone"));
                            currentUser.set_urlProfileImage(new JSONObject(user.getString("images")).getString("url"));
                            ImageRequest imageRequest = new ImageRequest(Constant.SERVER_GET_API + "upload/" + currentUser.get_urlProfileImage(), new Response.Listener<Bitmap>() {
                                @Override
                                public void onResponse(Bitmap response) {
                                    currentUser.set_profileImage(response);
                                }
                            }, 100, 100, ImageView.ScaleType.CENTER, Bitmap.Config.ALPHA_8, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            });

                            queue.add(imageRequest);

                            Toast.makeText(mContext,"Update information successful",Toast.LENGTH_SHORT).show();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(mContext,error.toString(),Toast.LENGTH_LONG).show();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                params.put("email",userRegister.getPhone());
                params.put("first_name",userRegister.getFirst_name());
                params.put("phone",userRegister.getReal_phone());
                params.put("notice","0");

                if(!userRegister.getPassword().equals("")){
                    params.put("password",userRegister.getPassword());
                    params.put("retype_password",userRegister.getRetype_password());
                }
                if(userRegister.getEncodeImage()!=null){
                    params.put("avata",userRegister.getEncodeImage());
                }

                if(userRegister.isReceiveNotice()){
                    params.put("notice","1");
                }

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
