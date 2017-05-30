package com.alarm.veriah.FamilyApp.zones;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alarm.veriah.FamilyApp.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import Helper.Constants;
import Utils.FontCache;
import Helper.GD;
import Model.ContactItem;
import Adapter.ContactsAdapterGeofence;


public class ZoneDetails extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener, LocationListener {
    private static final String TAG = "ZoneDetailsTAG";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private MapFragment mapFragment;
    private SeekBar seekBar;
    private GD gd = GD.get();
    private float radius;
    private TextView radiusTV;
    private Circle circle;
    private Marker marker;
    private double gpsLat, gpsLon;
    private String requestURL;
    protected HttpfamilyZonesCreateIncide HttpFamilyZonesCreate;
    protected HttpfamilyZonesUpdateIncide HttpfamilyZonesUpdate;
    private String id, name;
    private boolean newZone;
    private RelativeLayout rLEditNameZone;
    private EditText tvEditNameZone;
    private RecyclerView rVContactList;
    private LinearLayoutManager layoutManager;
    private ContactsAdapterGeofence mAdapter;
    private ArrayList<String> ids;
    private ScrollView mScrollView;
    private ImageView transparentImageView, removeEditText;
    private LatLng newPosition;
    private EditText editTextSearch;
    private Geocoder geoCoder;
    private GoogleMap mGoogleMap;
    private InputMethodManager imm;
    private InputMethodManager inputMethod;
    private String members;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private double lat, lon;
    private Location mLastLocation;
    private Activity activity;
    private MenuItem mMenuItem;
    private boolean modeEdit = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.zone_details_act);
        sharedPreferences = getSharedPreferences(Constants.SHARRED_KEY_PREFERENCES_KEY, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        Intent intent = getIntent();
        newZone = intent.getBooleanExtra("newZone", false);
        imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        ids = new ArrayList<>();
        activity = this;

        buildGoogleApiClient();         //Initialize mGoogleApiClient

        seekBar              = (SeekBar)        findViewById(R.id.map_geofence_seekbar);
        radiusTV             = (TextView)       findViewById(R.id.radius_info);
        rLEditNameZone       = (RelativeLayout) findViewById(R.id.ll_edittext);
        tvEditNameZone       = (EditText)       findViewById(R.id.tt_edit_name_zone);
        rVContactList        = (RecyclerView)   findViewById(R.id.recycler_view_contacts);
        mScrollView          = (ScrollView)     findViewById(R.id.scroll_view);
        transparentImageView = (ImageView)      findViewById(R.id.transparent_image);
        editTextSearch       = (EditText)       findViewById(R.id.et_search_address);
        removeEditText       = (ImageView)      findViewById(R.id.remove_text);

        initToolbar();
        initFont(FontCache.get(Constants.APP_TYPEFACE,this));

        if (newZone) {
            newZoneInit();
        } else {
            editZoneInit(getIntent());
        }

        radiusTV.setText("" + (int) radius  + " m" );
        seekBar.setProgress((int) radius / 10  - 10);

        /** Recycler view **/
        layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        rVContactList.setLayoutManager(layoutManager);
        mAdapter = new ContactsAdapterGeofence(gd.contactListForZonesGD, FontCache.get(Constants.APP_TYPEFACE,this), this, members);
        rVContactList.setAdapter(mAdapter);
        mAdapter.setOnItemCustomClickListener(new ContactsAdapterGeofence.OnItemCustomClickListener() {
            @Override
            public void onItemClick(View view, int Position) {
                if (gd.contactListForZonesGD != null) {
                    onContactListClick(gd.contactListForZonesGD.get(Position), view, Position);
                }
            }
        });

        seekBar.setMax(90);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radius = progress * 10 + 100;
                radiusTV.setText("" + (int) radius + " m");

                if (circle != null) {
                    circle.setRadius(radius);
                }

                Log.d(TAG, "" + radius);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        editTextSearch.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int Keycode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (Keycode == KeyEvent.KEYCODE_SEARCH) || (Keycode == KeyEvent.KEYCODE_ENTER)) {
                    inputMethod = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethod.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
                    String key = editTextSearch.getText().toString();
                    if (key != null && !key.isEmpty()) {
                        List<android.location.Address> addresses;

                        try {
                            addresses = geoCoder.getFromLocationName(editTextSearch.getText().toString(), 1);

                            if (addresses.size() > 0) {
                                Double lat = (double) (addresses.get(0).getLatitude());
                                Double lon = (double) (addresses.get(0).getLongitude());
                                final LatLng user = new LatLng(lat, lon);
                                Log.d(TAG,"addresses LAT "+lat+" ,LON:"+lon );
                                getLocation(mGoogleMap,lat,lon,true);

                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                return false;
            }
        });

        transparentImageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disallow ScrollView to intercept touch events.
                        mScrollView.requestDisallowInterceptTouchEvent(true);
                        // Disable touch on transparent view
                        return false;

                    case MotionEvent.ACTION_UP:
                        // Allow ScrollView to intercept touch events.
                        mScrollView.requestDisallowInterceptTouchEvent(false);
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        mScrollView.requestDisallowInterceptTouchEvent(true);
                        return false;

                    default:
                        return true;
                }
            }
        });
        /** MAP FRAGMENT INIT **/
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map_geofence);

        removeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextSearch.setText("");
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_geofence_activity, menu);
        mMenuItem = menu.findItem(R.id.add_zone);

        if(!newZone){
            mMenuItem.setIcon(R.drawable.edit_location);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case android.R.id.home:
                ZoneDetails.this.finish();
                ZoneDetails.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                return true;
            case R.id.add_zone:
                name = tvEditNameZone.getText().toString();
                if (!(name.equals("") || name.equals(" "))) {
                    if (newZone) {
                        addZone();
                    } else {

                        if(modeEdit) {
                            updateZone();
                        } else {
                            mMenuItem.setIcon(R.drawable.ic_save);
                            modeEdit = true;
                            tvEditNameZone.setEnabled(true);
                            seekBar.setEnabled(true);
                        }
                    }

                } else {
                    Toast.makeText(ZoneDetails.this, getResources().getString(R.string.toast_message_no_zone_name), Toast.LENGTH_LONG).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(10000); // Update location every second

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {

                List<android.location.Address> addresses;
                geoCoder = new Geocoder(activity, Locale.getDefault());
                String address = "";
                String city = "";

                mGoogleMap = googleMap;

                UiSettings uiSettings = mGoogleMap.getUiSettings();
                uiSettings.setZoomControlsEnabled(true);
                uiSettings.setCompassEnabled(false);

                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                        mGoogleApiClient);
                if (mLastLocation != null) {

                    if (newZone) {
                        lat = mLastLocation.getLatitude();
                        lon = mLastLocation.getLongitude();

                        try {
                            addresses = geoCoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                            city = addresses.get(0).getLocality();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        editTextSearch.setText(address + " " + city);

                        getLocation(mGoogleMap, lat, lon,modeEdit);
                    } else {
                        getLocation(mGoogleMap, gpsLat, gpsLon,modeEdit);
                    }
                }
            }
        });

    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
        Toast.makeText(this, getString(R.string.error_no_location), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lon = location.getLongitude();

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (circle != null) {
            circle.remove();
            marker.remove();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        activity = null;
    }

    public void initFont(Typeface typeface){
        editTextSearch.setTypeface(typeface);
        radiusTV.setTypeface(typeface);
        tvEditNameZone.setTypeface(typeface);
    }

    public void initToolbar(){
        Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final Drawable upArrow = getResources().getDrawable(R.drawable.action_bar_back);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        TextView toolbarTextView = (TextView) mToolbar.findViewById(R.id.toolbar_tv);
        toolbarTextView.setVisibility(View.VISIBLE);
        toolbarTextView.setTypeface(FontCache.get(Constants.APP_TYPEFACE,this));
        toolbarTextView.setText(getResources().getString(R.string.actionbar_geofence));
    }

    public void updateZone() {
        if (radius > 0.0) {
            if (circle != null) {
                newPosition = circle.getCenter();
                HttpfamilyZonesUpdate = new HttpfamilyZonesUpdateIncide();
                if (gd.isConnected(this)) {
                    HttpfamilyZonesUpdate.execute(this);
                } else {
                    Toast.makeText(this, getString(R.string.wrong), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getString(R.string.error_no_location), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.error_message_radious_0), Toast.LENGTH_LONG).show();
        }

    }

    public void addZone() {
        if (radius > 0.0) {
            if (circle != null) {
                newPosition = circle.getCenter();
                HttpFamilyZonesCreate = new HttpfamilyZonesCreateIncide();
                if (gd.isConnected(this)) {
                    HttpFamilyZonesCreate.execute(this);
                } else {
                    Toast.makeText(this, getString(R.string.wrong), Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(this, getString(R.string.error_no_location), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this, getString(R.string.error_message_radious_0), Toast.LENGTH_LONG).show();
        }
    }

    public void onContactListClick(ContactItem item, View view, int position) {
        String contactId = item.getId();

        if(modeEdit || newZone) {
            if (ids.contains(contactId)) {
                ids.remove(contactId);
                Log.d(TAG, ids.toString());
                item.setSelectedforGeofence(false);
                mAdapter.notifyItemChanged(position);
            } else {
                ids.add(contactId);
                Log.d(TAG, ids.toString());
                item.setSelectedforGeofence(true);
                mAdapter.notifyItemChanged(position);
            }
        }
    }

    synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    //vf: 04/08/2016         Init and Adding Circles
    private void initMarker(GoogleMap googleMap) {
        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.flag);
        /** Adding Default Circle With radius 10000 m **/

        if (newZone) {
            gpsLat = lat;
            gpsLon = lon;
        }

        marker = googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(gpsLat, gpsLon))
                .icon(icon)
                .title("Zone")
                .draggable(false)
                .snippet("Drag to change zone"));

    }

    //vf: 04/08/2016         Init and Adding Circles                                                 *
    private void initCircles(GoogleMap googleMap) {
        /** Adding Default Circle With radius 10000 m **/

        if (newZone) {
            gpsLat = lat;
            gpsLon = lon;
        }

        circle = googleMap.addCircle(new CircleOptions()
                .center(new LatLng(gpsLat, gpsLon))
                .radius(radius)
                .strokeColor(getResources().getColor(R.color.color_stroke_circle))
                .fillColor(0x451181fe));
    }

    //vf: 04/08/2016         Getting Log Lat - Zoom Google Map                                       *
    public void getLocation(final GoogleMap googleMap, double lat, double lon,final boolean edit) {

        googleMap.animateCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(lat, lon), 14.0f));

        googleMap.setMyLocationEnabled(true);
        initCircles(googleMap);
        initMarker(googleMap);
        // vf: center of the map 19/09/2016
        googleMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                LatLng centerOfMap = googleMap.getCameraPosition().target;
                if(edit) {
                    circle.setCenter(centerOfMap);
                    marker.setPosition(centerOfMap);
                }

                List<android.location.Address> addresses;
                geoCoder = new Geocoder(activity, Locale.getDefault());

                String address = "";
                String city = "";

                try {
                    addresses = geoCoder.getFromLocation(centerOfMap.latitude, centerOfMap.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    city = addresses.get(0).getLocality();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                editTextSearch.setText(address + " " + city);

                /** vf: 20 / 10 / 2016  update the edittext of the address**/

            }
        });
    }

    public void newZoneInit(){
        requestURL = Constants.SERVER_URL + Constants.WEB_SERVICES_GEOFENCE_ADD_ZONE;
        Log.d(TAG, "INTENT: ADD_ZONE");
        radius = 200; //Default Radius
    }

    public void editZoneInit(Intent intent){

        List<android.location.Address> addresses;
        geoCoder = new Geocoder(this, Locale.getDefault());
        String address = "";
        String city = "";

        requestURL = Constants.SERVER_URL + Constants.WEB_SERVICES_GEOFENCE_UPDATE_ZONE;
        id = intent.getStringExtra("id");

        gpsLat = intent.getDoubleExtra("gpsLat", 0.0);
        gpsLon = intent.getDoubleExtra("gpsLon", 0.0);
        radius = (float) intent.getDoubleExtra("radius", 200);
        name = intent.getStringExtra("name");
        members = intent.getStringExtra("members");
        String[] splittedMembers = members.split(",");

        for (int i = 0; i < splittedMembers.length; i++) {
            Log.d(TAG, splittedMembers[i]);
            ids.add(i, splittedMembers[i]);
        }

        tvEditNameZone.setText(name);
        tvEditNameZone.setEnabled(false);
        seekBar.setEnabled(false);

        try {
            addresses = geoCoder.getFromLocation(gpsLat, gpsLon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            city = addresses.get(0).getLocality();
        } catch (Exception e) {
            e.printStackTrace();
        }
        editTextSearch.setText(address + " " + city);
    }

    protected class HttpfamilyZonesCreateIncide extends AsyncTask {

        @Override
        public String doInBackground(Object[] params) {

            URL url;
            String response = "";
            int responseCode = 0;
            try {
                url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "" + "application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                String familyMemberIds = "";
                for (int i = 0; i < ids.size(); i++) {
                    if (i == 0) {
                        familyMemberIds = ids.get(0);
                    } else {
                        familyMemberIds = familyMemberIds + "," + ids.get(i);
                    }
                }
                Log.d(TAG, "familyMemberIds===" + familyMemberIds);

                //// TODO: 9/7/2016 familyMembersIds
                String body = "token=" + sharedPreferences.getString(Constants.TOKEN, "")
                        + "&userName=" + sharedPreferences.getString(Constants.USERNAME, "")
                        + "&radius=" + radius
                        + "&gpsLat=" + newPosition.latitude
                        + "&gpsLon=" + newPosition.longitude
                        + "&nameZone=" + name
                        + "&familyId=" + sharedPreferences.getString(Constants.PROFILE_FAMILY_ID, "")
                        + "&familyMemberIds=" + familyMemberIds;
                Log.d(TAG, "radious:: " + radius);
                Log.d(TAG, "body" + body);
                OutputStream output = new BufferedOutputStream(conn.getOutputStream());
                output.write(body.getBytes());
                output.flush();

                responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }

//                    Log.d(TAG, "HttpfamilyZonesCreateIncide response" + response);

                } else {
                    response = "";
                    Log.d("TAG", "HttpfamilyZonesCreateIncide unsuccessful responseCode:" + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response + "";

        }

        @Override
        protected void onPostExecute(Object result) {
            try {
                JSONObject json = new JSONObject(result.toString());
                String resp = json.getString("response");
                boolean res = json.getBoolean("res");


                if (res) {
                    Log.d(TAG, result.toString());
                    if(activity!=null)Toast.makeText(activity, getString(R.string.success_create_zone), Toast.LENGTH_LONG).show();
                    if(activity!=null)activity.finish();
                    if(activity!=null)activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                } else {
                    Toast.makeText(activity, getString(R.string.error_create_zone), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    protected class HttpfamilyZonesUpdateIncide extends AsyncTask {

        @Override
        public String doInBackground(Object[] params) {

            URL url;
            String response = "";
            int responseCode = 0;
            try {
                url = new URL(requestURL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "" + "application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                String familyMemberIds = "";
                for (int i = 0; i < ids.size(); i++) {
                    if (i == 0) {
                        familyMemberIds = ids.get(0);
                    } else {
                        familyMemberIds = familyMemberIds + "," + ids.get(i);
                    }
                }
                Log.d(TAG, "familyMemberIds===" + familyMemberIds);

                String body = "token=" + sharedPreferences.getString(Constants.TOKEN, "")
                        + "&userName=" + sharedPreferences.getString(Constants.USERNAME, "")
                        + "&radius=" + radius
                        + "&gpsLat=" + newPosition.latitude
                        + "&gpsLon=" + newPosition.longitude
                        + "&nameZone=" + name
                        + "&zoneId=" + id
                        + "&familyId=" + sharedPreferences.getString(Constants.PROFILE_FAMILY_ID, "")
                        + "&familyMemberIds=" + familyMemberIds;

                Log.d(TAG, "radious:: " + radius);
                Log.d(TAG, "body" + body);
                OutputStream output = new BufferedOutputStream(conn.getOutputStream());
                output.write(body.getBytes());
                output.flush();

                responseCode = conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                    Log.d(TAG, "HttpfamilyZonesUpdateIncide response" + response);
                } else {
                    response = "";
                    Log.d("TAG", "HttpfamilyZonesUpdateIncide unsuccessful responseCode:" + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response + "";

        }

        @Override
        protected void onPostExecute(Object result) {
            try {
                JSONObject json = new JSONObject(result.toString());
                boolean res = json.getBoolean("res");
                if (res) {
                    if (activity != null) {
                        Toast.makeText(activity, getString(R.string.success_update_zone), Toast.LENGTH_LONG).show();
                        activity.finish();
                        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }

                } else {
                    if (activity != null)
                        Toast.makeText(activity, getString(R.string.error_update_zone), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
