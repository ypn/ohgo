package ohgo.vptech.smarttraffic.main.reporter;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;


import android.provider.Settings;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import ohgo.vptech.smarttraffic.main.entities.Markers;
import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.cores.BaseActivity;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import ohgo.vptech.smarttraffic.main.utilities.constants.ReportField;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.clans.fab.FloatingActionMenu;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import com.google.android.gms.location.places.Place;

import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import de.hdodenhof.circleimageview.CircleImageView;

public class ReportPoliceActivity extends BaseActivity implements View.OnClickListener,StateReport {

    private ImageView img_report_capture,img_report_comment,img_report_place,img_capture;
    private TextView report_location_name,report_location_address,report_add_comment,tv_report_title;
    private LinearLayout ln_location;
    RelativeLayout ln_comment;
    private FloatingActionMenu menu;
    private TextView tv_user_name;
    private CircleImageView user_avatar;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PLACE_PICKER_REQUEST = 2;
    private static final int REQUEST_ADD_COMMENT =3;
    private String titleToolbar = "Report";
    private Markers marker;
    private Context mContext;
    private Bundle extra;
    private ProgressDialog progress;
    private RequestQueue queue;
    private String android_id ;

    private JSONObject jsonObject;//Object store value to report

    private Socket mSocket;
    {
        try{
            mSocket = IO.socket(Constant.SERVER_REALTIME_URL);
        }catch (URISyntaxException ex){
            throw new RuntimeException(ex);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        android_id = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        extra = getIntent().getExtras();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        FragmentReportPolice fragmentReportPolice = new FragmentReportPolice();

        Bundle bundle = new Bundle();
        bundle.putInt("type",extra.getInt("type"));
        fragmentTransaction.add(R.id.choose_state,fragmentReportPolice);
        fragmentReportPolice.setArguments(bundle);
        fragmentTransaction.commit();

        switch (extra.getInt("type")){
            case Constant.REPORT_TYPE_POLICE:
                titleToolbar = getResources().getString(R.string.title_activity_report_police);
                break;
            case Constant.REPORT_TYPE_CAMERA:
                titleToolbar = getResources().getString(R.string.title_activity_report_cam);
                break;
            case Constant.REPORT_TYPE_TRAFFIC_JAM:
                titleToolbar = getResources().getString(R.string.title_activity_report_trafic);
                break;
            case Constant.REPORT_TYPE_ACCIDENT:
                titleToolbar = getResources().getString(R.string.title_activity_report_accident);
                break;
        }


        setContentView(R.layout.activity_report_police);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tv_report_title = (TextView)toolbar.findViewById(R.id.tv_report_title);
        tv_report_title.setText(titleToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        marker = new Markers();
        marker.setType(extra.getInt("type"));

        if(crrLoc!=null){
            marker.setLatLng(new LatLng(crrLoc.getLatitude(),crrLoc.getLongitude()));
        }

        progress = new ProgressDialog(mContext,R.style.MyAlertDialog);
        progress.setTitle(getString(R.string.send_report_dialog));
        progress.setMessage(getString(R.string.waiting));

        //Addition information for report
        img_report_comment = (ImageView)findViewById(R.id.img_report_comment);
        img_report_comment.setOnClickListener(this);

        img_report_capture =(ImageView)findViewById(R.id.img_report_capture);
        img_report_capture.setOnClickListener(this);

        img_report_place =(ImageView)findViewById(R.id.img_report_place);
        img_report_place.setOnClickListener(this);

        img_capture = (ImageView)findViewById(R.id.img_capture);

        menu = (FloatingActionMenu)findViewById(R.id.menu);


        jsonObject = new JSONObject();
        //initializeDefaultMarker();

//        getAddressFromLatLng(this);

        //Show addition information
        report_location_address = (TextView)findViewById(R.id.report_location_adress);
        report_location_name =(TextView)findViewById(R.id.report_location_name);
        report_add_comment =(TextView)findViewById(R.id.report_add_comment);

        mSocket.connect();
        mSocket.on("report_success",handlerReportSuccess);

    }

    private Emitter.Listener handlerReportSuccess = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progress.dismiss();
                    if(args[0].toString().equals("0")){
                        Toast.makeText(ReportPoliceActivity.this,getString(R.string.pos_reported),Toast.LENGTH_SHORT).show();
                        menu.open(true);
                    }else{
                        JSONObject pos = (JSONObject)args[0];
                        Toast.makeText(ReportPoliceActivity.this,getResources().getString(R.string.thank_for_report),Toast.LENGTH_SHORT).show();
                        Intent output = new Intent();
                        output.putExtra(Constant.EXTRA_REPORT_COMPLETE,pos.toString());
                        setResult(RESULT_OK,output);
                        finish();
                    }
                }
            });
        }
    };


    @Override
    public void backData(int data) {
        marker.setState(data);
        try {
            jsonObject.put("state",marker.getState());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    String aaa;

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            //Add comment
            case R.id.img_report_comment:
                startActivityForResult(new Intent(ReportPoliceActivity.this,CommentBoxActivity.class)
                        .putExtra(Constant.EXTRA_CURRENT_COMMENT_REPORT,report_add_comment.getText().toString())
                        ,REQUEST_ADD_COMMENT);
                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);
                break;

            //capture image from camera
            case R.id.img_report_capture:
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;

                    try{
                        photoFile = Utilities.createImageFile(mContext);
                    }catch (IOException ex){
                        ex.printStackTrace();
                    }

                    if(photoFile!=null){
                        Uri photoUri = FileProvider.getUriForFile(mContext,"ohgo.vptech.smarttraffic.main",photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,photoUri);
                        aaa = photoFile.getAbsolutePath();
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }

                }

                break;

            //Add location
            case R.id.img_report_place:
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(ReportPoliceActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
                break;
        }

        menu.close(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            int targetImageViewWidth = img_capture.getWidth();
            int targetImageViewHeight = img_capture.getHeight();

            BitmapFactory.Options bmOption = new BitmapFactory.Options();
            bmOption.inJustDecodeBounds = true;

            BitmapFactory.decodeFile(aaa,bmOption);

            int cameraImageWidth = bmOption.outWidth;
            int cameraImageHeight = bmOption.outHeight;

            int scaleFactor = Math.min(cameraImageWidth/targetImageViewWidth,cameraImageHeight/targetImageViewHeight);

            bmOption.inSampleSize = scaleFactor;

            bmOption.inJustDecodeBounds = false;

            Bitmap b = BitmapFactory.decodeFile(aaa,bmOption);

            marker.setImageCode(Base64.encodeToString(Utilities.encodeImage(b),Base64.DEFAULT));

            img_capture.setImageBitmap(b);

        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {

                if(ln_location==null)
                    ln_location = (LinearLayout)findViewById(R.id.ln_location);
                if(ln_location.getVisibility()!=View.VISIBLE)ln_location.setVisibility(View.VISIBLE);

                Place place = PlacePicker.getPlace(this, data);
                String curr_add = (!place.getAddress().equals(""))?place.getAddress().toString():"";
                String curr_name =(!place.getName().equals(""))?place.getName().toString():"";
                marker.setAddress(curr_add);
                marker.setName(curr_name);
                marker.setLatLng(place.getLatLng());
                //marker.setMarker_id(place.getId());
                report_location_name.setText(curr_name);
                report_location_address.setText(curr_add);
            }
        }

        if(requestCode == REQUEST_ADD_COMMENT && resultCode == RESULT_OK){

            if(ln_comment==null)ln_comment=(RelativeLayout)findViewById(R.id.ln_comment);
            if(tv_user_name==null){
                tv_user_name =(TextView)findViewById(R.id.tv_user_name);
                tv_user_name.setText(currentUser.get_firstName());
            }
            if(user_avatar==null){
                user_avatar=(CircleImageView)findViewById(R.id.img_avatar);
                user_avatar.setImageBitmap(currentUser.get_profileImage());
            }
            if(ln_comment.getVisibility()!=View.VISIBLE){
                ln_comment.setVisibility(View.VISIBLE);

            }

            Bundle ex = data.getExtras();
            if(ex!=null){
                String comment = ex.getString(Constant.EXTRA_RESPONSE_RESULT_COMMENT_REPORT).toString();
                marker.setComment(comment);
                report_add_comment.setText(ex.getString(Constant.EXTRA_RESPONSE_RESULT_COMMENT_REPORT).toString());
            }

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    //Click event of button in toolbar menu.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==R.id.report_send){
            if(marker.getLatLng() == null){
                Toast.makeText(mContext,getResources().getString(R.string.error_null_location),Toast.LENGTH_SHORT).show();
                menu.open(true);
            }

            else if(marker.getState()==-1){
                Toast.makeText(mContext,getResources().getString(R.string.error_null_state),Toast.LENGTH_SHORT).show();
            }
            else {
                Handler h = new Handler();
                progress.show();
                h.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        if (marker.getAddress() == null || marker.getAddress().equals("")) {

                            if(queue==null){
                                queue = Volley.newRequestQueue(mContext);
                            }

                            Double lat = marker.getLatLng().latitude;
                            Double lng = marker.getLatLng().longitude;

                            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, "http://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng, null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    try {
                                        if(response.getString("status").equals("OK")){
                                            JSONArray a = response.getJSONArray("results");

                                            JSONObject jsonObject = a.getJSONObject(0);

                                            String s = jsonObject.getString("formatted_address");

                                            marker.setAddress(s);


                                            sendReport();

                                        }

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            });

                            queue.add(jsonObjectRequest);

                        }
                        else{
                           sendReport();
                        }
                    }

                },500);

            }

        }
        return true;
    }

    private  void sendReport(){
        try {
            String user = "";
            if (extra != null) {
                user = extra.getString("user");
            }
            jsonObject.put(ReportField.RP_LATTITUE, marker.getLatLng().latitude);
            jsonObject.put(ReportField.RP_LONGTITUE, marker.getLatLng().longitude);
            jsonObject.put(ReportField.RP_NAME, marker.getName());
            jsonObject.put(ReportField.RP_ADDRESS, marker.getAddress());
            jsonObject.put(ReportField.RP_TYPE, marker.getType());
            jsonObject.put(ReportField.RP_STATE, marker.getState());
            jsonObject.put("imageEncode", marker.getImageCode());
            jsonObject.put(ReportField.RP_COMMENT, marker.getComment());
            jsonObject.put("marker_id",android_id);
            jsonObject.put(ReportField.RP_USER, user);
            mSocket.emit("cordorate", jsonObject);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }
}
