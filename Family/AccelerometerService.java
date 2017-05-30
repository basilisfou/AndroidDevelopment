package services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.alarm.veriah.FamilyApp.MainActivity;

import java.util.Date;

import Helper.Constants;
import Helper.GD;
import Helper.WindowQueue;

/****************************************************** Accelerometer Service ********************************
 * 1) This Service is activated from the Fragment of Settings and deactivate when the alarm goes off or the  *
 *    user loges out                                                                                         *
 * 2) The purpose of this service is to measure the acceleration forces and rotational forces along three    *
 *    axes                                                                                                   *
 * 3) Contains the callbacks of the sensor manager Type of accelerometer                                     *
 * 4) When triggered can open the MainFragment with all the states of the application                        *
 *      @FOREGROUND - call local broadcast receiver of the main activity                                     *
 *      @BACKGROUND - startActivity(MainActivity)                                                            *
 *      @CLOSED     - startActivity(MainActivity)                                                            *
 * 5) this service can run on the background despite the Application is closed                               *
 *************************************************************************************************************/
public class AccelerometerService extends Service implements SensorEventListener {

	private SharedPreferences sharedPref;
	private Intent intentBroacast;
	private SharedPreferences.Editor editor;
	private boolean isOpen,isPaused;
	public static final String TAG = "AccelerometerService";
	private SensorManager sensorManager;
	private Sensor senAccelerometer;
	private float mAccel;
	GD gd = GD.get();
	private Date acceleratedPrevDate;
	private Date acceleratedCurrDate;
	public static final int NEXT_CALLBACK_DELAY = 4;
	private WindowQueue window;
	private AudioManager mAudioManager;


	@Override
	public void onCreate() {
		super.onCreate();
		/** shared preferences **/
		Log.d("AccelerometerService","AccelerometerService onCreate");
		sharedPref = getApplicationContext().getSharedPreferences(Constants.SHARRED_KEY_PREFERENCES_KEY, Context.MODE_PRIVATE);
		editor = sharedPref.edit();
		isOpen = sharedPref.getBoolean(Constants.APP_IS_OPEN, false);
		isPaused = sharedPref.getBoolean(Constants.APP_IS_PAUSED,false);
		sensorManager=(SensorManager)getSystemService(Context.SENSOR_SERVICE);
		senAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		window = new WindowQueue();
	}


    @Override
	public int onStartCommand(Intent intent,int flag, int startId) {
		editor = sharedPref.edit();

//		startService(new Intent(getApplicationContext(),HardButtonService.class));

		try{
			Log.d("AccelerometerService", "AccelerometerService onStartCommand");
			sensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		} catch (Exception e){
			Log.d("billy",e.toString());
		}

		return START_STICKY;

    }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	/** Call the Local Broadcast Manager when the app is running**/
	private void startLocalBroadcastReceiver() {
		intentBroacast = new Intent(Constants.BROADCAST_ACTION);
		LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroacast);
	}

	@Override

	public final void onAccuracyChanged(Sensor sensor,int accuracy){

	}
	@Override
	public final void onSensorChanged(SensorEvent event){

		if(event.sensor.getType()==Sensor.TYPE_ACCELEROMETER) {

			// assign directions
			float x = event.values[0];
			float y = event.values[1];
			float z = event.values[2];
			//Log.d(TAG,"x: " + x + " y: " + y + " z= " + z);
			mAccel = (float) Math.sqrt((double) (x * x + y * y + z * z));
			Log.d(TAG,"mAccel " + mAccel);
			window.enqueue(mAccel);

			SharedPreferences sharedPreferences = getSharedPreferences(Constants.SHARRED_KEY_PREFERENCES_MOBILE, Context.MODE_PRIVATE);

			Log.d(TAG,"window.getZeroGDurationRange(): " + window.getZeroGDurationRange());
			Log.d(TAG,"sharedPreferences.getInt(Constants.PROFILE_SEEK_BAR,0: " + sharedPreferences.getInt(Constants.PROFILE_SEEK_BAR,0));
			int progress = 200 - 2 * sharedPreferences.getInt(Constants.PROFILE_SEEK_BAR,0);
			if (window.getZeroGDurationRange() > progress && gd.userHasFullAccess(sharedPref.getString(Constants.PROFILE_REG_CHANNEL,""), sharedPref.getInt(Constants.PROFILE_NUMBER_OF_MEMBERS,1),
					sharedPref.getInt(Constants.PROFILE_NUMBER_OF_FRIENDS,0))) {
				Log.d(TAG,"***********MAN DOWN*****************");
				soundTheAlarm();
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.e("Accelometerservice", "onDestroy -- disableAccelerometer");
		sensorManager.unregisterListener(this);
	}


	public void soundTheAlarm(){
		boolean disableTimer = sharedPref.getBoolean(Constants.ON_STATE_COUNTDOWN_BOOL, false);

		if (!disableTimer) {

			acceleratedPrevDate = acceleratedCurrDate;
			acceleratedCurrDate = new Date();
			Log.d(TAG, "acceleratedPrevDate=" + acceleratedPrevDate);
			Log.d(TAG, "acceleratedCurrDate=" + acceleratedCurrDate);
			if (acceleratedPrevDate == null) {

			} else {

				long diff = acceleratedPrevDate.getTime() - acceleratedCurrDate.getTime();
				long diffSeconds = diff / 1000;
				if (diffSeconds < 0) {
					diffSeconds = -diffSeconds;
				}
				if (diffSeconds < NEXT_CALLBACK_DELAY) {
					return;
				}
			}

			isOpen = sharedPref.getBoolean(Constants.APP_IS_OPEN, false);
			gd.alarmTypeDesc = Constants.ALARM_TYPE_MANDOWN_DESC;
			gd.alarmType = Constants.ALARM_TYPE_MANDOWN;

			if (isOpen) {
				//sv
				startLocalBroadcastReceiver();
				Log.d(TAG, " AccelometerService - isOpen == true");

				/** App is not running open the Main activity with the specific fragment starting the countdown **/
			} else {
				isPaused = sharedPref.getBoolean(Constants.APP_IS_PAUSED, false);
				if (isPaused) {
					Log.d(TAG, " AccelometerService - ispaused");
					Intent dialogIntent = new Intent(this, MainActivity.class);
					dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					editor.putBoolean(Constants.START_ALARM_FROM_BACKGROUND_SERVICE, true);
					editor.putBoolean(Constants.TRIGGERD_BY_BLUETOOTH_SHARED_PREF, false);
					editor.commit();
					startActivity(dialogIntent);
				} else {
					//sensorManager.unregisterListener(this);
					Log.d(TAG, " AccelometerService - isOpen == false");
					Intent dialogIntent = new Intent(this, MainActivity.class);
					dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					dialogIntent.putExtra(Constants.TRIGGERD_BY_BLUETOOTH_SHARED_PREF, false);
					dialogIntent.putExtra(Constants.OPEN_ACTIVITY_FROM_SERVICE, Constants.OPEN_ACTIVITY_FROM_SERVICE);
					startActivity(dialogIntent);
				}
			}
		}
	}


}
