package com.example.vasilis.TheGadgetFlow;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.CloudieNetwork.GadgetFlow.R;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import Helper.Constants;
import Helper.FontCache;
import Utils.CommonUtils;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.identity.Registration;

public class LogIn2 extends AppCompatActivity {
    private EditText username, password;
    private Button login;
    private TextView reset;
    private String username_string, password_string;
    private OkHttpClient client = new OkHttpClient();
    private okhttp3.OkHttpClient client2 = new okhttp3.OkHttpClient();
    private String nonce;
    private SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;
    private static final String TAG = "LogIn2";
    private String cookie,userName,niceName,usersMail,usersID,fullName;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in2);
        progressDialog = new ProgressDialog(this);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar); //replacing the old Action bar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//disable toolbar title

        sharedPreferences = getSharedPreferences("gadgetflow", 0);
        editor            = sharedPreferences.edit();

        username = (EditText)findViewById(R.id.username_log_in);
        password = (EditText)findViewById(R.id.password_log_in);
        login    = (Button)findViewById(R.id.login_button);
        reset    = (TextView) findViewById(R.id.reset_pass);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                username_string = username.getText().toString();
                password_string = password.getText().toString();

                if(username_string.length() != 0 && password_string.length() != 0){
                    getNonce();
                } else {Toast.makeText(LogIn2.this,"Please fill in an username and a password to proceed",Toast.LENGTH_LONG).show();}
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LogIn2.this, ResetPasswordActivity.class);
                startActivity(intent);
                finish();

            }
        });
        changeFont();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //back button
//        if(id == android.R.id.home){
//            this.finish();
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    public void onStop(){
        super.onStop();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,LogInActivity.class);
        startActivity(intent);
    }

    /** Get Nonce request **/
    public void getNonce(){
        if(CommonUtils.isNetworkAvailable(this) ){
            new getNonce().execute(); //get Nonce
        } else {
            Toast.makeText(this,"No internet! Please check your network",Toast.LENGTH_LONG).show();
        }
    }

    /** Get Login request **/
    public void httpLogin(String nonce){
        if(CommonUtils.isNetworkAvailable(this) ){
            if(nonce != null) {
                new request().execute(Constants.LOG_IN.replace("@nonce@", nonce).replace("@username_string@", username_string).replace("@password_string@", password_string));
            } else {
                Toast.makeText(this,"Something went wrong please try again",Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(this,"No internet! Please check your network",Toast.LENGTH_LONG).show();
        }

    }

    /** Get user Preferences **/
    public void httpUserPreference(String cookie){
        if(CommonUtils.isNetworkAvailable(this)){
            if(cookie != null ) {
                new getUserPreference().execute(Constants.GET_USER_PREFERENCE.replace("@cookie@", cookie));
            }
        } else {
            Toast.makeText(getApplicationContext(),"No internet! Please check your network",Toast.LENGTH_LONG).show();
        }
    }

    private void changeFont(){
        username.setTypeface(FontCache.get("fonts/OpenSans-Regular.ttf",this));
        password.setTypeface(FontCache.get("fonts/OpenSans-Regular.ttf",this));
        login.setTypeface(FontCache.get("fonts/SanFrancisco-Bold.ttf",this));
        reset.setTypeface(FontCache.get("fonts/OpenSans-Regular.ttf",this));
    }

    private String Request(String url) throws IOException {
        okhttp3.Request request2 = new okhttp3.Request.Builder().url(url).build();

        okhttp3.Response response = client2.newCall(request2).execute();
        return response.body().string();
    }
    private String nonceRequest(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /** Intercom Registration **/

    private void registerIntercom(String userID) {

        Registration registration = Registration.create().withUserId(userID);
        Intercom.client().registerIdentifiedUser(registration);
        Intercom.client().handlePushMessage();
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
                        Toast.makeText(getApplicationContext(),result.getString("error"),Toast.LENGTH_LONG).show();
                    } else if (result.getString("status").equals("ok")) {

                        Log.d(TAG,result.toString());

                        editor.putString(Constants.USER_NAME, userName);
                        editor.putString(Constants.COOKIE,cookie);
                        editor.putString(Constants.NICE_NAME,niceName);
                        editor.putString(Constants.USERS_EMAIL,usersMail);
                        editor.putString(Constants.USERS_ID,usersID);
                        editor.putString(Constants.FULL_NAME,fullName);
                        editor.putString(Constants.NONCE, nonce);
                        editor.putString(Constants.FACEBOOK_URL,result.getString("facebook"));
                        editor.putString(Constants.TWITTER_URL,result.getString("twitter"));

                        if (result.getString("wishlist_public").equals("1")) {
                            editor.putBoolean(Constants.WISH_LIST_SETTINGS,true);//public
                        } else {
                            editor.putBoolean(Constants.WISH_LIST_SETTINGS,false);//private
                        }
                        editor.apply();

                        Log.d(TAG,"userID: " + usersID);
                        registerIntercom(usersID);

                        progressDialog.dismiss();

                        Toast.makeText(LogIn2.this,"Hello " + userName ,Toast.LENGTH_LONG).show();

                        Intent intent = new Intent(LogIn2.this, MainActivity.class);
                        intent.putExtra(Constants.LOG_IN_INTENT_TO_MAIN,true);
                        startActivity(intent);

                        LogIn2.this.finish();

//                        String token = Installation.id(LogIn2.this);
//                        new registerDevice().execute(Constants.REGISTER_DEVICE.replace("@cookie@",cookie).replace("@devicetoken@",token));
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                progressDialog.dismiss();
            } catch (Exception e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),"Something went wrong please try again",Toast.LENGTH_LONG).show();
            }
        }
    }
    private class request extends AsyncTask<String, Void, JSONObject>{

        @Override
        protected JSONObject doInBackground(String... Strings) {
            JSONObject jsonObject = null;
            try {
                String jsonData = nonceRequest(Strings[0]);
                jsonObject= new JSONObject(jsonData);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if(result.getString("status").equals("error")){

                    Toast toast = Toast.makeText(getApplicationContext(),result.getString("error") ,Toast.LENGTH_LONG);
                    toast.show();

                    progressDialog.dismiss();

                } else if(result.getString("status").equals("ok")) {
                    Log.d(TAG,result.toString());
                    JSONObject user = result.getJSONObject("user");

                    cookie    = result.getString("cookie");
                    userName  = user.getString("username");
                    niceName  = user.getString("nicename");
                    usersMail = user.getString("email");
                    usersID   = user.getString("id");
                    fullName  = user.getString("firstname");

                    httpUserPreference(cookie);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch(NullPointerException e){
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
            }
        }
    }
    private class getNonce extends AsyncTask<Void, Void, JSONObject> {

        @Override
        protected void onPreExecute(){
            super.onPreExecute();
            progressDialog.setMessage("Signing in...");
            progressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(Void... Voids) {
            JSONObject jsonObject = null;
            try {
                String jsonData = nonceRequest(Constants.GET_NONCE);
                jsonObject= new JSONObject(jsonData);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }


        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                nonce = result.getString("nonce");
                httpLogin(nonce);//Log in and Proceed to the main activity
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){

            }
        }
    }
}
