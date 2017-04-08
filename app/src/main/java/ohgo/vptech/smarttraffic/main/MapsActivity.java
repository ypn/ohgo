package ohgo.vptech.smarttraffic.main;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;

import ohgo.vptech.smarttraffic.main.adapters.PlacesAdapter;
import ohgo.vptech.smarttraffic.main.entities.Places;
import ohgo.vptech.smarttraffic.main.modules.background.BackgroundService;
import ohgo.vptech.smarttraffic.main.utilities.Utilities;
import ohgo.vptech.smarttraffic.main.utilities.constants.CacheUser;
import ohgo.vptech.smarttraffic.main.utilities.constants.ReportField;
import ohgo.vptech.smarttraffic.main.entities.ShortUsers;
import ohgo.vptech.smarttraffic.main.entities.Users;
import ohgo.vptech.smarttraffic.main.adapters.MarkerRender;
import ohgo.vptech.smarttraffic.main.adapters.MarkerReport;
import ohgo.vptech.smarttraffic.main.cores.BaseActivity;
import ohgo.vptech.smarttraffic.main.reporter.ReportDetailActivity;
import ohgo.vptech.smarttraffic.main.reporter.ReportPoliceActivity;
import ohgo.vptech.smarttraffic.main.utilities.constants.Constant;
import ohgo.vptech.smarttraffic.main.utilities.constants.UserField;

import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.clans.fab.FloatingActionButton;

import com.github.clans.fab.FloatingActionMenu;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import finder.DirectionFinder;
import finder.DirectionFinderListener;
import finder.Route;
import de.hdodenhof.circleimageview.CircleImageView;

