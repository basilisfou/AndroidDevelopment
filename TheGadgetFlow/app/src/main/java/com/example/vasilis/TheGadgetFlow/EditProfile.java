package com.example.vasilis.TheGadgetFlow;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.CloudieNetwork.GadgetFlow.R;
import com.facebook.login.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;

import Helper.Constants;
import Helper.FontCache;
import Helper.GD;
import Helper.MemoryCache;
import Utils.CommonUtils;
import io.intercom.android.sdk.Intercom;

/**
 * Created by Vasilis Fouroulis on 30/12/2015
 */
public class EditProfile extends AppCompatActivity {
    private TextView fullname, dialogTitle;
    private String cookie,email,firstName,emailChange,passWordChange,facebookChange,twitterChange;
    private static okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
    private Dialog dialog;
    private ProgressDialog progressDialog;
    private Typeface font,font2;
    private SwitchCompat publicWishList;
    private Toolbar mToolbar;
    private Context c;
    GD gd = GD.get();
    private MemoryCache mc = MemoryCache.get();
    private CompoundButton.OnCheckedChangeListener mListener;


    private static final String TAG ="EditProfile";
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String nonce, username,newPassword;
    private boolean publicWishListBool;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_profile);
        sharedPreferences = this.getSharedPreferences("gadgetflow", 0);// 0 - for private mode
        editor = sharedPreferences.edit();
        cookie = sharedPreferences.getString(Constants.COOKIE, "");
        nonce = sharedPreferences.getString("nonce","");
        username = sharedPreferences.getString(Constants.USER_NAME,"");
        font = FontCache.get("fonts/OpenSans-Regular.ttf",this);
        font2 = FontCache.get("fonts/OpenSans-Bold.ttf",this);
        progressDialog = new ProgressDialog(this);
        c = this;

        /***Toolbar customization****/
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        (mToolbar.findViewById(R.id.toolbar_title)).setVisibility(View.VISIBLE);
        ((TextView)mToolbar.findViewById(R.id.toolbar_title)).setText(R.string.action_bar_edit_prof);
        ((TextView)mToolbar.findViewById(R.id.toolbar_title)).setTypeface(font);

        //hide logo
        ImageView logo = (ImageView) mToolbar.findViewById(R.id.logo);
        logo.setVisibility(View.GONE);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//disable toolbar title

        publicWishList = (SwitchCompat)findViewById(R.id.switch_settings);
        fullname = (TextView)findViewById(R.id.text_view_alterName);

        if (CommonUtils.isNetworkAvailable(this)) {
            new getUserPreference().execute(Constants.GET_USER_PREFERENCE.replace("@cookie@", cookie));
        } else {
            //No internet
            Toast.makeText(this,"No internet! Please check your network",Toast.LENGTH_LONG).show();
        }

        changeFont();
        /****************************************************************************************************************************************************************
         * Wish list public or private
         *************************************************************************************************************************************************************/
        mListener = new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                dialog = new Dialog(getApplicationContext());

                String setUserPreferences = Constants.SET_USER_PREFERENCE.replace("@cookie@", cookie).concat("&wishlist_state=");
                if (isChecked) {
                    setUserPreferences = setUserPreferences.concat("public");
                    publicWishListBool = isChecked;

                } else {
                    setUserPreferences = setUserPreferences.concat("private");
                    publicWishListBool = isChecked;
                }

                //
                if (CommonUtils.isNetworkAvailable(getApplicationContext())) {

                    new setUserPreferencePublicWishList().execute(setUserPreferences);
                } else {
                    //No internet
                    Toast.makeText(getApplicationContext(),"No internet! Please check your network",Toast.LENGTH_LONG).show();
                }
            }
        };


        /****************************************************************************************************************************************************************
         * Change the name
         ****************************************************************************************************************************************************************/
        (findViewById(R.id.ll_fill_name)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(c);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                final EditText name;
                final Button Done, Cancel;
                dialog.setContentView(R.layout.dialog_change_email);

                dialogTitle = (TextView) dialog.findViewById(R.id.dialog_email_title);
                dialogTitle.setText(getResources().getString(R.string.edit_profile_popup_change_name));
                dialogTitle.setTypeface(FontCache.get("fonts/OpenSans-Bold.ttf",getApplicationContext()));

                name = (EditText) dialog.findViewById(R.id.dialog_alert_email);
                name.setTypeface(font);
                Done = (Button) dialog.findViewById(R.id.chemail_ok_bt);
                Done.setTypeface(font);
                Cancel = (Button) dialog.findViewById(R.id.chemail_cancel_bt);
                Cancel.setTypeface(font);

                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                Done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        firstName = name.getText().toString(); // name
                        if (firstName.length() > 0) {
                            if (CommonUtils.isNetworkAvailable(getApplicationContext())) {
                                new setUserPreference().execute(Constants.SET_USER_PREFERENCE.replace("@cookie@", cookie).concat("&display_name=" + firstName));

                            } else {
                                //No internet
                                Toast.makeText(getApplicationContext(),"No internet! Please check your network",Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "please fill your name and password", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
                dialog.show();
            }
        });
        /****************************************************************************************************************************************************************
         * Changing the e-mail
         ****************************************************************************************************************************************************************/
        (findViewById(R.id.ll_change_email)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog = new Dialog(c);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_change_email);

                final EditText ed_email;
                final Button Done, Cancel;

                dialogTitle = (TextView) dialog.findViewById(R.id.dialog_email_title);
                dialogTitle.setText(getResources().getString(R.string.edit_profile_popup_change_mail));
                dialogTitle.setTypeface(font2);

                ed_email = (EditText) dialog.findViewById(R.id.dialog_alert_email);
                ed_email.setTypeface(font);
                ed_email.setText(sharedPreferences.getString(Constants.USERS_EMAIL,""));

                Cancel = (Button) dialog.findViewById(R.id.chemail_cancel_bt);
                Cancel.setTypeface(font);
                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                Done = (Button) dialog.findViewById(R.id.chemail_ok_bt);
                Done.setTypeface(font);
                Done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        emailChange = ed_email.getText().toString();

                        if (emailChange.length() > 0) {

                            if (CommonUtils.isNetworkAvailable(getApplicationContext())) {

                                new setUserPreference().execute(Constants.SET_USER_PREFERENCE.replace("@cookie@", cookie).concat("&email=" + emailChange));

                            } else {
                                //No internet
                                Toast.makeText(getApplicationContext(),"No internet! Please check your network",Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Give an E-mail", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
                dialog.show();
            }
        });
        /******************************************************************************************
         * Change the password
         ******************************************************************************************/
        findViewById(R.id.ll_change_pass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog = new Dialog(c);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_change_password);
                final EditText change_password, confirm_password;
                final Button Done, Cancel;

                dialogTitle = (TextView) dialog.findViewById(R.id.dialog_pass_title);
                dialogTitle.setTypeface(font2);

                change_password = (EditText) dialog.findViewById(R.id.dialog_alert_password);
                change_password.setTypeface(font);
                confirm_password = (EditText) dialog.findViewById(R.id.dialog_alert_confirm_password);
                change_password.setTypeface(font);
                Done = (Button) dialog.findViewById(R.id.chpass_ok_bt);
                Done.setTypeface(font);
                Cancel = (Button) dialog.findViewById(R.id.chepass_cancel_bt);
                Cancel.setTypeface(font);

                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                Done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        passWordChange = change_password.getText().toString();
                        String passWordChangeConfirm = confirm_password.getText().toString();

                        if (passWordChange.length() > 0 && passWordChangeConfirm.length() > 0) {
                            if (passWordChange.equals(passWordChangeConfirm)) {
                                dialog.dismiss();
                                newPassword = passWordChange;
                                if (CommonUtils.isNetworkAvailable(getApplicationContext())) {
                                    new changePassword().execute(Constants.SET_USER_PREFERENCE.replace("@cookie@", cookie).concat("&password=" + passWordChange));
                                } else {
                                    //No internet
                                    Toast.makeText(getApplicationContext(),"No internet! Please check your network",Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), "Password does not match", Toast.LENGTH_LONG);
                                toast.show();
                            }
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Please fill in both text box", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
                dialog.show();
            }
        });
        /******************************************************************************************
         * Change the facebook link
         ******************************************************************************************/
        findViewById(R.id.ll_facebook).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog = new Dialog(c);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_change_email);
                final EditText change_fb;
                final Button Done, Cancel;

                dialogTitle = (TextView) dialog.findViewById(R.id.dialog_email_title);
                dialogTitle.setText(getResources().getString(R.string.edit_profile_popup_fb_change));
                dialogTitle.setTypeface(font2);

                change_fb = (EditText) dialog.findViewById(R.id.dialog_alert_email);
                change_fb.setTypeface(font);
                change_fb.setText(sharedPreferences.getString(Constants.FACEBOOK_URL,""));
