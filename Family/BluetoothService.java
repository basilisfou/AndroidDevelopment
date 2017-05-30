package services;

import android.app.ActivityManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.alarm.veriah.FamilyApp.MainActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Helper.BleCharacteristics;
import Helper.BleServices;
import Helper.Constants;
import Helper.GD;

/***
 * Created by vasilis fouroulis on 3/28/2016.
 */
/****************************************************** Bluetooth Service ************************************
 * 1) This Service is activated from the Activity of Bluetooth settings  and deactivate when the alarm goes  *
 *    off ( Becomes red )or the user loges out                                                               *                                                                                         *
 * 2) The purpose of this service is to listen to the clicks of the bluetooth devices, these device must be  *
 *    paired first, also they must be BLE devices ( Bluetooth low energy ). By default we use the UUID       *
 *    of characteristic 0000ffe1-0000-1000-8000-00805f9b34fb, iTag                                           *                                                 *
 * 3) Contains the callbacks of the bluetooth gatt (Bluetooth connection)                                    *
 * 4) When triggered can open the MainFragment in all the states of the application                          *
 *      @FOREGROUND - call local broadcast receiver of the main activity                                     *
 *      @BACKGROUND - startActivity(MainActivity)                                                            *
 *      @CLOSED     - startActivity(MainActivity)                                                            *
 * 5) this service can run on the background despite the Application is closed, in onDestroy the bluetooth   *
 *    callbacks is being unregistered                                                                        *
 *************************************************************************************************************/
public class BluetoothService extends Service {
    private Intent intentBroadcast;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private String BluetoothAdressRegisterd;
    private Intent dialogIntent;
    public static final String TAG = "BluetoothService";
    private static final long SCAN_PERIOD = 36000000;//10 hours
    private BluetoothGatt mGatt;
    private SharedPreferences sharedPref;
    private Date clickedButtonPrevDate;
    private Date clickedButtonCurrDate;
    private boolean isServicesRunning;
    private ScanCallback mScanCallback;
    private SharedPreferences.Editor editor;
    private boolean isPaused;
    GD gd = GD.get();
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isServicesRunning = true;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Log.w(TAG, "BLUETOOTH SERVICE onDestroy");

        //stop scanning
        isServicesRunning = false;
        scanLeDevice(false);

