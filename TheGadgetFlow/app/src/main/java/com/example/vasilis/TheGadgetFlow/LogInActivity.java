package com.example.vasilis.TheGadgetFlow;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import Helper.Constants;
import Helper.FontCache;

import com.CloudieNetwork.GadgetFlow.R;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;

import com.facebook.login.LoginResult;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;

import Utils.CommonUtils;
import Utils.Installation;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.identity.Registration;

public class LogInActivity extends AppCompatActivity {
    private Button LogIn, SignUp;
    private SharedPreferences.Editor editor;
    private String cookie;
    private CallbackManager callbackManager;
    private okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
    private ProgressDialog progressDialog;
    private Activity activity;
    private Button loginButton;
    public static final String TAG = "LogInActivityTag";

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FacebookSdk.sdkInitialize(getApplication());

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_log_in);

        activity = this;

        progressDialog = new ProgressDialog(this);

        SharedPreferences sharedPreferences = this.getSharedPreferences("gadgetflow", 0);
        editor = sharedPreferences.edit();
        cookie = sharedPreferences.getString(Constants.COOKIE,null);

        /** already sign in */
        if(cookie != null){

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(Constants.LOG_IN_INTENT_TO_MAIN,true);
            startActivity(intent);
            CommonUtils.successfulLogin(sharedPreferences.getString(Constants.USERS_ID,""),this); //Registering users to intercom with device id
            Log.d(TAG,sharedPreferences.getString(Constants.USERS_ID,""));
            this.finish();
        }

        LogIn         = (Button)findViewById(R.id.logIn);
        SignUp        = (Button)findViewById(R.id.signUp);
        TextView skip = (TextView)findViewById(R.id.skip_for_now);
        loginButton   = (Button)findViewById(R.id.login_button_facebook);

        callbackManager = CallbackManager.Factory.create();        /**Facebook initialization*/
        final LoginManager loginManager = LoginManager.getInstance();

        changeFont();

        LogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity,LogIn2.class);
                startActivity(intent);
                finish();
            }
        });

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, RegisterActivity.class);
                startActivity(intent);
                finish();
            }
        });

        loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                new facebookRequest().execute(Constants.FACEBOOK_GET_TOKEN.replace("@token@", loginResult.getAccessToken().getToken()));
            }

            @Override
            public void onCancel() {
//                Log.d(TAG,"2) cancel Facebook");
            }

            @Override
            public void onError(FacebookException error) {
//                Log.e(TAG,"2) Error Facebook" + error.toString());
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Log.d(TAG,"1) FACEBOOK LOGIN BUTTON CLICK");
                if(CommonUtils.isNetworkAvailable(activity) ) {
                    loginManager.logInWithReadPermissions(activity, Arrays.asList("public_profile, email, user_birthday, user_friends"));
                } else {
                    Toast.makeText(activity,"No internet! Please check your network",Toast.LENGTH_LONG).show();
                }
            }
        });

        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                skipAction();
            }
        });
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    private String Request(String url) throws IOException {
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();

        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /** skip Action **/
    private void skipAction(){
        editor.apply();

        CommonUtils.successfulLogin(FirebaseInstanceId.getInstance().getToken(),this); //Registering users to intercom with device id

        Intent intent = new Intent(this,MainActivity.class);
        intent.putExtra(Constants.LOG_IN_INTENT_TO_MAIN,true);
        startActivity(intent);
        this.finish();
    }

    private void changeFont(){

        ((TextView)findViewById(R.id.app_description)).setTypeface(FontCache.get("fonts/OpenSans-Regular.ttf",this));
        ((TextView)findViewById(R.id.skip_for_now)).setTypeface(FontCache.get("fonts/OpenSans-Regular.ttf",this));
        LogIn.setTypeface(FontCache.get("fonts/SanFrancisco-Bold.ttf",this));
        SignUp.setTypeface(FontCache.get("fonts/SanFrancisco-Bold.ttf",this));
        loginButton.setTypeface(FontCache.get("fonts/SanFrancisco-Bold.ttf",this));
    }

    private class facebookRequest extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... Strings) {
            JSONObject jsonObject = null;
            try {
                String jsonData = Request(Strings[0]);
                jsonObject= new JSONObject(jsonData);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

            progressDialog.setMessage("Signing in...");
            progressDialog.show();
        }
        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                if(result.getString("status").equals("error")){

//                    Toast toast = Toast.makeText(activity,result.getString("error") ,Toast.LENGTH_LONG);
//                    toast.show();
//                    Log.d(TAG,result.getString("error"));
                    progressDialog.dismiss();

                } else if(result.getString("status").equals("ok")) {
//                    Log.d(TAG,"************************************************");
//                    Log.d(TAG,"3) onPostExecute," + "facebook");
//                    Log.d(TAG,result.toString());
                    cookie = result.getString("cookie");
                    CommonUtils.successfulLogin(result.getString("wp_user_id"),LogInActivity.this); //Login to Intercom

                    if(CommonUtils.isNetworkAvailable(getApplicationContext()) ){
                        if(cookie != null ) {
                            new getUserPreference().execute(Constants.GET_USER_PREFERENCE.replace("@cookie@", cookie));
                        }
                    } else {
                        //No internet
                        Toast.makeText(getApplicationContext(),"No internet! Please check your network",Toast.LENGTH_LONG).show();
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();

            }
        }
    }

    private class getUserPreference extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject jsonObject = null;
            try {
                String jsonData = Request(strings[0]);
                jsonObject = new JSONObject(jsonData);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if(result != null) {
                    if (result.getString("status").equals("error")) {
//                        Toast.makeText(getApplicationContext(),result.getString("error"),Toast.LENGTH_LONG).show();
//                        Log.d(TAG,result.getString("error"));
                    } else if (result.getString("status").equals("ok")) {
//                        Log.d(TAG,"************************************************");
//                        Log.d(TAG,"4) onPostExecute," + "getUserPreference");
//                        Log.d(TAG,result.toString());
//                        Log.d(TAG,"************************************************");
                        editor.putString(Constants.FULL_NAME,result.getString("first_name"));
                        editor.putString(Constants.USERS_EMAIL,result.getString("user_email"));
                        editor.putString(Constants.FACEBOOK_URL,result.getString("facebook"));
                        editor.putString(Constants.TWITTER_URL,result.getString("twitter"));
                        editor.putString(Constants.COOKIE, cookie);
                        editor.putString(Constants.USER_NAME,result.getString("user_login"));

                        if (result.getString("wishlist_public").equals("1")) {
                            //public
                            editor.putBoolean(Constants.WISH_LIST_SETTINGS,true);
                        } else {
                            //private
                            editor.putBoolean(Constants.WISH_LIST_SETTINGS,false);
                        }

                        Toast toast = Toast.makeText(activity, "Welcome to GadgetFlow", Toast.LENGTH_LONG);
                        toast.show();

                        editor.apply();

                        Intent intent = new Intent(activity, MainActivity.class);
                        intent.putExtra(Constants.LOG_IN_INTENT_TO_MAIN,true);
                        startActivity(intent);

                        LogInActivity.this.finish();

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
