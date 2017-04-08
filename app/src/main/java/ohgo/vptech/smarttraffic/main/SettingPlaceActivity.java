package ohgo.vptech.smarttraffic.main;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.Toolbar;
import android.view.View;

import ohgo.vptech.smarttraffic.main.adapters.PlacesAdapter;
import ohgo.vptech.smarttraffic.main.cores.Original;
import ohgo.vptech.smarttraffic.main.entities.Places;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingPlaceActivity extends Original {

    private ListView mListPlace;
    private Toolbar mToolbar;
    private static final int PLACE_PICKER_REQUEST = 2;
    private int pos_select = 0;
    private List<Places> listPlaces;
    private PlacesAdapter placesAdapter;
    private SharedPreferences setting;
    private JSONArray jsonArrayPlace;
    private JSONObject objectPlace;
    private ImageView send_data;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_place);

        queue = Volley.newRequestQueue(this);

        mListPlace = (ListView)findViewById(R.id.list_places);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        send_data = (ImageView) mToolbar.findViewById(R.id.send_data);

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setting = getSharedPreferences(Constant.CODE_PREFERENCE_SETTING,0);

        listPlaces = initializeList();


        String a = currentUser.get_address();
        if(a!=null && a!= ""){
            try {
                jsonArrayPlace = new JSONArray(a);
                for(int i = 0 ; i < jsonArrayPlace.length() ; i++){
                    JSONObject jsonObject = jsonArrayPlace.getJSONObject(i);

                    int pos_change = jsonObject.getInt("pos");
                    Places p = listPlaces.get(pos_change);
                    p.setSummary(jsonObject.getString("address"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }else{
            jsonArrayPlace = new JSONArray();
        }


        placesAdapter = new PlacesAdapter(this,R.layout.place_item_in,listPlaces );

        mListPlace.setAdapter(placesAdapter);

        mListPlace.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                mListPlace.getChildAt(position).setEnabled(false);
                try {
                    startActivityForResult(builder.build(SettingPlaceActivity.this), PLACE_PICKER_REQUEST);
                    pos_select = position;
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                    mListPlace.getChildAt(position).setEnabled(true);
                } catch (GooglePlayServicesNotAvailableException e) {
                    mListPlace.getChildAt(position).setEnabled(true);
                    e.printStackTrace();
                }
            }
        });


        send_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                send_data.setEnabled(false);

                int user_id = currentUser.get_Id();

                final ProgressDialog progressDialog = new ProgressDialog(SettingPlaceActivity.this,R.style.MyAlertDialog);
                progressDialog.setTitle(getString(R.string.add_place_title));
                progressDialog.setMessage(getString(R.string.waiting));
                progressDialog.show();

                final StringRequest stringRequest = new StringRequest(Request.Method.POST, Constant.SERVER_GET_API +"api_update_profile1" + "/" + user_id, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressDialog.dismiss();
                        SharedPreferences settings = getSharedPreferences("LANGS",0);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("user",response);
                        editor.commit();
                        Toast.makeText(SettingPlaceActivity.this,getString(R.string.save_change),Toast.LENGTH_SHORT).show();
                        send_data.setEnabled(true);
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Toast.makeText(SettingPlaceActivity.this,error.toString(),Toast.LENGTH_SHORT).show();
                        send_data.setEnabled(true);
                    }
                }){
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        Map<String,String> params = new HashMap<>();
                        params.put("address",jsonArrayPlace.toString());
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
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("pos_select",pos_select);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if(savedInstanceState.get("pos_select")!=null){
            pos_select = savedInstanceState.getInt("pos_select");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK){

            SharedPreferences.Editor editor = setting.edit();

            Place place = PlacePicker.getPlace(this, data);

            String curr_add = (!place.getAddress().equals(""))?place.getAddress().toString():"";
            Double lat = place.getLatLng().latitude;
            Double lng = place.getLatLng().longitude;

            Places p = listPlaces.get(pos_select);
            p.setSummary(curr_add);

            objectPlace = new JSONObject();

            try {
                objectPlace.put("address",curr_add);
                objectPlace.put("lat",lat);
                objectPlace.put("lng",lng);
                objectPlace.put("pos",pos_select);
                jsonArrayPlace.put(pos_select,objectPlace);
                editor.putString(Constant.CODE_PREFERENCE_PLACE,jsonArrayPlace.toString());
                editor.commit();

            } catch (JSONException e) {
                e.printStackTrace();
            }

            mListPlace.setAdapter(placesAdapter);

            Utilities.setListViewHeightBasedOnChildren(mListPlace);

            if(mListPlace.getChildAt(pos_select)!=null){
                mListPlace.getChildAt(pos_select).setEnabled(true);
            }

        }
    }
    //order of object initialize never change
    private List<Places> initializeList(){
        List<Places> lPlace = new ArrayList<>();
        Places home,work,favorite;
        String tap_to_add = getResources().getString(R.string.tap_to_add);

        home = new Places();
        home.setName(getResources().getString(R.string.menu_item_home));
        home.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.home_copy));
        home.setSummary(tap_to_add);
        lPlace.add(home);

        work = new Places();
        work.setName(getResources().getString(R.string.menu_item_work));
        work.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.work_copy));
        work.setSummary(tap_to_add);
        lPlace.add(work);

        favorite = new Places();
        favorite.setName(getResources().getString(R.string.menu_item_favorite));
        favorite.setIcon(BitmapFactory.decodeResource(getResources(),R.drawable.favorite_copy));
        favorite.setSummary(tap_to_add);
        lPlace.add(favorite);


        return  lPlace;
    }


}
