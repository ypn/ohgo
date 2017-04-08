package ohgo.vptech.smarttraffic.main.utilities;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import android.os.Environment;
import android.support.annotation.NonNull;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;

import android.view.View;
import android.view.ViewGroup;
import ohgo.vptech.smarttraffic.main.MainActivity;

import ohgo.vptech.smarttraffic.main.R;
import ohgo.vptech.smarttraffic.main.entities.Places;
import ohgo.vptech.smarttraffic.main.modules.jobscheduler.JobServiceBackground;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by ypn on 7/29/2016.
 */
public class Utilities {
    private static Locale myLocale;


    /**
     * @auth :ypn
     * @desc: set locale language for entry app.
     * @create_at: 27/7/2016
     * @param lang Language code
     * @param context current activity     *
     */
    public static void setLocale(String lang, Context context) {
        myLocale = new Locale(lang);
        Locale.setDefault(myLocale);
        Resources s = context.getResources();
        DisplayMetrics dm = s.getDisplayMetrics();
        Configuration conf = s.getConfiguration();
        conf.locale = myLocale;
        s.updateConfiguration(conf, dm);

        if(!(context instanceof MainActivity)){
            Intent refesh = new Intent(context, MainActivity.class);
            context.startActivity(refesh);
        }

    }

    /**
     * @auth:ypn
     * @desc:check null for edit text
     * @created_at:8/1/2016
     * @param editText Object need to check null
     * @param errMessage Error message show when null text be entry
     * @return false when null.
     */
    public static boolean checkNullInput(EditText editText, String errMessage, int minLength, int maxLength) {

        if (editText.getText().toString().length() < minLength || editText.getText().toString().length() > maxLength) {
            editText.setError(errMessage);
            return false;
        } else {
            editText.setError(null);
            return true;
        }
    }


    /**
     * @auth:ypn
     * @desc:Check password have have at least a digit, an upper case and 8 character and confirm password must match with password.
     * @param inputPass box which entry password
     * @param confirmPass box which entry confirm password
     * @return false if condition in desc is wrong
     */
    public static boolean checkPassword(EditText inputPass, EditText confirmPass, String pass_not_match, String passempty) {
        String pass = inputPass.getText().toString();
        String cf_pass = confirmPass.getText().toString();
        if(!pass.equals("") && pass.length() >= 6 ){
            inputPass.setError(null);
            if (pass.equals(cf_pass)) {
                confirmPass.setError(null);
                return true;
            } else {
                confirmPass.setError(pass_not_match);
                return false;
            }
        }
        else{
            inputPass.setError(passempty);
            return  false;
        }

    }

    //CHeck password with pattern
    /*public static boolean checkPassword(EditText inputPass, EditText confirmPass, String pass_not_match, String pattern_password) {
        String pattern = "(?=^.{8,30}$)(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])";
        String pass = inputPass.getText().toString();
        String cf_pass = confirmPass.getText().toString();
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(pass);
        if (m.find()) {
            inputPass.setError(null);
            if (pass.equals(cf_pass)) {
                confirmPass.setError(null);
                return true;
            } else {
                confirmPass.setError(pass_not_match);
                return false;
            }

        } else {
            inputPass.setError(pattern_password);
            return false;
        }
    }*/

    public static boolean checkEmail(EditText inputEmail,String errorMail) {
        String data = inputEmail.getText().toString();
        boolean check = !TextUtils.isEmpty(data) && android.util.Patterns.EMAIL_ADDRESS.matcher(data).matches();
        if (!check) {
            inputEmail.setError(errorMail);
        } else {
            inputEmail.setError(null);
        }
        return check;
    }

