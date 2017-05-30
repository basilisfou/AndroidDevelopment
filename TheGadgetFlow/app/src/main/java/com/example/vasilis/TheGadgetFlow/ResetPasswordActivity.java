
package com.example.vasilis.TheGadgetFlow;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.CloudieNetwork.GadgetFlow.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import Helper.Constants;
import Helper.FontCache;
import Utils.CommonUtils;


public class ResetPasswordActivity extends AppCompatActivity {
    private Context context;
    private EditText email;
    private Button reset;
    private String email_string;
    private static okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
    private ProgressDialog progressDialog;
    private Typeface font, font2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        progressDialog = new ProgressDialog(this);
        context = this;

        font = FontCache.get("fonts/OpenSans-Regular.ttf",this);
        font2 = FontCache.get("fonts/SanFrancisco-Bold.ttf",this);

        /**
         * Toolbar customization
         */
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar); //replacing the old Action bar
        setSupportActionBar(mToolbar);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//disable toolbar title

        email = (EditText)findViewById(R.id.email_reset);
        reset = (Button)findViewById(R.id.button_reset);

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email_string = email.getText().toString();

                if(email_string.length() > 0){
                    if(CommonUtils.isNetworkAvailable(context) ){

                        new resetPassword().execute(Constants.RESET_PASSWORD.replace("@email@",email_string));
                    } else{
                        //No internet
                        Toast toast = Toast.makeText(context, "No internet! Please check your network", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }else{
                    Toast toast = Toast.makeText(context,"Please fill in your e-mail in the text box",Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });
        changeFont();
    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        int id = item.getItemId();
//
//        //back button
//        if(id == android.R.id.home){
//            this.finish();
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    private static String Request(String url) throws IOException {

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }
    private class resetPassword extends AsyncTask<String, Void, JSONObject> {

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

            progressDialog.setMessage("Resetting the Password...");
            progressDialog.show();
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
//                Log.d("billy",result.toString());
                if(result.getString("status").equals("error")){
                    Toast toast = Toast.makeText(context,result.getString("error") ,Toast.LENGTH_LONG);
                    toast.show();
                    progressDialog.dismiss();
                } else if(result.getString("status").equals("ok")) {
                    Toast toast = Toast.makeText(context, result.getString("msg") ,Toast.LENGTH_LONG);
                    toast.show();
                    progressDialog.dismiss();
                    Intent intent = new Intent(context, LogInActivity.class);
                    startActivity(intent);
                    finish();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            } catch(Exception e){
                e.printStackTrace();
                Toast toast = Toast.makeText(context, "No internet! Please check your network", Toast.LENGTH_LONG);
                toast.show();
                progressDialog.dismiss();

            }
        }
    }

    private void changeFont(){
        email.setTypeface(font);
        reset.setTypeface(font2);
        ((TextView)findViewById(R.id.should)).setTypeface(font);
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        context = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(this,LogIn2.class);
        startActivity(intent);
    }
}
