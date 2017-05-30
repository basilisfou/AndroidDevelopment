package com.veriah.tennisquare;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.util.Log;
import com.bumptech.glide.Glide;
import org.json.JSONArray;

import org.json.JSONObject;
import javax.net.ssl.HttpsURLConnection;

import CommonUtils.Constants;
import CommonUtils.GD;
import CommonUtils.HttpConnectionWrapper;

/**
 * Created by vasilis fouroulis on 9/23/2016.
 */
public class MyTennisSquare extends MultiDexApplication {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Typeface font;
    private static final String TAG = "MyTennisSquare";

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        sharedPreferences = getSharedPreferences(Constants.SHARED_PREFERENCES_KEY, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();



        Log.d(TAG,"is token null:"+  sharedPreferences.getString(Constants.TOKEN,null));
        /** * vf: First Time the Logged-in user enters the app, gets the profile **/
        if(sharedPreferences.getString(Constants.TOKEN,null) != null){
            String body = "userId=" + sharedPreferences.getString(Constants.USER_ID,"") + "&userName=" + sharedPreferences.getString(Constants.USERNAME,"") + "&token=" + sharedPreferences.getString(Constants.TOKEN,"");

            getProfile(body,Constants.WEB_SERVICES_PROFILE_USER);
        }
    }

    public SharedPreferences getSharedPreferencesCustom() {
        return sharedPreferences;
    }

    public SharedPreferences.Editor getEditorCustom() {
        return editor;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * vf: First Time the Logged in user enter the app, gets the profile
     * @param body
     * @param WebService
     */
    protected void getProfile(String body, String WebService){

        if(GD.get().isConnected(this)) {
            new HttpGetProfile().execute(WebService, body);
        }
    }


    protected class HttpGetProfile extends AsyncTask<String,Integer,Object> {

        int responseCode = 0;

        @Override
        protected Object doInBackground(String... params) {
            Log.d(TAG,"DOINBACKGROUND "+ Constants.SERVER_URL + params[0]+ " " + params[1]);
            HttpConnectionWrapper wrapper = new HttpConnectionWrapper(Constants.SERVER_URL + params[0],"POST",params[1], 15000);
            String response = wrapper.getResponse();
            responseCode = wrapper.getResponseCode();

            return response;
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);

            if(responseCode == HttpsURLConnection.HTTP_OK){
                try {
                    JSONObject json = new JSONObject(result.toString());
                    boolean res = json.getBoolean("res");
                    Log.d(TAG,result.toString());
                    if(res) {

                        JSONArray jsonArray = json.getJSONArray("response");

                        for (int i = 0 ; i < jsonArray.length();i ++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            editor.putString(Constants.ROLE_USER,object.getString("role"));
                            editor.putBoolean(Constants.BACKHAND,object.getBoolean("backhanded"));
                            editor.putBoolean(Constants.PLAYS,object.getBoolean("plays"));
                            editor.putString(Constants.BIRTH_PLACE,object.getString("birthplace"));
                            editor.putString(Constants.BIRTH_DATE,object.getString("birthDate"));
                            editor.putString(Constants.USER_LANG,object.getString("userLang"));
                            editor.putString(Constants.NATIONALITY,object.getString("nationality"));
                            editor.putString(Constants.AGE,object.getString(Constants.AGE));
                            editor.putString(Constants.HAIR_COLOR,object.getString(Constants.HAIR_COLOR));
                            editor.putString(Constants.HEIGHT,object.getString(Constants.HEIGHT));
                            editor.putString(Constants.WEIGHT,object.getString(Constants.WEIGHT));
                            editor.putString(Constants.MOBILE_2,object.getString(Constants.MOBILE_2));
                            editor.putString(Constants.MOBILE,object.getString(Constants.MOBILE));
                            editor.putString(Constants.COUNTRY,object.getString(Constants.COUNTRY));
                            editor.putString(Constants.CITY,object.getString(Constants.CITY));
                            editor.putString(Constants.ADDRESS,object.getString(Constants.ADDRESS));
                            editor.putString(Constants.LANGUAGE,object.getString(Constants.LANGUAGE));
                            editor.putString(Constants.GENDER,object.getString(Constants.GENDER));
                            editor.putString(Constants.NAME,object.getString(Constants.NAME));
                            editor.putString(Constants.SURNAME,object.getString(Constants.SURNAME));
                            editor.putString(Constants.RANKING,object.getString(Constants.RANKING));

                            if(object.has("profileURL")) {
                                Log.d(TAG,"profile url found" + object.has("profileURL"));
                               editor.putString(Constants.USER_PROFILE, object.getString(Constants.USER_PROFILE));
                            }else{
                                Log.d(TAG,"profile url not found" + object.has("profileURL"));
                               editor.putString(Constants.USER_PROFILE,null);
                            }

                            editor.apply();
                            Log.d(TAG,"OBJECT IMAGE"+ object.toString());
                            Log.d(TAG, "user profile"+ object.getString("profileURL"));
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

            Log.d(TAG,"****************************************************************************************************************************************************************************************");
        }
    }

}