//                Log.d(TAG,sharedPreferences.getString(Constants.FACEBOOK_URL,""));
                Done = (Button) dialog.findViewById(R.id.chemail_ok_bt);
                Done.setTypeface(font);
                Cancel = (Button) dialog.findViewById(R.id.chemail_cancel_bt);
                Cancel.setTypeface(font);

                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                Done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        facebookChange = change_fb.getText().toString();

                        if (facebookChange.length() > 0) {

                            if (CommonUtils.isNetworkAvailable(getApplicationContext())) {
                                new setUserPreference().execute(Constants.SET_USER_PREFERENCE.replace("@cookie@", cookie).concat("&facebook=" + facebookChange));
                            } else {
                                //No internet
                                Toast.makeText(getApplicationContext(),"No internet! Please check your network",Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Please fill in both text box", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
                dialog.show();
            }
        });

        /**
         * Change the facebook link
         */
        findViewById(R.id.ll_twitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                dialog = new Dialog(c);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.setContentView(R.layout.dialog_change_email);
                final EditText change_tw;
                final Button Done, Cancel;

                dialogTitle = (TextView) dialog.findViewById(R.id.dialog_email_title);
                dialogTitle.setText(getResources().getString(R.string.edit_profile_popup_twitter));
                dialogTitle.setTypeface(font2);

                change_tw = (EditText) dialog.findViewById(R.id.dialog_alert_email);
                change_tw.setTypeface(font);
                change_tw.setText(sharedPreferences.getString(Constants.TWITTER_URL,""));
//                Log.d(TAG,sharedPreferences.getString(Constants.TWITTER_URL,""));
                Done = (Button) dialog.findViewById(R.id.chemail_ok_bt);
                Done.setTypeface(font);
                Cancel = (Button) dialog.findViewById(R.id.chemail_cancel_bt);
                Cancel.setTypeface(font);

                Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                Done.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        twitterChange = change_tw.getText().toString();

                        if (twitterChange.length() > 0) {

                            if (CommonUtils.isNetworkAvailable(getApplicationContext())) {
                                new setUserPreference().execute(Constants.SET_USER_PREFERENCE.replace("@cookie@", cookie).concat("&twitter=" + twitterChange));
                            } else {
                                //No internet
                                Toast.makeText(getApplicationContext(),"No internet! Please check your network",Toast.LENGTH_LONG).show();
                            }

                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Please fill in both text box", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    public boolean onOptionsItemSelected (MenuItem item) {

        int id = item.getItemId();

        //back button
        if(id == android.R.id.home){
            this.finish();
            this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    private static String Request(String url) throws IOException {

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }

    /***************************************************************************************************************************************
     *************************************************************** Get user Preference ***************************************************
     ***************************************************************************************************************************************/
    private class getUserPreference extends AsyncTask<String, Void, JSONObject> {

        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(c);
            pDialog.setMessage("Loading...");
            pDialog.show();
        }

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

                if (result.getString("status").equals("error")) {
                    if(result.getString("error").equalsIgnoreCase("Invalid cookie. Use the `generate_auth_cookie` method.")){
                        logOut();
                        pDialog.dismiss();
                    } else {
                        Toast.makeText(getApplicationContext(),result.getString("error"),Toast.LENGTH_LONG).show();
                        pDialog.dismiss();
                    }

                } else if (result.getString("status").equals("ok")) {

//                        Log.d(TAG,result.toString());
                    fullname.setText(result.getString("first_name"));
                    editor.putString(Constants.FULL_NAME,result.getString("first_name"));
                    email = result.getString("user_email");
                    editor.putString(Constants.USERS_EMAIL,result.getString("user_email"));
                    editor.putString(Constants.FACEBOOK_URL,result.getString("facebook"));
                    editor.putString(Constants.TWITTER_URL,result.getString("twitter"));
                    publicWishList.setOnCheckedChangeListener(null);
                    if (result.getString("wishlist_public").equals("1")) {
                        //public
                        publicWishList.setChecked(true);
                        editor.putBoolean(Constants.WISH_LIST_SETTINGS,true);
                    } else {
                        //private
                        publicWishList.setChecked(false);
                        editor.putBoolean(Constants.WISH_LIST_SETTINGS,false);
                    }
                    editor.apply();
                    publicWishList.setOnCheckedChangeListener(mListener);
                    fullname.setText(sharedPreferences.getString(Constants.FULL_NAME, ""));
                    publicWishList.setChecked(sharedPreferences.getBoolean(Constants.WISH_LIST_SETTINGS, false));

                    pDialog.dismiss();
                }

            } catch (JSONException e) {
                e.printStackTrace();
                pDialog.dismiss();
                if(c != null)Toast.makeText(c, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();

            }   catch (Exception e) {
                e.printStackTrace();
                pDialog.dismiss();
                if(c != null)Toast.makeText(c, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();

            }
        }
    }

    /****************************************************************************************************************************************
     ********************************************************************** Set user Preference**********************************************
     ****************************************************************************************************************************************/
    private class setUserPreference extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject jsonObject = null;
            try {
                String jsonData = Request(strings[0]);
                jsonObject = new JSONObject(jsonData);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                //Log.d("billy", result.toString());
                if (result.getString("status").equals("error")) {
                    progressDialog.dismiss();
                    if(result.getString("error").equalsIgnoreCase("Invalid cookie. Use the `generate_auth_cookie` method.")){
                        logOut();
                    } else {
                        Toast.makeText(getApplicationContext(),result.getString("error"),Toast.LENGTH_LONG).show();
                    }
                } else if (result.getString("status").equals("ok")) {
                    progressDialog.dismiss();
                    dialog.dismiss();
                    //get again the user settings
                    if (CommonUtils.isNetworkAvailable(getApplicationContext())) {
                        new getUserPreference().execute(Constants.GET_USER_PREFERENCE.replace("@cookie@", cookie));
                    } else {
                        //No internet
                        Toast.makeText(getApplicationContext(),"No internet! Please check your network",Toast.LENGTH_LONG).show();
                    }
                    Toast toast = Toast.makeText(getApplicationContext(), result.getString("0"), Toast.LENGTH_LONG);
                    toast.show();


                }
            } catch (JSONException e) {
                e.printStackTrace();
                dialog.dismiss();
                if(c != null)Toast.makeText(c, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();

            } catch (Exception e){
                e.printStackTrace();
                progressDialog.dismiss();
                dialog.dismiss();
                if(c != null)Toast.makeText(c, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
            }

        }
    }

    /****************************************************************************************************************************************
     ********************************************************************** Change Password**********************************************
     ****************************************************************************************************************************************/
    private class changePassword extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject jsonObject = null;
            try {
                String jsonData = Request(strings[0]);
                jsonObject = new JSONObject(jsonData);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                //Log.d("billy", result.toString());
                if (result.getString("status").equals("error")) {

                    if(result.getString("error").equalsIgnoreCase("Invalid cookie. Use the `generate_auth_cookie` method.")){
                        logOut();
                    } else {
                        Toast.makeText(getApplicationContext(),result.getString("error"),Toast.LENGTH_LONG).show();
                    }

                } else if (result.getString("status").equals("ok")) {

                    //get again the user settings
                    if (CommonUtils.isNetworkAvailable(getApplicationContext())) {
                        new logInRequest().execute(Constants.LOG_IN
                                .replace("@nonce@",nonce)
                                .replace("@username_string@",username)
                                .replace("@password_string@",newPassword));
                    } else {
                        Toast.makeText(getApplicationContext(),"No internet! Please check your network",Toast.LENGTH_LONG).show();
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
                if(c != null)Toast.makeText(c, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
            } catch (Exception e){
                e.printStackTrace();
                if(c != null)Toast.makeText(c, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
            }
        }
    }

    /**************************************************************************************************************************************
     ********************************************************** logIn Request *************************************************************
     **************************************************************************************************************************************/
    private class logInRequest extends AsyncTask<String, Void, JSONObject>{

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
            } catch (Exception e){
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPreExecute(){
            super.onPreExecute();

        }
        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                if(result.getString("status").equals("error")){
                    Toast toast = Toast.makeText(getApplicationContext(),result.getString("error") ,Toast.LENGTH_LONG);
                    toast.show();
                    progressDialog.dismiss();
                } else if(result.getString("status").equals("ok")) {
                    /** change cookie **/
                    cookie = result.getString("cookie");
                    editor.putString(Constants.COOKIE,cookie);
                    //Log.d(TAG,cookie);
                    editor.apply();
                    progressDialog.dismiss();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    private void changeFont() {
        TextView textView_fullname = (TextView) findViewById(R.id.textView_fullname);
        textView_fullname.setTypeface(font);
        TextView text_view_change_email = (TextView) findViewById(R.id.text_view_change_email);
        text_view_change_email.setTypeface(font);
        fullname.setTypeface(font);
        TextView text_view_change_password = (TextView) findViewById(R.id.text_view_change_password);
        text_view_change_password.setTypeface(font);

    }


    /****************************************************************************************************************************************
     ********************************************************************** Set user Preference WishList**********************************************
     ****************************************************************************************************************************************/
    private class setUserPreferencePublicWishList extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject jsonObject = null;
            try {
                String jsonData = Request(strings[0]);
                jsonObject = new JSONObject(jsonData);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jsonObject;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog.setMessage("Loading...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
                //Log.d("billy", result.toString());
                if (result.getString("status").equals("error")) {
                    progressDialog.dismiss();

                    if(result.getString("error").equalsIgnoreCase("Invalid cookie. Use the `generate_auth_cookie` method.")){
                        logOut();
                    } else {
                        Toast.makeText(getApplicationContext(),result.getString("error"),Toast.LENGTH_LONG).show();
                    }

                } else if (result.getString("status").equals("ok")) {
//                    Log.d(TAG,result.toString());

                    progressDialog.dismiss();
                    dialog.dismiss();

                    editor.putBoolean(Constants.WISH_LIST_SETTINGS,publicWishListBool);
                    editor.apply();

//                    Toast toast = Toast.makeText(getApplicationContext(), result.getString("0"), Toast.LENGTH_LONG);
//                    toast.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if(c != null)Toast.makeText(c, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
            } catch (Exception e){
                e.printStackTrace();
                progressDialog.dismiss();
                dialog.dismiss();
                if(c != null)Toast.makeText(c, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
            }

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        font = null;
        font2 = null;
        c = null;
    }

    public void logOut(){
        // sign out - delete token from the device
        editor.clear();
        editor.commit();
        //sing out - clear facebook session
        try {
            LoginManager.getInstance().logOut();
        }catch (Exception e){
            e.printStackTrace();
        }
        //Clear gd of wish list
        if(mc.wishList!=null) {
            mc.wishList.clear();
        }
        Intercom.client().reset(); //log out from Iterncom

        gd.items = "";
        Intent intent = new Intent(this, LogInActivity.class);
        startActivity(intent);
        this.finish();
    }

}