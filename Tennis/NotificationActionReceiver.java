package receivers;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.veriah.tennisquare.MyTennisSquare;
import com.veriah.tennisquare.R;
import org.json.JSONException;
import org.json.JSONObject;

import CommonUtils.Constants;
import WebServices.HttpPutRequest;

/**
 * Created by vasilis fouroulis on 14/02/2017. LOVE
 * Define the actions of the Notification buttons
 */
public class NotificationActionReceiver extends BroadcastReceiver{
    private static final String TAG = "NotificationActions";

    private static final String CHALLENGE_ID = "challenge_id";
    private static final String BROADCAST_INTENT_ACCEPT = "accept_challenge_broadcast_action";
    private static final String BROADCAST_INTENT_CANCEL = "cancel_challenge_broadcast_action";

    @Override
    public void onReceive(Context context, Intent intent) {

        Bundle extras = intent.getExtras();

        if(extras!= null && extras.getBoolean(BROADCAST_INTENT_ACCEPT,false)){
            acceptChallenge(extras.getString(CHALLENGE_ID),context); Log.d(TAG,"accept challenge"); // Accept Challenge
        } else if(extras != null && !extras.getBoolean(BROADCAST_INTENT_ACCEPT,false)){
            denyChallenge(extras.getString(CHALLENGE_ID),context); Log.d(TAG,"deny challenge"); // Deny Challenge
        } else if(extras!= null  && extras.getBoolean(BROADCAST_INTENT_CANCEL,false)){
            cancelBooking(extras.getString(CHALLENGE_ID),context); Log.d(TAG,"cancel booking"); // cancel booking
        }
    }

    public void acceptChallenge(String id,final Context context){
        String body = "token=" +((MyTennisSquare)context.getApplicationContext()).getSharedPreferencesCustom().getString(Constants.TOKEN,"");
        new HttpPutRequest(new HttpPutRequest.MyAsyncTaskListener() {
            @Override
            public void onPreExecutelistener() {

            }

            @Override
            public void onPostExecutelistener(String result, int responseCode) {
                Log.d(TAG,result);
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);

                    if(jsonObject.getBoolean("res")) {
                        Toast.makeText(context, context.getString(R.string.notification_action_accept_chal), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string.notification_action_deny_chal), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }).execute(Constants.WEB_SERVICES_ACCEPT_CHALLENGE.replace("@ID@",id),body);
    }

    public void denyChallenge(String id,final Context context){
        String body = "token=" +((MyTennisSquare)context.getApplicationContext()).getSharedPreferencesCustom().getString(Constants.TOKEN,"");
        new HttpPutRequest(new HttpPutRequest.MyAsyncTaskListener() {
            @Override
            public void onPreExecutelistener() {

            }

            @Override
            public void onPostExecutelistener(String result, int responseCode) {
                Log.d(TAG,result);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);

                    if(jsonObject.getBoolean("res")) {
                        Toast.makeText(context, context.getString(R.string.notification_action_deny_chal), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string.notification_action_deny_chal), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute(Constants.WEB_SERVICES_DECLIN_CHALLENGE.replace("@ID@",id),body);
    }

    public void cancelBooking(String id, final Context context){
        String body = "token=" +((MyTennisSquare)context.getApplicationContext()).getSharedPreferencesCustom().getString(Constants.TOKEN,"");
        new HttpPutRequest(new HttpPutRequest.MyAsyncTaskListener() {
            @Override
            public void onPreExecutelistener() {

            }

            @Override
            public void onPostExecutelistener(String result, int responseCode) {
                Log.d(TAG,result);

                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);

                    if(jsonObject.getBoolean("res")) {
                        Toast.makeText(context, context.getString(R.string.notification_action_deny_chal), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(context, context.getString(R.string.notification_action_deny_chal), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute(Constants.WEB_SERVICES_CANCEL_BOOKING.replace("@ID@",id),body);
    }

}
