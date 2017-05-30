package com.example.vasilis.TheGadgetFlow;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.CloudieNetwork.GadgetFlow.R;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import Helper.Constants;
import Helper.FontCache;
import Utils.CommonUtils;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.identity.Registration;

public class RegisterActivity extends AppCompatActivity {

    private EditText username;
    private EditText email;
    private EditText password;
    private String username_string;
    private String email_string;
    private String password_string;
    private String nonce;
    private String userId;
    private String cookie;
    private static okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
    private SharedPreferences.Editor editor;
    private ProgressDialog progressDialog;
    private Button singUp;
    private static final String TAG = "RegisterActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        progressDialog = new ProgressDialog(this);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar); //replacing the old Action bar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//disable toolbar title

        SharedPreferences pref = getSharedPreferences("gadgetflow", 0);// 0 - for private mode
        editor = pref.edit();

        username = (EditText)findViewById(R.id.username_log_up);
        email    = (EditText)findViewById(R.id.email_sign_up);
        password = (EditText)findViewById(R.id.password_sign_up);
        singUp   = (Button)findViewById(R.id.sign_up_bt);

        changeFont();

        singUp.setOnClickListener( new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                username_string = username.getText().toString();
                email_string    = email.getText().toString();
                password_string = password.getText().toString();

                if(username_string.length() != 0 && email_string.length() != 0 && password_string.length() != 0 ){

                    if(CommonUtils.isNetworkAvailable(getApplicationContext())){
                        new getNonce().execute(); //get Nonce
                    } else {
                        Toast.makeText(getApplicationContext(), "No internet! Please check your network", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(),"Please fill in all the fields",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,LogInActivity.class);
        startActivity(intent);
    }

    /** Get nonce**/
    static String nonceRequest(String url) throws IOException  {
        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private void changeFont() {
        username.setTypeface(FontCache.get("fonts/OpenSans-Regular.ttf",this));
        email.setTypeface(FontCache.get("fonts/OpenSans-Regular.ttf",this));
        password.setTypeface(FontCache.get("fonts/OpenSans-Regular.ttf",this));
        singUp.setTypeface(FontCache.get("fonts/SanFrancisco-Bold.ttf",this));
    }

    /** Intercom Registration **/
    private void successfulLogin(String userID) {

        Registration registration = Registration.create().withUserId(userID);
        Intercom.client().registerIdentifiedUser(registration);
        Intercom.client().handlePushMessage();
    }

    private class getNonce extends AsyncTask<Void, Void, JSONObject>{

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
        protected void onPreExecute(){
            super.onPreExecute();

            progressDialog.setMessage("Loading ...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {

                nonce = result.getString("nonce");

                if(CommonUtils.isNetworkAvailable(getApplicationContext()) && nonce != null){
                    new HttpRegister().execute(Constants.REGISTER
                            .replace("@username_string@",username_string)
                            .replace("@email_string@",email_string)
                            .replace("@nonce@",nonce)
                            .replace("@display_name@",username_string)
                            .replace("@password_string@",password_string));
                } else {
                    Toast.makeText(getApplicationContext(), "No internet! Please check your network", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
                progressDialog.dismiss();
            }
        }
    }
    private class HttpRegister extends AsyncTask<String, Void, JSONObject>{

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
                    Toast.makeText(RegisterActivity.this,result.getString("error") ,Toast.LENGTH_LONG).show();
//                    Log.d(TAG,result.toString());
                    progressDialog.dismiss();
                } else if(result.getString("status").equals("ok")) {

                    userId = result.getString("user_id");
                    cookie = result.getString("cookie");

                    editor.putString(Constants.COOKIE, cookie);
                    editor.putString(Constants.USERS_ID, userId);
                    editor.putString(Constants.USER_NAME, username_string);
                    editor.putBoolean(Constants.WISH_LIST_SETTINGS,false);
                    editor.putString(Constants.USERS_EMAIL,email_string);
                    editor.apply();

                    successfulLogin(userId);

                    progressDialog.dismiss();

                    Toast.makeText(RegisterActivity.this,"Welcome to GadgetFlow" ,Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                    intent.putExtra(Constants.LOG_IN_INTENT_TO_MAIN,true);
                    startActivity(intent);

                    RegisterActivity.this.finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e){
                e.printStackTrace();
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(),"An Error Occurred Please Try Again " ,Toast.LENGTH_LONG).show();
            }
        }
    }
}
