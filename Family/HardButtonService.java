package services;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.media.VolumeProviderCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import com.alarm.veriah.FamilyApp.MainActivity;

import java.util.Date;

import Helper.Constants;
import Helper.GD;

/**
 * Created by Vasilis Fouroulis on 04/05/2017.
 */

public class HardButtonService extends Service {

    public static final String TAG = "HardButtonServiceTAG";
    private SharedPreferences prefs;
    private MediaSessionCompat mediaSession;
    private SharedPreferences.Editor editor;
    private Handler handlerResetCounter = new Handler();
    private static final String COUNTER_SHARRED_PREF = "counter"; //vf: Key to save the counter
    private String token;
    private boolean isOpen;
    private Date acceleratedPrevDate;
    private Date acceleratedCurrDate;
    public static final int NEXT_CALLBACK_DELAY = 4;

    /**
     * VF: Local Broadcast Receiver
     *     Listens for screen on - off
     */
    private BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                getApplication().getSharedPreferences(Constants.SHARRED_KEY_PREFERENCES_KEY, Context.MODE_PRIVATE).edit().putBoolean(Constants.SCREEN_IS_ON,false).apply();
                Log.d(TAG,"ACTION_SCREEN_OFF");
                registerMediaSession(initVolumeProvicer());

            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                getApplication().getSharedPreferences(Constants.SHARRED_KEY_PREFERENCES_KEY, Context.MODE_PRIVATE).edit().putBoolean(Constants.SCREEN_IS_ON,true).apply();
                Log.d(TAG,"ACTION_SCREEN_ON");
               //todo implementation for LG
//                if(prefs.getString(Constants.DEVICE_MANUFACTURER_KEY,"").equals("LGE")){
//                } else {
//
//                }
                KeyguardManager myKM = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);

                if(myKM.isKeyguardLocked()){
                    Log.d(TAG,"isKeyguardLocked");

                } else {
                    Log.d(TAG,"not isKeyguardLocked");

                    unregisterMediaSession();
                }
                //todo check screen lock

            } else if(intent.getAction().equals(Intent.ACTION_USER_PRESENT)){
                unregisterMediaSession();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"INSIDE ON CREATE");
        prefs  = getApplication().getSharedPreferences(Constants.SHARRED_KEY_PREFERENCES_KEY, Context.MODE_PRIVATE);
        editor = prefs.edit();

        token = prefs.getString(Constants.TOKEN,null);
        mediaSession = new MediaSessionCompat(getApplicationContext(), "HardButtonPlayer");
        registerScreenReceiver();

        //vf: Only if the screen is off register media session
        //    Catch the situation where the service is restarting
        if(!prefs.getBoolean(Constants.SCREEN_IS_ON,true)){

            registerMediaSession(initVolumeProvicer());

        }
    }

    /** VF: 1) Unregister Media Session
     *      2) Unregister Screen Receiver
     *      3) Stop Handler **/

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {

            //VF: Unregister media Session and Screen Receiver
            //Only if the user has switch off the HARD BUTTON SETTING
            //because if the App is Killed the Services is restarting
            if(!prefs.getBoolean(Constants.PROFILE_HARD_BUTTON,false)){
                unregisterMediaSession();
                unregisterReceiver(mScreenReceiver);
            }
        } catch (java.lang.IllegalArgumentException e){
            e.toString();
            Log.d(TAG,e.toString());
        }

        handlerResetCounter.removeCallbacksAndMessages(null);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private int getCounter(){
        return prefs.getInt(COUNTER_SHARRED_PREF,0);
    }

    private void setCounter(int counter){
        editor.putInt(COUNTER_SHARRED_PREF,counter); // counter ++
        Log.d(TAG,"Counter== " + counter);
        editor.apply();
    }

    private VolumeProviderCompat initVolumeProvicer(){

        //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
        return new VolumeProviderCompat(VolumeProviderCompat.VOLUME_CONTROL_RELATIVE, /*max volume*/100, /*initial volume level*/50) {
                    @Override
                    public void onAdjustVolume(int direction) {

                        AudioManager audioManager = (AudioManager) getApplicationContext().getSystemService(getApplicationContext().AUDIO_SERVICE);
                        audioManager.adjustStreamVolume(audioManager.STREAM_MUSIC,direction,0);
                        int counterInterval;
                        int counterLimit;
                        Log.d(TAG, "Direction : " + direction);
                        if(token!=null){

                            if(prefs.getString(Constants.DEVICE_MANUFACTURER_KEY,"").equals("LGE")){
                                counterInterval = 20000;
                                counterLimit = 6;
                            } else {
                                counterInterval = 10000;
                                counterLimit = 80;
                            }

                            //reset counter
                            if(getCounter() == 1){

                                handlerResetCounter.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        setCounter(0);
                                        Log.d(TAG,"inside handler");
                                    }
                                }, counterInterval);
                            }

                            setCounter(getCounter() + 1);

                            if(getCounter()  >= counterLimit && prefs.getBoolean(Constants.PROFILE_HARD_BUTTON, false)
                                    && GD.get().userHasFullAccess(prefs.getString(Constants.PROFILE_REG_CHANNEL,""),
                                    prefs.getInt(Constants.PROFILE_NUMBER_OF_MEMBERS,1), prefs.getInt(Constants.PROFILE_NUMBER_OF_FRIENDS,0))){
                                Log.d(TAG,"Should start alarm");

                                startAlarm(getApplicationContext());
                                setCounter(0);
                            }
                        }
                    }
                };

    }

    private void startAlarm(Context context){


        handlerResetCounter.removeCallbacksAndMessages(null);

        acceleratedPrevDate = acceleratedCurrDate;
        acceleratedCurrDate = new Date();
			Log.d(TAG, "acceleratedPrevDate=" + acceleratedPrevDate);
			Log.d(TAG, "acceleratedCurrDate=" + acceleratedCurrDate);

        if (acceleratedPrevDate != null) {

            long diff = acceleratedPrevDate.getTime() - acceleratedCurrDate.getTime();
            long diffSeconds = diff / 1000;
            if (diffSeconds < 0) {
                diffSeconds = -diffSeconds;
            }

            if (diffSeconds < NEXT_CALLBACK_DELAY) {
                Log.d(TAG,"too many alarms return");
                return;
            }
        }

        isOpen = prefs.getBoolean(Constants.APP_IS_OPEN, false);
        GD.get().alarmTypeDesc = Constants.ALARM_TYPE_SOS_EMERGENCY_DESC;
        GD.get().alarmType = Constants.ALARM_TYPE_SOS_EMERGENCY;

        if (isOpen) {
            //sv
            startLocalBroadcastReceiver(context);
            Log.d(TAG, " AccelometerService - isOpen == true");

            /** App is not running open the Main activity with the specific fragment starting the countdown **/
        } else {
            Boolean isPaused = prefs.getBoolean(Constants.APP_IS_PAUSED, false);
            if (isPaused) {
                Log.d(TAG, " AccelometerService - ispaused");
                Intent dialogIntent = new Intent(context, MainActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                editor.putBoolean(Constants.START_ALARM_FROM_BACKGROUND_SERVICE, true);
                editor.putBoolean(Constants.TRIGGERD_BY_BLUETOOTH_SHARED_PREF, false);
                editor.commit();
                context.startActivity(dialogIntent);
            } else {
                //sensorManager.unregisterListener(this);
                Log.d(TAG, " AccelometerService - isOpen == false");
                Intent dialogIntent = new Intent(context, MainActivity.class);
                dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                dialogIntent.putExtra(Constants.TRIGGERD_BY_BLUETOOTH_SHARED_PREF, false);
                dialogIntent.putExtra(Constants.OPEN_ACTIVITY_FROM_SERVICE, Constants.OPEN_ACTIVITY_FROM_SERVICE);
                context.startActivity(dialogIntent);
            }

        }
    }

    /**
     * VF: REGISTER LOCAL BROADCAST RECEIVER TO LISTEN FOR SCREEN ON - OFF,
     * USING INTENT FILTER ACTION_SCREEN_ON AND ACTION_SCREEN_OFF
     */
    private void registerScreenReceiver(){
        //Register Screen Receiver to detect if the screen of the phone is off or on
        final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mScreenReceiver, filter);
    }

    /***
     * VF: Local Broadcast Receiver to communicate with the Main Fragment in order to fire the alarm
     ***/
    private void startLocalBroadcastReceiver(Context context) {
        Intent intentBroacast = new Intent(Constants.BROADCAST_ACTION);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intentBroacast);
    }

    public void registerMediaSession(VolumeProviderCompat myVolumeProvider){

        Log.w(TAG,"registerMediaSession");
        mediaSession = new MediaSessionCompat(getApplicationContext(), "HardButtonPlayer");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0) //you simulate a player which plays something.
                .build());

        mediaSession.setPlaybackToRemote(myVolumeProvider);
        mediaSession.setActive(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSession.setCallback(new MediaSessionCompat.Callback(){
                @Override
                public void onCustomAction(String action, Bundle extras) {
                    super.onCustomAction(action, extras);

                    Log.d(TAG,"FFFFF");
                }
            });
        }

        setCounter(0);

    }

    public void unregisterMediaSession(){
        Log.w(TAG,"unregisterMediaSession");

        mediaSession.release();
    }
}