    public static byte[] encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        return outputStream.toByteArray();
    }

    public static void createCustomTitleActionbar(Context context, String title, ActionBar ab) {
        TextView tv = new TextView(context);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tv.setText(title);
        tv.setTextColor(Color.WHITE);
        tv.setTextSize(Constant.FONT_SIZE_TITLE_BAR);
        tv.setGravity(Gravity.CENTER);

        ColorDrawable colorDrawable = new ColorDrawable(ContextCompat.getColor(context, R.color.color_action_bar));
        ab.setBackgroundDrawable(colorDrawable);
        ab.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        ab.setCustomView(tv);

    }

    /**
     * @auth:ypn
     * @desc:create full size image when user take photo for report
     */
    public static File createImageFile(Context context) throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);
        return image;
    }

    @NonNull
    public static String getStateReport(Context context , int state){

        switch (state){
            case Constant.POLICE_HIDE:
                return context.getResources().getString(R.string.notice_police_hide);
            case Constant.POLICE_VISIBLE:
                return context.getResources().getString(R.string.notice_police_show);
            case Constant.POLICE_OTHESIDE:
                return context.getResources().getString(R.string.notice_police_other);

            case Constant.TRAFFIC_MODERATE:
                return context.getResources().getString(R.string.notice_traffic_moderate);
            case Constant.TRAFFIC_HEAVY:
                return context.getResources().getString(R.string.notice_traffic_heavy);
            case Constant.TRAFFIC_STANDTILL:
                return context.getResources().getString(R.string.notice_traffic_standstil);

            case Constant.ACCIDENT_MIRROR:
                return context.getResources().getString(R.string.notice_accident_mirror);
            case Constant.ACCIDENT_MAJOR:
                return context.getResources().getString(R.string.notice_accident_major);
            case Constant.ACCIDENT_OTHER_SIDE:
                return context.getResources().getString(R.string.notice_accident_other);

            case Constant.CAM_SPEED:
                return context.getResources().getString(R.string.notice_camera_speed);
            case Constant.CAM_RED_LIGHT:
                return context.getResources().getString(R.string.notice_camera_red_light);
            case Constant.CAM_FAKE:
                return context.getResources().getString(R.string.notice_camera_fake);

            default:
                return context.getResources().getString(R.string.report_state_unknow);
        }
    }


    public static  int getResourceMarker(int type){
        int res = 0;

        switch (type){
            case Constant.REPORT_TYPE_POLICE:
                res = R.drawable.police_marker;
                break;
            case Constant.REPORT_TYPE_ACCIDENT:
                res = R.drawable.accident_marker;
                break;
            case Constant.REPORT_TYPE_CAMERA:
                res = R.drawable.camera_marker;
                break;
            case Constant.REPORT_TYPE_TRAFFIC_JAM:
                res = R.drawable.traffic_jam_marker;
                break;
        }

        return  res;
    }

    public static List<Places> initializeList(Context context){
        List<Places> lPlace = new ArrayList<>();
        Places home,work,favorite;
        String not_ready = context.getResources().getString(R.string.not_ready);

        home = new Places();
        home.setName(context.getResources().getString(R.string.menu_item_home));
        home.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.home));
        home.setSummary(not_ready);
        lPlace.add(home);

        work = new Places();
        work.setName(context.getResources().getString(R.string.menu_item_work));
        work.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.work));
        work.setSummary(not_ready);
        lPlace.add(work);

        favorite = new Places();
        favorite.setName(context.getResources().getString(R.string.menu_item_favorite));
        favorite.setIcon(BitmapFactory.decodeResource(context.getResources(),R.drawable.favorite));
        favorite.setSummary(not_ready);
        lPlace.add(favorite);

        return  lPlace;
    }


    //Create auto resize height listview
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


    public static boolean isJSONValid(String test) {
        try {
            new JSONObject(test);
        } catch (JSONException ex) {
            try {
                new JSONArray(test);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }


    /**
     * @desc: start background service for handler notification when has new report.
     * @auth: ypn
     * @param context
     */
    public static void startJobService(Context context){
        JobInfo.Builder builder = new JobInfo.Builder(Constant.ID_JOB_SERVICE,
                new ComponentName(context.getPackageName(), JobServiceBackground.class.getName())
        );

        builder.setMinimumLatency(1 * Constant.MILIS);
        builder.setOverrideDeadline(1 * Constant.MILIS);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED);//Need any type of network connection.
       // builder.setRequiresDeviceIdle(false);
        builder.setPersisted(true);//across device reboots
        builder.setBackoffCriteria(1 * Constant.MILIS,JobInfo.BACKOFF_POLICY_EXPONENTIAL);

        JobScheduler jobScheduler = (JobScheduler)context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(builder.build());
    }

}