        //disconnect from the server in order not to receive callbacks after the service stops
        if(mGatt != null)
            mGatt.disconnect();
    }

    @Override
    public int onStartCommand(Intent intent,int flag, int startId) {
        if(Build.VERSION.SDK_INT  <  21){
            /** BEFORE LOLLIPOP **/
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        }else {
            final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = bluetoothManager.getAdapter();
            mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            settings = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build();
            filters = new ArrayList<>();
            initScanCallback();
        }

        mHandler = new Handler();
        isServicesRunning = true;

        sharedPref = getApplicationContext().getSharedPreferences(Constants.SHARRED_KEY_PREFERENCES_KEY, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
        dialogIntent = new Intent(this, MainActivity.class);
        Log.d(TAG,"Must start bluetooth scan");
        scanLeDevice(true);

        return START_STICKY;

    }

    /**
     * Method that starts the scanning
     * @param enable
     */
    private void scanLeDevice(final boolean enable) {
        if  (enable) {
            /** STOP SCANNING AFTER SCAN PERIOD **/
            mHandler.postDelayed(new Runnable() {
                @Override
                public  void    run() {
                    if  (Build.VERSION.SDK_INT  <   21) {
                        if (mBluetoothAdapter.isEnabled())
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    }
                    else {
                        if (mBluetoothAdapter.isEnabled())
                            mLEScanner.stopScan(mScanCallback);
                        }
                }
            },  SCAN_PERIOD);
            /** START SCANNING IMMEDIATELY **/
            if  (Build.VERSION.SDK_INT  <   21) {
                if(mBluetoothAdapter.isEnabled())
                    mBluetoothAdapter.startLeScan(mLeScanCallback);
                Log.d(TAG,"Scanning for devices before lollipop");
            } else {
                Log.d(TAG, "Scanning for devices >= lollipop");
                if(mBluetoothAdapter.isEnabled())
                    mLEScanner.startScan(filters, settings, mScanCallback);

            }
        }
        else {
            if  (Build.VERSION.SDK_INT  <   21) {
                if(mBluetoothAdapter.isEnabled())
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
            }
            else {
                if(mBluetoothAdapter.isEnabled())
                    mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    public void initScanCallback(){

        mScanCallback = new ScanCallback() {

            @Override
            public void onScanResult(int callbackType,ScanResult result) {
                BluetoothDevice btDevice = result.getDevice();
                Log.d(TAG, "SCANNING");
                BluetoothAdressRegisterd = sharedPref.getString(Constants.BLUETOOTH_DEVICE_ADDRESS, null);
                if(btDevice.getAddress().equals(BluetoothAdressRegisterd)){
                    if  (mGatt  ==  null)   {
                        Log.d(TAG, "********************************************************");
                        Log.d(TAG, "CONNECTING");
                        Log.d(TAG, "********************************************************");
                        mGatt   =   btDevice.connectGatt(getApplicationContext(),    false,  mainGattCallback);
                    }
                    scanLeDevice(false);
                }
            }

            @Override
            public void  onScanFailed(int errorCode) {
                Log.d(TAG,"ERROR SCANNING, ERRORCODE: " + errorCode);
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                Log.d(TAG,"onBatchScanResults");
                for (ScanResult sr  :   results) {

                }

            }
        };
    }
    /**
     * Scan call back for devices < Lollipop
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final  BluetoothDevice device, int rssi, byte[]  scanRecord) {

            Runnable MyRunnable = new Runnable() {
                @Override
                public void run() {
                    BluetoothAdressRegisterd = sharedPref.getString(Constants.BLUETOOTH_DEVICE_ADDRESS, null);
                    Log.e(TAG,"SHARED PREF: " + BluetoothAdressRegisterd);
                    Log.d(TAG,"SCAN DEVICE: " + device.getAddress());
                    if(device.getAddress().equals(BluetoothAdressRegisterd)){
                        if  (mGatt  ==  null)   {
                            Log.d(TAG, "********************************************************");
                            Log.d(TAG, "CONNECTING");
                            Log.d(TAG, "********************************************************");
                            mGatt   =   device.connectGatt(getApplicationContext(),    false,  mainGattCallback);
                        }
                        scanLeDevice(false);
                    }
                }
            };
            Thread t = new Thread(MyRunnable);
            t.start();
        }
    };

    private final BluetoothGattCallback mainGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt   gatt,   int status, int newState) {
            switch  (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "BluetoothGattCallback: CONNECTED");
                    gatt.discoverServices();
                    BluetoothAdressRegisterd = sharedPref.getString(Constants.BLUETOOTH_DEVICE_ADDRESS, "");
                    if (gatt.getDevice().getAddress().equals(BluetoothAdressRegisterd)) {
                        editor.putBoolean(Constants.BLUETOOTH_A_DEVICE_IS_CONNECTED,true);
                        editor.apply();
                    }

                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e(TAG, "BluetoothGattCallback: DISCONNECTED");

                    if(isServicesRunning){
                        mGatt  =  null;
                            scanLeDevice(true);
                    }else {
                            scanLeDevice(false);
                    }
                    editor.putBoolean(Constants.BLUETOOTH_A_DEVICE_IS_CONNECTED,false);
                    editor.apply();

                    break;
                default:
                    Log.e(TAG,   "BluetoothGattCallback: STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt  gatt,   int status) {
            List<BluetoothGattService>  services    =   gatt.getServices();
            List<BluetoothGattCharacteristic> Characteristics;
            BleServices bleServices = new BleServices();
            BleCharacteristics bleCharacteristics = new BleCharacteristics();
            for (int i = 0 ; i < services.size() ; i++){
                BluetoothGattService bluetoothGattService = services.get(i);
                Log.d(TAG, "*************************************************************************");
                Log.d(TAG,"SERVICE UUID : " + bluetoothGattService.getUuid());
                Log.d(TAG,"SERVICE NAME : " + bleServices.getServiceNameFromUUID(bluetoothGattService.getUuid().toString()));
                Characteristics = bluetoothGattService.getCharacteristics();

                for(int j = 0 ; j < Characteristics.size() ; j++){
                    BluetoothGattCharacteristic characteristic = Characteristics.get(j);
                    Log.d(TAG,"     Characteristic UUID       : " + characteristic.getUuid());
                    Log.d(TAG,"     Characteristic NAME       : " + bleCharacteristics.getCharacteristicsFromUUID(characteristic.getUuid().toString()));
                    Log.d(TAG,"     Characteristic Properties : " + characteristic.getProperties());
                    Log.d(TAG,"     Characteristic WriteType  : " + characteristic.getWriteType());
                    Log.d(TAG,"     Value                    : " + characteristic.getValue());

                    if(characteristic.getUuid().toString().equals("0000ffe1-0000-1000-8000-00805f9b34fb")) {
                        gatt.setCharacteristicNotification(characteristic, true);
                    }

                    List<BluetoothGattDescriptor> Descriptors = characteristic.getDescriptors();
                    for(int w = 0; w < Descriptors.size();w++){
                        BluetoothGattDescriptor Descriptor = Descriptors.get(w);
                        Log.d(TAG,"         Descriptor UUID       : " + Descriptor.getUuid());
                        Log.d(TAG,"         Value                     : " + Descriptor.getValue());
                        Log.d(TAG, "        ****************************************************************");
                    }

                }
                Log.d(TAG, "*************************************************************************");
            }
        }

        @Override
        public  void onCharacteristicRead(BluetoothGatt  gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.i(TAG, "onCharacteristicRead: " + characteristic.toString());
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            BluetoothAdressRegisterd = sharedPref.getString(Constants.BLUETOOTH_DEVICE_ADDRESS, null);
            if (gatt.getDevice().getAddress().equals(BluetoothAdressRegisterd)) {
                Log.d(TAG, "*************************************************************************");
                Log.i(TAG, "onCharacteristicChanged: " + characteristic.toString() + ", UUID: " + characteristic.getUuid() + ", Value: " + characteristic.getValue());
                Log.d(TAG, "*************************************************************************");

                clickedButtonPrevDate = clickedButtonCurrDate;
                clickedButtonCurrDate = new Date();
                Log.d(TAG, "clickedButtonPrevDate=" + clickedButtonPrevDate);
                Log.d(TAG, "clickedButtonCurrDate=" + clickedButtonCurrDate);
                if (clickedButtonPrevDate == null) {
                } else {
                    long diff = clickedButtonPrevDate.getTime() - clickedButtonCurrDate.getTime();
                    long diffSeconds = diff / 1000;
                    if (diffSeconds < 0) {
                        diffSeconds = -diffSeconds;
                    }
                    if (diffSeconds < 3) {
                        return;
                    }
                }

                if (characteristic.getUuid().toString().equals("0000ffe1-0000-1000-8000-00805f9b34fb") && gd.userHasFullAccess(sharedPref.getString(Constants.PROFILE_REG_CHANNEL,""), sharedPref.getInt(Constants.PROFILE_NUMBER_OF_MEMBERS,1),
                        sharedPref.getInt(Constants.PROFILE_NUMBER_OF_FRIENDS,0))) {
                    soundTheAlarm();
                }
            } else{
                gatt.disconnect();
            }
        }
    };

    /**
     * Method that defines if the App is running or not
     **/
    public String isForeground() {
        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE); //taking the activity
        List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
        return tasks.get(0).processName;
    }

    /** Call the Local Broadcast Manager - bluetooth when the app is running **/
    private void callLocalBroadcastReceiver() {
        Log.d(TAG, "BLUETOOTH SERVICE + callLocalBroadcastReceiver");
        intentBroadcast = new Intent(Constants.BROADCAST_ACTION_BLUETOOTH);
//        intentBroadcast.putExtra(Constants.LAST_HEALTH_CHECK_BOOL,true);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentBroadcast);
    }

    private void soundTheAlarm(){
        boolean disableTimer = sharedPref.getBoolean(Constants.ON_STATE_COUNTDOWN_BOOL, false);
        if (!disableTimer) {
            gd.alarmTypeDesc = Constants.ALARM_TYPE_SOS_EMERGENCY_DESC;
            gd.alarmType = Constants.ALARM_TYPE_BLUETOOTH;
            /** Check if the APP is on background , foreground and closed **/
            Boolean isOpened = sharedPref.getBoolean(Constants.APP_IS_OPEN, false);
            if (isOpened) {
                Log.d(TAG, "app running");
                callLocalBroadcastReceiver();

            } else {

                isPaused = sharedPref.getBoolean(Constants.APP_IS_PAUSED, false);

                if (isPaused) {
                    Log.d(TAG, "app is paused");
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    editor.putBoolean(Constants.START_ALARM_FROM_BACKGROUND_SERVICE, true);
                    editor.putBoolean(Constants.TRIGGERD_BY_BLUETOOTH_SHARED_PREF, true);
                    editor.commit();
                    startActivity(dialogIntent);

                } else {

                    Log.d(TAG, "app is not running");
                    dialogIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    dialogIntent.putExtra(Constants.INTENT_TRIGGERD_BY_BLUETOOTH, true);
                    dialogIntent.putExtra(Constants.OPEN_ACTIVITY_FROM_SERVICE, Constants.OPEN_ACTIVITY_FROM_SERVICE);
                    startActivity(dialogIntent);
                }
            }
        }
    }

}
