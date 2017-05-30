package services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Log;
import com.alarm.veriah.FamilyApp.R;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import javax.net.ssl.HttpsURLConnection;
import Helper.Constants;
import Helper.GD;
import Model.ContactItem;

/**
 * Created by vasilis Fouroulis on 8/10/2016.
 */
/**
 * Listener for geofenceIB transition changes.
 *
 * Receives geofenceIB transition events from Location Services in the form of an Intent containing
 * the transition type and geofenceIB id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class GeofenceService extends Service {

    protected static final String TAG = "GeofenceService";
    private SharedPreferences sharedPreferences;
    private String notificationDetails;
    private Geocoder geoCoder;
    GD gd = GD.get();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sharedPreferences = getSharedPreferences(Constants.SHARRED_KEY_PREFERENCES_KEY, Context.MODE_PRIVATE);

    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent,int flag, int startId) {

        if(intent != null) {

            GeofencingEvent geofencingEvent;
            geofencingEvent = GeofencingEvent.fromIntent(intent);

            // Get the transition type.
            int geofenceTransition = geofencingEvent.getGeofenceTransition();

            // Test that the reported transition was of interest.
            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER || geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

                // Get the geofences that were triggered. A single event can trigger multiple geofences.
                List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

                // Get the transition details as a String.
                String geofenceTransitionDetails = getGeofenceTransitionDetails(
                        getApplicationContext(),
                        geofenceTransition,
                        triggeringGeofences
                );
                notificationDetails = geofenceTransitionDetails;

                sendPushNotification(geofencingEvent.getTriggeringLocation(), geofencingEvent.getGeofenceTransition());
            }
        }

        return START_STICKY;

    }

    /**
     * Maps geofenceIB transition types to their human-readable equivalents.
     * @param transitionType    A transition type constant defined in Geofence
     * @return                  A String indicating the type of transition
     */
    private String getTransitionString(Context context,int transitionType) {

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return context.getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return context.getString(R.string.geofence_transition_exited);
            default:
                return context.getString(R.string.unknown_geofence_transition);
        }
    }

    private String getTransitionString(int transitionType) {

        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return "enterZone";
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return "exitZone";
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    private String getGeofenceTransitionDetails(Context context, int geofenceTransition, List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(context,geofenceTransition);

        // Get the Ids of each geofenceIB that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ",  triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**Make an HTTP Call to the Web Service in order to upload the contacts to a specific user.*/
    public void sendPushNotification(Location location, int tranzition)  {

        List<android.location.Address> addresses;
        geoCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        String address="";
        String city ="";


        try {
            addresses = geoCoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            city = addresses.get(0).getLocality();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url  = Constants.SERVER_URL+ Constants.WEB_SERVICES_SMS_ZONE_TRANZITION;

        String body =   "token="   +sharedPreferences.getString(Constants.TOKEN,"")
                +"&userName="      +sharedPreferences.getString(Constants.USERNAME,"")
                +"&familyId="      +sharedPreferences.getString(Constants.PROFILE_FAMILY_ID,"")
                +"&zoneId="        +""
                +"&gpsLat="        + location.getLatitude()
                +"&gpsLon="        + location.getLongitude()
                +"&address="       + address + " " + city
                +"&gmtDiff="       + getOffset()
                +"&zoneTransType=" + getTransitionString(tranzition)
                +"&message="       + notificationDetails ;

        new HttpGeofence().execute(url,body); /*** Send push notifications ***/
    }

    public void sendSMSNoData() {
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> parts = smsManager.divideMessage(notificationDetails);
        Log.d("SendsmsService", ":: MESSAGE no data ::" + notificationDetails);
        for (ContactItem ci : gd.contactListGD) {
            smsManager.sendMultipartTextMessage(ci.getMobile(), null, parts, null, null);
        }
    }

    public int getOffset(){
        TimeZone timezone = TimeZone.getDefault();
        int seconds = timezone.getOffset(Calendar.ZONE_OFFSET)/1000;

        return seconds;
    }

    protected class HttpGeofence extends AsyncTask<String,Integer,Object>{
        String endPoint;
        @Override
        public Object doInBackground(String... params) {

            URL url;
            String response = "";
            int responseCode= 0;
            try {
                endPoint = params[0];
                url = new URL(endPoint);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept", "" + "application/json");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                String body = params[1];

                Log.d(TAG,"body"+body);
                OutputStream output = new BufferedOutputStream(conn.getOutputStream());
                output.write(body.getBytes());
                output.flush();

                responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }

                    Log.d(TAG, "" + response);

                }
                else {
                    response="";
                    Log.d("TAG", "" + responseCode);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return response;

        }

        @Override
        protected void onPostExecute(Object result) {
            try {
                JSONObject json = new JSONObject(result.toString());
                Log.d(TAG,result.toString());

            } catch (JSONException e) {
                e.printStackTrace();
                //Send sms from the Phone
//                sendSMSNoData();
            }
        }
    }
}
