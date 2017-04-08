package ohgo.vptech.smarttraffic.main.reporter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import ohgo.vptech.smarttraffic.main.MapsActivity;
import ohgo.vptech.smarttraffic.main.cores.Original;
import ohgo.vptech.smarttraffic.main.entities.CommentItem;
import ohgo.vptech.smarttraffic.main.entities.LikeReport;
import ohgo.vptech.smarttraffic.main.entities.Users;
import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.adapters.CommentBoxAdapter;
import ohgo.vptech.smarttraffic.main.adapters.VolleySingleton;
import ohgo.vptech.smarttraffic.main.cores.BaseActivity;
import ohgo.vptech.smarttraffic.main.utilities.constants.CacheUser;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import ohgo.vptech.smarttraffic.main.utilities.constants.ReportField;
import ohgo.vptech.smarttraffic.main.utilities.constants.UserField;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import com.google.android.gms.maps.model.LatLng;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ypn on 9/12/2016.
 */
public class ReportDetailActivity extends Original {
    private Toolbar mToolbar;
    private RequestQueue queue;
    private Context mContext;
    private Socket mSocket;
    private LatLng latLng;

    {
        try {
            mSocket = IO.socket(Constant.SERVER_REALTIME_URL);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_detail);
        queue = Volley.newRequestQueue(this);
        mContext = this;
        mSocket.connect();//Connect to server handler real-time
        mToolbar = (Toolbar) findViewById(R.id.tb_main);

        final List<CommentItem> listComment = new ArrayList<>();
        final CommentBoxAdapter adapter = new CommentBoxAdapter(this,listComment);
        final RecyclerView commentBox =(RecyclerView)findViewById(R.id.comment_box);
        final NetworkImageView evidence_image = (NetworkImageView)findViewById(R.id.evidence_image);
        final EditText edt_entry_comment = (EditText)findViewById(R.id.edt_entry_comment);
        final TextView address = (TextView)findViewById(R.id.address);
        final TextView report_time = (TextView)findViewById(R.id.report_time);
        final long now = System.currentTimeMillis();
        final ImageView vote_up = (ImageView) findViewById(R.id.vote_up);
        final ImageView btn_thumb_down = (ImageView) findViewById(R.id.btn_thumb_down);
        final TextView report_count_comment = (TextView)findViewById(R.id.report_count_comment);
        final LikeReport lr = new LikeReport();
        final TextView report_vote_up =(TextView)findViewById(R.id.report_vote_up);
        final TextView report_vote_down =(TextView)findViewById(R.id.report_vote_down);
        final ImageView btn_send_comment =(ImageView) findViewById(R.id.btn_send_comment);
        final ProgressBar pgb_load_image =(ProgressBar)findViewById(R.id.pgb_load_image);
        final ProgressBar pgb_load_comment =(ProgressBar)findViewById(R.id.pgb_load_comment);


        Intent intent = getIntent();
        Bundle extra = intent.getExtras();
        final String jsonCurrentUser = extra.getString("currentUser");

        //draw divider
        Paint paint = new Paint();
        paint.setStrokeWidth(1);
        paint.setColor(getResources().getColor(R.color.divider_recyclerview));
        paint.setAntiAlias(true);
        paint.setPathEffect(new DashPathEffect(new float[]{25.0f, 25.0f}, 0));
        commentBox.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(mContext).paint(paint).build());

        try {

            JSONObject data = new JSONObject(extra.getString("REPORT_DATA"));

            if(currentUser==null){
                currentUser = new Users();
                SharedPreferences setting = getSharedPreferences(Constant.CODE_PREFERENCE_SETTING, 0);
                JSONObject userData = new JSONObject(setting.getString("user", null));

                String _firstname = userData.getString(UserField.FIRST_NAME);
                String _email = userData.getString(UserField.EMAIL);
                String _phone = userData.getString(UserField.PHONE);
                int _id = userData.getInt(UserField.ID);

                currentUser.set_Id(_id);
                currentUser.set_firstName(_firstname);
                currentUser.set_email(_email);
                currentUser.set_phoneNumber(_phone);
                currentUser.set_urlProfileImage(new JSONObject(userData.getString(UserField.IMAGES)).getString(UserField.AVATAR_URL));

                ImageRequest imageRequest = new ImageRequest(Constant.SERVER_GET_API + "upload/" + currentUser.get_urlProfileImage(), new Response.Listener<Bitmap>() {
                    @Override
                    public void onResponse(Bitmap response) {
                        currentUser.set_profileImage(response);
                    }
                }, 100, 100, ImageView.ScaleType.CENTER, Bitmap.Config.ALPHA_8, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        currentUser.set_profileImage(BitmapFactory.decodeResource(getResources(),R.drawable.avatar_circle_blue_120dp));
                    }
                });