public class MapsActivity extends BaseActivity implements
        GoogleMap.OnCameraIdleListener,
        OnMapReadyCallback,
        DirectionFinderListener,
        View.OnClickListener {

    private FloatingActionButton btn_report_police, btn_report_camera, btn_report_traffic_jam, btn_report_traffic_accident;
    private FloatingActionMenu menu;
    private PlaceAutocompleteFragment place_autocomplete_fragment;
    private GoogleMap mMap;
    private Toolbar toolbar;
    private ActionBarDrawerToggle drawerToggle;
    private NavigationView navigationView;
    private View header;
    private ListView listPlaces;
    private DrawerLayout drawerLayout;
    private List<Marker> originMarkers = new ArrayList<>();
    private List<Marker> destinationMarkers = new ArrayList<>();
    private List<Polyline> polylinePaths = new ArrayList<>();
    private RequestQueue queue;
    private ShortUsers shortUsers;
    private String jsonCurrentUser, current_desc;
    private HashMap<String, MarkerReport> hashMapReport = new HashMap<>();
    private ClusterManager<MarkerReport> clusterManager;
    private MarkerReport _markerReport;
    private Context mContext;
    private Bundle ex;
    private SharedPreferences setting;
    private View item_setting, item_logout;
    private LinearLayout persistent_bottom_sheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView tv_distance, tv_time, tv_dest_name, tv_dest_address;
    private String dest = "";
    private List<Places> list;
    private float _zoomOffset = 0.0001f;

    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(Constant.SERVER_REALTIME_URL);
        } catch (URISyntaxException ex) {
            ex.printStackTrace();
        }
    }

    private boolean _first_boot;

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);
        if(!_first_boot && mMap !=null){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constant.CODE_REQUEST_FINE_LOC);
                return;
            } else {
                mMap.setMyLocationEnabled(true);

                if (getIntent().hasExtra(Constant.EXTRA_LATLNG)) {
                    LatLng prev = ex.getParcelable(Constant.EXTRA_LATLNG);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(prev));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(prev, 15));
                } else {
                    LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));

                    if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notifications_new_message", true)) {
                        Toast.makeText(this,"Start job service",Toast.LENGTH_SHORT).show();
                        startService(new Intent(this, BackgroundService.class));
                        //Utilities.startJobService(mContext);
                    }
                }

                _first_boot = true;
            }
        }
    }

    /**
     * @auth ypn
     * @desc define custom infowindow for google map marker.
     */
    class MarkerInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {
        private final View mContentsView;
        private final TextView marker_name;
        private final TextView marker_address;
        private final ImageView iv_user_report;

        MarkerInfoWindowAdapter() {
            mContentsView = getLayoutInflater().inflate(R.layout.popup, null);
            iv_user_report = (ImageView) mContentsView.findViewById(R.id.img_marker);
            marker_name = (TextView) mContentsView.findViewById(R.id.marker_name);
            marker_address = (TextView) mContentsView.findViewById(R.id.marker_address);
        }

        @Override
        public View getInfoContents(Marker marker) {
            marker_name.setText(_markerReport.getTitle());
            try {
                JSONObject markerInfo = new JSONObject(_markerReport.getSnippet());
                marker_address.setText(markerInfo.getString(ReportField.RP_ADDRESS));
                JSONObject user = new JSONObject(markerInfo.getString(ReportField.RP_USER));

                ImageRequest imageRequest = new ImageRequest(Constant.SERVER_GET_API + "upload/" + user.getString(CacheUser.IMG_PROFILE),
                        new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                iv_user_report.setImageBitmap(response);
                            }
                        }, 100, 100, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_4444,
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                iv_user_report.setImageResource(R.drawable.nouser);
                            }
                        });

                queue.add(imageRequest);


            } catch (JSONException e) {
                e.printStackTrace();
            }

            return mContentsView;
        }

        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mContext = this;

        setting = getSharedPreferences(Constant.CODE_PREFERENCE_SETTING, 0);

        //Initialize view
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        navigationView = (NavigationView) findViewById(R.id.sidebar_navigation_view);
        navigationView.setItemIconTintList(null);

        header = navigationView.findViewById(R.id.drawer_header);
        item_setting = navigationView.findViewById(R.id.menu_settings);
        item_logout = navigationView.findViewById(R.id.menu_logout);

        item_setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapsActivity.this, SettingsActivity.class));
            }
        });

        item_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(mContext);
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent t = new Intent(MapsActivity.this, SignInUpActivity.class);
                        startActivity(t);

                        final String LOG_OUT = "event_logout";
                        Intent intent = new Intent(LOG_OUT);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.setMessage(getString(R.string.logout_message));
                builder.setTitle(getString(R.string.logout_title));

                android.app.AlertDialog dialog = builder.create();
                dialog.show();

            }
        });

        listPlaces = (ListView) navigationView.findViewById(R.id.list_place);

        listPlaces.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Places p = list.get(position);

                if (!p.getSummary().equals(getString(R.string.not_ready))) {
                    dest = p.getName();
                    current_desc = p.getSummary();
                    drawerLayout.closeDrawers();
                    sendRequest(p.getSummary());
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(mContext, SettingPlaceActivity.class));
                        }
                    });
                    builder.setNegativeButton(getString(R.string.add_after), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                    builder.setMessage(getString(R.string.add_place_message));
                    builder.setTitle(R.string.add_place_title);

                    AlertDialog dialog = builder.create();
                    dialog.show();


                }
            }
        });

        list = Utilities.initializeList(mContext);

        PlacesAdapter placesAdapter = new PlacesAdapter(mContext, R.layout.place_item, list);
        listPlaces.setAdapter(placesAdapter);

        //Button handle report
        btn_report_police = (FloatingActionButton) findViewById(R.id.btn_report_police);
        btn_report_police.setOnClickListener(this);

        btn_report_camera = (FloatingActionButton) findViewById(R.id.btn_report_camera);
        btn_report_camera.setOnClickListener(this);

        btn_report_traffic_accident = (FloatingActionButton) findViewById(R.id.btn_report_trafic_accident);
        btn_report_traffic_accident.setOnClickListener(this);

        btn_report_traffic_jam = (FloatingActionButton) findViewById(R.id.btn_report_trafic_jam);
        btn_report_traffic_jam.setOnClickListener(this);

        place_autocomplete_fragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        persistent_bottom_sheet = (LinearLayout) findViewById(R.id.bottom_sheet_persistent);
        bottomSheetBehavior = BottomSheetBehavior.from(persistent_bottom_sheet);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_EXPANDED:
                        menu.hideMenu(true);
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        menu.showMenu(true);
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        tv_distance = (TextView) persistent_bottom_sheet.findViewById(R.id.tv_distance);
        tv_dest_name = (TextView) persistent_bottom_sheet.findViewById(R.id.tv_dest_name);
        tv_dest_address = (TextView) persistent_bottom_sheet.findViewById(R.id.tv_dest_address);
        tv_time = (TextView) persistent_bottom_sheet.findViewById(R.id.tv_time);

        place_autocomplete_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                current_desc = place.getAddress().toString();
                dest = place.getName().toString();
                sendRequest(current_desc);
            }

            @Override
            public void onError(Status status) {

            }
        });

        menu = (FloatingActionMenu) findViewById(R.id.menu);
        setSupportActionBar(toolbar);

        drawerToggle = new ActionBarDrawerToggle(MapsActivity.this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.addDrawerListener(drawerToggle);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        drawerToggle.syncState();


    }

    @Override
    protected void onStart() {
        super.onStart();
        ex = getIntent().getExtras();
        mSocket.connect();//Connect to server handler real-time
        mSocket.on(Constant.TAG_LISTEN_REPORT, handlerNewReportMessage);
        mSocket.on("removeReport", handlerRemoveReport);
        //Utilities.startJobService(this);

        final CircleImageView avatar = (CircleImageView) header.findViewById(R.id.profile_image);

        TextView user_name = (TextView) header.findViewById(R.id.user_name);
        TextView phone_number = (TextView) header.findViewById(R.id.phone_number);

        queue = Volley.newRequestQueue(mContext);
        currentUser = new Users();
        shortUsers = new ShortUsers();
        boolean first = setting.getBoolean(Constant.CODE_FIRST_TIME, false);

        String d = new String();

        if (!first) {
            d = ex.getString("user", "");
        } else {
            String saveUser = setting.getString("user", null);
            if (saveUser != null) {
                d = saveUser;
            }
        }

        try {
            JSONObject userData = new JSONObject(d);

            if (userData.has("address")) {

                String a = userData.getString("address");

                if (a != null && a != "" && a != "null") {
                    currentUser.set_address(a);
                    JSONArray placeAddress = new JSONArray(a);

                    for (int i = 0; i < placeAddress.length(); i++) {
                        JSONObject jsonObject = placeAddress.getJSONObject(i);

                        int pos_change = jsonObject.getInt("pos");
                        Places p = list.get(pos_change);
                        p.setSummary(jsonObject.getString("address"));
                    }
                }

            }

            String _firstname = userData.getString(UserField.FIRST_NAME);
            String _lastname = userData.getString(UserField.LAST_NAME);
            String _email = userData.getString(UserField.EMAIL);
            String _phone = userData.getString(UserField.PHONE);
            int _id = userData.getInt(UserField.ID);
            int _notice = userData.getInt("notice");
            String phone = userData.getString(UserField.EMAIL);

            currentUser.set_Id(_id);
            shortUsers.set_id(_id);
            currentUser.set_firstName(_firstname);
            shortUsers.setFirstName(_firstname);
            user_name.setText(_firstname + " " + _lastname);
            phone_number.setText(phone);
            currentUser.set_email(_email);
            currentUser.set_phoneNumber(_phone);
            if (_notice == 1) currentUser.setNotice(true);
            shortUsers.setLastName(_lastname);
            currentUser.set_urlProfileImage(new JSONObject(userData.getString(UserField.IMAGES)).getString(UserField.AVATAR_URL));
            shortUsers.setUrlImageProfile(new JSONObject(userData.getString(UserField.IMAGES)).getString(UserField.AVATAR_URL));

            final Gson gson = new Gson();

            final Type type = new TypeToken<ShortUsers>() {
            }.getType();

            jsonCurrentUser = gson.toJson(shortUsers, type);

            ImageRequest imageRequest = new ImageRequest(Constant.SERVER_GET_API + "upload/" + currentUser.get_urlProfileImage(), new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    currentUser.set_profileImage(response);
                    avatar.setImageBitmap(response);
                    avatar.buildDrawingCache();
                    avatar.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivity(
                                    new Intent(mContext, SettingProfile.class),
                                    ActivityOptionsCompat.makeSceneTransitionAnimation(MapsActivity.this, avatar, "profile").toBundle()
                            );
                        }
                    });
                }
            }, 100, 100, ImageView.ScaleType.CENTER, Bitmap.Config.ALPHA_8, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

            queue.add(imageRequest);


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        mSocket.off();
        queue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
        queue.stop();
    }

    /**
     * Delete marker.
     */

    private Emitter.Listener handlerRemoveReport = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        String _id = data.getString("reportId");
                        MarkerReport markerReport = hashMapReport.get(_id);

                        if (markerReport != null) {
                            clusterManager.removeItem(markerReport);
                            hashMapReport.remove(_id);
                            float zoom = mMap.getCameraPosition().zoom;
                            mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom + _zoomOffset));
                            _zoomOffset = -_zoomOffset;
                        }

                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    };

    private Emitter.Listener handlerNewReportMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            MapsActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    try {
                        Double longtitue = data.getDouble(ReportField.RP_LONGTITUE);
                        Double latitue = data.getDouble(ReportField.RP_LATTITUE);
                        String _id = data.getString(ReportField.RP_ID);
                        int state = data.getInt(ReportField.RP_STATE);
                        int type = data.getInt(ReportField.RP_TYPE);

                        String title = Utilities.getStateReport(mContext, state);

                        MarkerReport item = new MarkerReport(latitue, longtitue, title, data.toString(), Utilities.getResourceMarker(type));

                        clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MarkerInfoWindowAdapter());

                        clusterManager.setRenderer(new MarkerRender(MapsActivity.this, mMap, clusterManager));

                        clusterManager.addItem(item);

                        hashMapReport.put(_id, item);

                    } catch (JSONException ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    };

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constant.CODE_REQUEST_FINE_LOC) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                    if (crrLoc != null) {
                        LatLng loc = new LatLng(crrLoc.getLatitude(), crrLoc.getLongitude());
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 15));
                        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("notifications_new_message", true)) {
                            startService(new Intent(MapsActivity.this, BackgroundService.class));
                            Utilities.startJobService(mContext);
                        }
                    }

                }
        }
    }*/

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(0, 150, 0, 150);
        mMap.setOnCameraIdleListener(this);
        clusterManager = new ClusterManager<>(this, mMap);

      /*  if (getIntent().hasExtra(Constant.EXTRA_LATLNG)) {
            LatLng prev = ex.getParcelable(Constant.EXTRA_LATLNG);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(prev));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(prev, 15));
        }*/

      /*  mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(final Marker marker) {
                new AlertDialog.Builder(MapsActivity.this)
                        .setTitle(getString(R.string.delete_report))
                        .setMessage(getString(R.string.del_report_message))
                        .setNegativeButton(android.R.string.cancel, null) // dismisses by default
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    JSONObject info = new JSONObject(marker.getSnippet());
                                    String _id = info.getString("_id");

                                    JSONObject put = new JSONObject();

                                    put.put("reportId",_id);
                                    mSocket.emit("removeReport",put);
                                    MarkerReport hr = hashMapReport.get(_id);

                                    if(hr!=null){
                                        hashMapReport.remove(_id);
                                        clusterManager.removeItem(hr);

                                        float zoom = mMap.getCameraPosition().zoom;
                                        mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom + _zoomOffset));
                                        _zoomOffset = - _zoomOffset;
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                .create()
                .show();

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });*/

        mMap.setOnMarkerClickListener(clusterManager);
        mMap.setOnCameraChangeListener(clusterManager);
        mMap.setInfoWindowAdapter(clusterManager.getMarkerManager());
        mMap.setOnInfoWindowClickListener(clusterManager);

        clusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MarkerReport>() {
            @Override
            public boolean onClusterItemClick(MarkerReport markerReport) {
                _markerReport = markerReport;
                return false;
            }
        });

        clusterManager.setOnClusterItemInfoWindowClickListener(new ClusterManager.OnClusterItemInfoWindowClickListener<MarkerReport>() {
            @Override
            public void onClusterItemInfoWindowClick(MarkerReport markerReport) {
                Intent intent = new Intent(MapsActivity.this, ReportDetailActivity.class);
                String reportData;
                Gson gson = new Gson();
                Type type = new TypeToken<MarkerReport>() {
                }.getType();
                reportData = gson.toJson(markerReport, type);
                intent.putExtra("REPORT_DATA", reportData);
                intent.putExtra("currentUser", jsonCurrentUser);
                startActivity(intent);
            }
        });

       /* if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, Constant.CODE_REQUEST_FINE_LOC);
        } else {
            mMap.setMyLocationEnabled(true);
        }*/
    }

    private void sendRequest(String des) {

        if (crrLoc != null) {
            String origin = crrLoc.getLatitude() + "," + crrLoc.getLongitude();
            String destination = des;
            if (origin.isEmpty()) {
                Toast.makeText(this, "Please enter origin address!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (destination.isEmpty()) {
                Toast.makeText(this, "Please enter destination address!", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                new DirectionFinder(this, origin, destination).execute();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, getString(R.string.require_location), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onDirectionFinderStart() {
        if (originMarkers != null) {
            for (Marker marker : originMarkers) {
                marker.remove();
            }
        }

        if (destinationMarkers != null) {
            for (Marker marker : destinationMarkers) {
                marker.remove();
            }
        }

        if (polylinePaths != null) {
            for (Polyline polyline : polylinePaths) {
                polyline.remove();
            }
        }
    }

    /**
     * @auth ypn
     * @desc drawn route when find path successfully.
     * @param routes
     */

    private List<Route> routes;

    @Override
    public void onDirectionFinderSuccess(List<Route> routes) {
        polylinePaths = new ArrayList<>();
        originMarkers = new ArrayList<>();
        destinationMarkers = new ArrayList<>();

        for (Route route : routes) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(route.startLocation, 15));

            originMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.startAddress)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination))
                    .position(route.startLocation)));
            destinationMarkers.add(mMap.addMarker(new MarkerOptions()
                    .title(route.endAddress)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.destination))
                    .position(route.endLocation)));

            PolylineOptions polylineOptions = new PolylineOptions().
                    geodesic(true).
                    color(Color.parseColor("#28ABEC")).
                    width(15);

            for (int i = 0; i < route.points.size(); i++)
                polylineOptions.add(route.points.get(i));

            polylinePaths.add(mMap.addPolyline(polylineOptions));

            tv_distance.setText(route.distance.text);
            tv_time.setText(route.duration.text);
            tv_dest_name.setText(dest);
            tv_dest_address.setText(route.endAddress);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(route.startLocation);
            builder.include(route.endLocation);
            LatLngBounds bounds = builder.build();

            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 75);
            mMap.animateCamera(cu);

            this.routes = routes;
        }
    }

    /**
     * @auth ypn
     * @desc show warning message when user entry invalid location.
     */
    @Override
    public void onLocationNotFound() {
        final Handler h = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Toast.makeText(MapsActivity.this, R.string.location_not_found, Toast.LENGTH_SHORT).show();
            }
        };
        h.sendMessageDelayed(new Message(), 1000);
    }

    /**
     * @auth ypn
     * @desc show warning message when user entry valid location but cannot find route.
     */
    @Override
    public void onRouteNotFound() {
        final Handler h = new Handler() {
            @Override
            public void handleMessage(Message message) {
                Toast.makeText(MapsActivity.this, R.string.route_not_found, Toast.LENGTH_SHORT).show();
            }
        };
        h.sendMessageDelayed(new Message(), 1000);
    }

    @Override
    public void onClick(View view) {

        Intent report_police = new Intent(this, ReportPoliceActivity.class);
        report_police.putExtra("user", jsonCurrentUser);
        switch (view.getId()) {
            case R.id.btn_report_police:
                report_police.putExtra("type", Constant.REPORT_TYPE_POLICE);
                break;
            case R.id.btn_report_camera:
                report_police.putExtra("type", Constant.REPORT_TYPE_CAMERA);
                break;
            case R.id.btn_report_trafic_accident:
                report_police.putExtra("type", Constant.REPORT_TYPE_ACCIDENT);
                break;
            case R.id.btn_report_trafic_jam:
                report_police.putExtra("type", Constant.REPORT_TYPE_TRAFFIC_JAM);
                break;
        }
        startActivityForResult(report_police, 1992);

        menu.close(false);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1992 && resultCode == RESULT_OK){
            Bundle extra = data.getExtras();
            if (extra != null) {
                try {
                    JSONObject pos = new JSONObject(extra.getString(Constant.EXTRA_REPORT_COMPLETE));
                    Double lat = pos.getDouble(ReportField.RP_LATTITUE);
                    Double lon = pos.getDouble(ReportField.RP_LONGTITUE);
                    String _id = pos.getString(ReportField.RP_ID);
                    int state = pos.getInt(ReportField.RP_STATE);
                    int type = pos.getInt(ReportField.RP_TYPE);

                    LatLng newLatlng = new LatLng(lat, lon);

                    String title = Utilities.getStateReport(mContext, state);

                    MarkerReport item = new MarkerReport(lat, lon, title, pos.toString(), Utilities.getResourceMarker(type));

                    if (clusterManager != null) {
                        clusterManager.addItem(item);

                        clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MarkerInfoWindowAdapter());


                        clusterManager.setRenderer(new MarkerRender(MapsActivity.this, mMap, clusterManager));

                        hashMapReport.put(_id, item);

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newLatlng, 15));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }
    }

    @Override
    public void onCameraIdle() {
        LatLng _request = mMap.getCameraPosition().target;
        if(mMap.getCameraPosition().zoom < 12 ){
            mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
        }
        getMarkerByLatLgn(_request);
    }


    /**
     * Desc :Load report in range of location.
     * @param _request
     */
    protected void getMarkerByLatLgn(LatLng _request){

        int range = setting.getInt(Constant.CODE_RANGE,5000);

        String _param ="";
        _param += "lat="  + _request.latitude;
        _param += "&lng=" + _request.longitude;
        _param += "&radius=" + range;

        StringRequest dataReport = new StringRequest(Request.Method.GET, Constant.SERVER_GET_API + "api_get_report?" + _param, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    if(response!=null){
                        for (int i = 0; i < jsonArray.length() ; i++){
                            JSONObject jo = jsonArray.getJSONObject(i);
                            String _id = jo.getString(ReportField.RP_ID);
                            if(!hashMapReport.containsKey(_id)){
                                Double lat = jo.getDouble(ReportField.RP_LATTITUE);

                                Double lon = jo.getDouble(ReportField.RP_LONGTITUE);

                                int state = jo.getInt(ReportField.RP_STATE);
                                int type = jo.getInt(ReportField.RP_TYPE);

                                String title = Utilities.getStateReport(mContext,state);

                                MarkerReport item = new MarkerReport(lat,lon,title,jo.toString(),Utilities.getResourceMarker(type));


                                clusterManager.addItem(item);

                                clusterManager.getMarkerCollection().setOnInfoWindowAdapter(new MarkerInfoWindowAdapter());

                                clusterManager.setRenderer(new MarkerRender(MapsActivity.this,mMap,clusterManager));

                                hashMapReport.put(_id,item);

                            }
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
        });

        queue.add(dataReport);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("current_dest_direction",current_desc);
        outState.putString("dest",dest);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        dest = savedInstanceState.getString("dest");
        if(savedInstanceState.getString("current_dest_direction")!=null){
            current_desc = savedInstanceState.getString("current_dest_direction");
            sendRequest(current_desc);
        }
    }

    private static final int TIME_DELAY = 2000;
    private static long back_pressed;


    @Override
    public void onBackPressed() {
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){

            if (originMarkers != null) {
                for (Marker marker : originMarkers) {
                    marker.remove();
                }
            }

            if (destinationMarkers != null) {
                for (Marker marker : destinationMarkers) {
                    marker.remove();
                }
            }

            if (polylinePaths != null) {
                for (Polyline polyline : polylinePaths) {
                    polyline.remove();
                }
            }

            current_desc =null;

            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        }else{
            if (back_pressed + TIME_DELAY > System.currentTimeMillis()) {
                super.onBackPressed();
            } else {
                Toast.makeText(getBaseContext(), getString(R.string.click_again_to_exist),
                        Toast.LENGTH_SHORT).show();
            }
            back_pressed = System.currentTimeMillis();
        }

    }
}