                queue.add(imageRequest);
            }


            mToolbar.setTitle(data.getString("mTitle"));
            final JSONObject markerInfo = new JSONObject(data.getString("mSnippet"));
            final String reportId = markerInfo.getString(ReportField.RP_ID);
            String imgUrl = markerInfo.getString(ReportField.RP_IMG_URL);
            String comment = markerInfo.getString(ReportField.RP_COMMENT);
            String str_address = markerInfo.getString(ReportField.RP_ADDRESS);
            Double lat = markerInfo.getDouble(ReportField.RP_LATTITUE);
            Double lng = markerInfo.getDouble(ReportField.RP_LONGTITUE);

            latLng = new LatLng(lat,lng);

            JSONObject user_comment;

            if(markerInfo.has("user") && !markerInfo.getString("user").equals("")){
                user_comment = new JSONObject(markerInfo.getString("user"));
            }

            else{
                user_comment = new JSONObject();
                user_comment.put(CacheUser.FIRST_NAME,"");
                user_comment.put(CacheUser.LAST_NAME,"");
                user_comment.put(CacheUser.IMG_PROFILE,"");
            }

            Timestamp get_time_report = markerInfo.has(ReportField.RP_CREATE_AT)?new Timestamp(Long.parseLong(markerInfo.getString(ReportField.RP_CREATE_AT))):null;

            String time_report = DateUtils.getRelativeTimeSpanString(get_time_report!=null?get_time_report.getTime():now,now,DateUtils.MINUTE_IN_MILLIS).toString();


            edt_entry_comment.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        String cm = edt_entry_comment.getText().toString();
                        if(cm!=null&&!cm.equals("")){
                            final CommentItem newCi = new CommentItem();
                            newCi.setUserName(currentUser.get_firstName());
                            newCi.setContent(cm);
                            newCi.setTimeDiff("just now");
                            if(currentUser.get_profileImage()!=null){
                                newCi.setUserAvatar(currentUser.get_profileImage());
                            }

                            JSONObject newComment = new JSONObject();
                            try {
                                newComment.put("_comment",cm);
                                newComment.put("_id",markerInfo.getString("_id"));
                                newComment.put("user",jsonCurrentUser);
                                mSocket.emit(markerInfo.getString("_id"),newComment);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            listComment.add(0,newCi);
                            adapter.notifyItemInserted(0);
                            adapter.notifyDataSetChanged();
                            edt_entry_comment.setText(null);

                            InputMethodManager inputManager = (InputMethodManager)
                                    mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputManager.toggleSoftInput(0, 0);

                            commentBox.scrollToPosition(0);
                        }
                        return  true;
                    }else{
                        return false;
                    }
                }
            });

            btn_send_comment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String cm = edt_entry_comment.getText().toString();
                    if(cm!=null&&!cm.equals("")){
                        final CommentItem newCi = new CommentItem();
                        newCi.setUserName(currentUser.get_firstName());
                        newCi.setContent(cm);
                        newCi.setTimeDiff("just now");
                        if(currentUser.get_profileImage()!=null){
                            newCi.setUserAvatar(currentUser.get_profileImage());
                        }

                        JSONObject newComment = new JSONObject();
                        try {
                            newComment.put("_comment",cm);
                            newComment.put("_id",markerInfo.getString("_id"));
                            newComment.put("user",jsonCurrentUser);
                            mSocket.emit(markerInfo.getString("_id"),newComment);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        listComment.add(newCi);
                        adapter.notifyItemInserted(listComment.size()-1);
                        adapter.notifyDataSetChanged();
                        edt_entry_comment.setText(null);

                        InputMethodManager inputManager = (InputMethodManager)
                                mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        inputManager.toggleSoftInput(0, 0);
                        commentBox.scrollToPosition(listComment.size());
                    }
                }
            });

            if(comment!=null && !comment.equals("") && user_comment!=null){

                final CommentItem ci = new CommentItem();

                ci.setContent(comment);
                ci.setUserName(
                        (user_comment.has(CacheUser.FIRST_NAME)?user_comment.getString(CacheUser.FIRST_NAME):"")
                                + (user_comment.has(CacheUser.LAST_NAME)?user_comment.getString(CacheUser.LAST_NAME):""));
                ci.setTimeDiff(time_report);

                ImageRequest imageRequest = new ImageRequest(Constant.SERVER_GET_API + "upload/" + user_comment.getString(CacheUser.IMG_PROFILE),
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                ci.setUserAvatar(response);
                                listComment.add(ci);
                                adapter.notifyItemInserted(listComment.size()-1);
                                adapter.notifyDataSetChanged();
                            }
                        }, 100, 100, ImageView.ScaleType.CENTER, Bitmap.Config.ALPHA_8,
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Bitmap bm_avatar = BitmapFactory.decodeResource(getResources(),R.drawable.avatar_circle_blue_120dp);
                                ci.setUserAvatar(bm_avatar);
                                listComment.add(ci);
                                adapter.notifyItemInserted(listComment.size()-1);
                                adapter.notifyDataSetChanged();
                            }
                        });

                queue.add(imageRequest);

                lr.setCount_comment(lr.getCount_comment()+1);
                report_count_comment.setText("" + lr.getCount_comment());

            }

            report_time.setText(" " + time_report);
            address.setText(""+str_address);

            if(!imgUrl.equals("")){
                ImageLoader mImageLoader;
                mImageLoader = VolleySingleton.getInstance(mContext,pgb_load_image).getImageLoader();
                evidence_image.setImageUrl(Constant.SERVER_REALTIME_URL + imgUrl, mImageLoader);
            }else {
                evidence_image.setDefaultImageResId(R.drawable.image_report_detail);
            }

            commentBox.setAdapter(adapter);
            commentBox.setLayoutManager(new LinearLayoutManager(mContext));

            if(!reportId.equals("")){
                StringRequest count_like_dislike = new StringRequest(Request.Method.POST, Constant.SERVER_REALTIME_URL + "get-rp-detail", new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject data = new JSONObject(response);
                            JSONArray uid_like = data.getJSONArray(ReportField.RP_UID_LIKE);

                            int c_like = uid_like.length();
                            lr.setCount_like(c_like);
                            report_vote_up.setText("" +c_like);
                            for(int i = 0; i < c_like ; i++){
                                if(currentUser.get_Id()==uid_like.getInt(i)){
                                    lr.setLike(true);
                                    vote_up.setImageResource(R.drawable.hight_like);
                                    break;
                                }
                            }

                            JSONArray uid_dislike = data.getJSONArray(ReportField.RP_UID_DISLIKE);
                            int c_dislike = uid_dislike.length();
                            lr.setCount_dis_like(c_dislike);
                            report_vote_down.setText(""+c_dislike);

                            for (int i=0 ; i < c_dislike ; i++){
                                if(currentUser.get_Id()==uid_dislike.getInt(i)){
                                    lr.setDislike(true);
                                    btn_thumb_down.setImageResource(R.drawable.group_copy);
                                    break;
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<>();
                        params.put("reportId",reportId);
                        return  params;
                    }
                };

                queue.add(count_like_dislike);

                vote_up.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!lr.isLike()) {
                            final StringRequest vote_up_request = new StringRequest(Request.Method.POST, Constant.SERVER_REALTIME_URL + "update-rp-like", new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if(response.equals("success")){
                                        lr.setLike(!lr.isLike());
                                        lr.setCount_like(lr.getCount_like()+1);
                                        report_vote_up.setText(""+lr.getCount_like());
                                        vote_up.setImageResource(R.drawable.hight_like);
                                        Toast.makeText(mContext,getString(R.string.like_report),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("reportId", reportId);
                                    params.put("uid", String.valueOf(currentUser.get_Id()));
                                    return params;
                                }
                            };
                            queue.add(vote_up_request);

                            if(lr.isDislike()){
                                lr.setCount_dis_like(lr.getCount_dis_like()-1);
                                report_vote_down.setText(""+lr.getCount_dis_like());
                                lr.setDislike(false);
                                btn_thumb_down.setImageResource(R.drawable.group_copy3);
                            }
                        }else{
                            StringRequest vote_exit_up = new StringRequest(Request.Method.POST, Constant.SERVER_REALTIME_URL + "update-rp-exit-like", new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if(response.equals("success")){
                                        lr.setLike(!lr.isLike());
                                        lr.setCount_like(lr.getCount_like()-1);
                                        report_vote_up.setText(""+lr.getCount_like());
                                        vote_up.setImageResource(R.drawable.group_copy2);
                                        Toast.makeText(mContext,getString(R.string.remove_like_report),Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            }) {
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String, String> params = new HashMap<>();
                                    params.put("reportId", reportId);
                                    params.put("uid", String.valueOf(currentUser.get_Id()));
                                    return params;
                                }
                            };

                            queue.add(vote_exit_up);
                        }
                    }
                });

                btn_thumb_down.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if(!lr.isDislike()){
                            final StringRequest dislike = new StringRequest(Request.Method.POST, Constant.SERVER_REALTIME_URL + "update-rp-dislike", new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if(response.equals("success")){
                                        lr.setDislike(!lr.isDislike());
                                        lr.setCount_dis_like(lr.getCount_dis_like()+1);
                                        report_vote_down.setText(""+lr.getCount_dis_like());
                                        btn_thumb_down.setImageResource(R.drawable.group_copy);
                                        Toast.makeText(mContext,getString(R.string.dislike_report), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            }){
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String,String> params = new HashMap<>();
                                    params.put("reportId", reportId);
                                    params.put("uid", String.valueOf(currentUser.get_Id()));
                                    return params;
                                }
                            };

                            queue.add(dislike);

                            if(lr.isLike()){
                                lr.setCount_like(lr.getCount_like()-1);
                                report_vote_up.setText(""+lr.getCount_like());
                                vote_up.setImageResource(R.drawable.group_copy2);
                                lr.setLike(false);
                            }
                        }
                        else{
                            StringRequest dislike = new StringRequest(Request.Method.POST, Constant.SERVER_REALTIME_URL + "update-rp-exit-dislike", new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    if(response.equals("success")){
                                        lr.setDislike(!lr.isDislike());
                                        lr.setCount_dis_like(lr.getCount_dis_like()-1);
                                        report_vote_down.setText(""+lr.getCount_dis_like());
                                        btn_thumb_down.setImageResource(R.drawable.group_copy3);
                                        Toast.makeText(mContext, getString(R.string.remove_dislike_report), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                }
                            }){
                                @Override
                                protected Map<String, String> getParams() throws AuthFailureError {
                                    Map<String,String> params = new HashMap<>();
                                    params.put("reportId", reportId);
                                    params.put("uid", String.valueOf(currentUser.get_Id()));
                                    return params;
                                }
                            };

                            queue.add(dislike);
                        }
                    }
                });

                pgb_load_comment.setVisibility(View.VISIBLE);
                Map<String, String> postParam= new HashMap<>();
                postParam.put("reportId", reportId);
                JsonObjectRequest requestComment = new JsonObjectRequest(
                        Request.Method.POST,
                        Constant.SERVER_REALTIME_URL + "get-comments",
                        new JSONObject(postParam),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    JSONArray arrayComment = response.getJSONArray("data");
                                    lr.setCount_comment(lr.getCount_comment() + arrayComment.length());
                                    report_count_comment.setText("" + lr.getCount_comment());
                                    for(int i = 0 ; i < arrayComment.length(); i++){
                                        JSONObject comm = arrayComment.getJSONObject(i);
                                        final CommentItem ci = new CommentItem();
                                        JSONObject userInf ;
                                        if(comm.has("user") && !comm.getString("user").equals("")){
                                            userInf = new JSONObject(comm.getString("user"));
                                        }else{
                                            userInf = new JSONObject();
                                            userInf.put(CacheUser.FIRST_NAME,"");
                                            userInf.put(CacheUser.LAST_NAME,"");
                                            userInf.put(CacheUser.IMG_PROFILE,"");
                                        }
                                        Timestamp created_at = comm.has("create_at")?new Timestamp(Long.parseLong(comm.getString("create_at"))):null;
                                        String create_at = DateUtils.getRelativeTimeSpanString(created_at.getTime(),now,DateUtils.MINUTE_IN_MILLIS).toString();

                                        ci.setTimeDiff(create_at);
                                        ci.setContent(comm.getString("content"));
                                        ci.setUserName(userInf.getString(CacheUser.FIRST_NAME) + userInf.getString(CacheUser.LAST_NAME));
                                        ImageRequest imageRequest = new ImageRequest(Constant.SERVER_GET_API + "upload/" + userInf.getString(CacheUser.IMG_PROFILE),
                                                new Response.Listener<Bitmap>() {
                                                    @Override
                                                    public void onResponse(Bitmap response) {
                                                        ci.setUserAvatar(response);
                                                        listComment.add(ci);
                                                        adapter.notifyItemInserted(listComment.size()-1);
                                                        adapter.notifyDataSetChanged();
                                                    }
                                                }, 100, 100, ImageView.ScaleType.CENTER, Bitmap.Config.ALPHA_8,
                                                new Response.ErrorListener() {
                                                    @Override
                                                    public void onErrorResponse(VolleyError error) {

                                                    }
                                                });

                                        queue.add(imageRequest);
                                    }
                                    pgb_load_comment.setVisibility(View.GONE);
                                    commentBox.setVisibility(View.VISIBLE);

                                } catch (JSONException e) {
                                    pgb_load_comment.setVisibility(View.GONE);
                                    e.printStackTrace();
                                }

                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                pgb_load_comment.setVisibility(View.GONE);
                                Toast.makeText(mContext,"Error request data",Toast.LENGTH_SHORT).show();
                            }
                        });

                queue.add(requestComment);
            }

            Emitter.Listener handlerReportComment = new Emitter.Listener() {
                @Override
                public void call(final Object... args) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            JSONObject newComment = (JSONObject)args[0];

                            final CommentItem newCi = new CommentItem();
                            try {
                                JSONObject userComment = new JSONObject(newComment.getString("user"));
                                newCi.setContent(newComment.getString("_comment"));
                                newCi.setUserName(userComment.getString("firstName") + " " + userComment.getString("lastName"));


                                newCi.setTimeDiff("just now");

                                ImageRequest imageRequest = new ImageRequest(Constant.SERVER_GET_API + "upload/" + userComment.getString("urlImageProfile"),
                                        new Response.Listener<Bitmap>() {
                                            @Override
                                            public void onResponse(Bitmap response) {
                                                newCi.setUserAvatar(response);
                                                listComment.add(newCi);
                                                adapter.notifyItemInserted(listComment.size()-1);
                                                adapter.notifyDataSetChanged();
                                            }
                                        }, 100, 100, ImageView.ScaleType.CENTER, Bitmap.Config.ALPHA_8,
                                        new Response.ErrorListener() {
                                            @Override
                                            public void onErrorResponse(VolleyError error) {

                                            }
                                        });

                                queue.add(imageRequest);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        }
                    });
                }
            };

            mSocket.on(markerInfo.getString("_id"),handlerReportComment);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                if(getIntent().hasExtra("ypn_service")) {
                    startActivity(new Intent(ReportDetailActivity.this, MapsActivity.class)
                            .putExtra(Constant.EXTRA_LATLNG, latLng)
                    );
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(getIntent().hasExtra("ypn_service")){
            startActivity(new Intent(this, MapsActivity.class)
                    .putExtra(Constant.EXTRA_LATLNG,latLng)
            );
        }
    }
}
