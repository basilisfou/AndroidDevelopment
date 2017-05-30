package Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.CloudieNetwork.GadgetFlow.R;
import com.example.vasilis.TheGadgetFlow.EditProfile;
import com.example.vasilis.TheGadgetFlow.LogInActivity;
import com.facebook.login.LoginManager;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import Helper.Constants;
import Helper.GD;
import Helper.MemoryCache;
import Utils.CommonUtils;
import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.UnreadConversationCountListener;

/**
 * Created by Vasilis Fouroulis on 21/12/2015.
 */
public class FragmentSettings extends Fragment {

    private String cookie;
    private SharedPreferences.Editor editor;
    private Activity context;
    private final static String TAG = "FragmentSettings";
    private static okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
    ProgressDialog progressDialog;
    private Typeface font;
    private ImageView logIn_sign_out;
    private GD gd = GD.get();
    private MemoryCache mc = MemoryCache.get();
    private SharedPreferences pref;
    private RelativeLayout rlEditProfile;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;

    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        Activity activity = null;
        if(c instanceof Activity){
            activity = (Activity) c;
        }
        context = activity;


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent , Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, parent , false);
        TextView LogSetting = (TextView)view.findViewById(R.id.textView_setting03);

        rlEditProfile  = (RelativeLayout) view.findViewById(R.id.ll_ed_pr);
        logIn_sign_out = (ImageView)view.findViewById(R.id.iv_wishlist03);
        final TextView intercomTV = (TextView)view.findViewById(R.id.textView_intercon);

        pref = getActivity().getSharedPreferences("gadgetflow", 0);// 0 - for private mode
        editor = pref.edit();
        cookie = pref.getString(Constants.COOKIE, cookie);

        font = Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Regular.ttf");
        progressDialog = new ProgressDialog(context);

        changeFont(view);

        Intercom.client().getUnreadConversationCount();
        Intercom.client().addUnreadConversationCountListener(new UnreadConversationCountListener() {
            @Override
            public void onCountUpdate(int i) {
                if(i > 0 ){
                    if(isAdded()) intercomTV.setText(getString(R.string.settings_intercon) + " ( " + String.valueOf(i) + " )");
                } else {
                    if(isAdded()) intercomTV.setText(getString(R.string.settings_intercon));
                }
            }
        });

        if(cookie != null){
//            rlEditProfile.setVisibility(View.GONE);
            if (CommonUtils.isNetworkAvailable(getActivity())) {
                new getUserPreference().execute(Constants.GET_USER_PREFERENCE.replace("@cookie@", cookie));
            } else {
                //No internet
                Toast.makeText(getActivity(),"No internet! Please check your network",Toast.LENGTH_LONG).show();
            }
            //signed in
            LogSetting.setText(context.getResources().getString(R.string.settings_Log_out));
            logIn_sign_out.setImageResource(R.drawable.log_out);

        } else {
            //signed out

            LogSetting.setText(context.getResources().getString(R.string.settings_Log_in));
            (view.findViewById(R.id.ll_ed_pr)).setVisibility(View.GONE);
            logIn_sign_out.setImageResource(R.drawable.wishlistlogin);
        }
        //edit profile
        rlEditProfile.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (cookie != null) {
                    Intent intent = new Intent(getActivity(),EditProfile.class);
                    startActivity(intent);

                } else {
                    Intent intent = new Intent(context, LogInActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        
        //sign in  - sign out
        view.findViewById(R.id.ll_log_in).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (cookie != null) {
                    logOut();
                } else {
                    Intent intent = new Intent(context, LogInActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                }
            }
        });

        //help and support
        view.findViewById(R.id.ll_intercon).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intercom.client().displayMessenger();
            }
        });

        //sending e-mail - contact us
        view.findViewById(R.id.ll_contact_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"hello@thegadgetflow.com"});
                try {
                    startActivity(Intent.createChooser(i, "Send mail..."));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(getActivity(), "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        /**----------------------------------------------
         * Subscribe On youtube
         *----------------------------------------------*/
        view.findViewById(R.id.ll_subscribe_on_youtube).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("https://www.youtube.com/user/thegadgetflow");
            }
        });

        /**----------------------------------------------
         * Subscribe On news letter
         *----------------------------------------------*/
        view.findViewById(R.id.ll_subscribe).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("http://thegadgetflow.com/newsletter/");
            }
        });
        /**----------------------------------------------
         * Knowledge Base
         *----------------------------------------------*/
        view.findViewById(R.id.ll_knowledge_base).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("http://faq.thegadgetflow.com");
            }
        });
        /**----------------------------------------------
         * Press and media
         *----------------------------------------------*/
        view.findViewById(R.id.ll_press).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("http://thegadgetflow.com/press");
            }
        });
        /**----------------------------------------------
         * Hiring
         *----------------------------------------------*/
        view.findViewById(R.id.ll_hiring).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("http://thegadgetflow.com/apply");
            }
        });

        /**----------------------------------------------
         * Labs
         *----------------------------------------------*/
        view.findViewById(R.id.ll_labs).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("http://thegadgetflow.com/lab-experiments/");
            }
        });

        /**---------------------------------------------
         * About us
         *----------------------------------------------*/
        (view.findViewById(R.id.ll_about_us)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("http://thegadgetflow.com/about-us/");
            }
        });
        /**---------------------------------------------
         * Blog
         *----------------------------------------------*/
        (view.findViewById(R.id.ll_Blog)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("http://thegadgetflow.com/blog/");
            }
        });
        /**---------------------------------------------
         * Term of Use
         **----------------------------------------------*/
        (view.findViewById(R.id.ll_Terms)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("http://thegadgetflow.com/terms-of-use/");
            }
        });
        /**---------------------------------------------
         * Privacy Policy
         *----------------------------------------------*/
        (view.findViewById(R.id.ll_privacy)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("http://thegadgetflow.com/privacy-policy/");
            }
        });
        /**---------------------------------------------
         * Status
         *----------------------------------------------*/
        (view.findViewById(R.id.ll_status)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("http://status.thegadgetflow.com/");
            }
        });

        /**---------------------------------------------
         * official shop
         *----------------------------------------------*/
        (view.findViewById(R.id.ll_shop)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openUrlOnExternalBrowser("http://shop.thegadgetflow.com/");
            }
        });

        return view;
    }

    private void changeFont(View view){
        ((TextView)view.findViewById(R.id.textView_setting02)).setTypeface(font);
        ((TextView)view.findViewById(R.id.textView_setting03)).setTypeface(font);
        ((TextView)view.findViewById(R.id.textView_setting04)).setTypeface(font);
        ((TextView)view.findViewById(R.id.textView_subscribe_on_youtube)).setTypeface(font);
        ((TextView)view.findViewById(R.id.textView_knowledge_base)).setTypeface(font);
        ((TextView)view.findViewById(R.id.textView_about_us)).setTypeface(font);
        ((TextView)view.findViewById(R.id.textView_Blog)).setTypeface(font);
        ((TextView)view.findViewById(R.id.textView_Terms)).setTypeface(font);
        ((TextView)view.findViewById(R.id.textView_privacy)).setTypeface(font);
        ((TextView)view.findViewById(R.id.textView_status)).setTypeface(font);
        ((TextView)view.findViewById(R.id.textView_shop)).setTypeface(font);
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    public void openUrlOnExternalBrowser(String url){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gd.setFragmentName(this.getClass().getSimpleName());
        context = null;
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
        gd.items ="";
        //Clear gd of wish list
        if(mc.wishList!=null) {
            mc.wishList.clear();
            mc.wishList = null;
        }

        mc.latest = null;
        mc.trendings = null;

        Intercom.client().reset(); //log out from Iterncom

        try {
            Toast toast = Toast.makeText(context, "Goodbye :(", Toast.LENGTH_LONG);
            toast.show();
        } catch (Exception e){
            /** vf: Crashlytics 231 context was null replacing with get activity**/
            e.printStackTrace();
        }
        if(context != null) {
            Intent intent = new Intent(context, LogInActivity.class);
            startActivity(intent);
            context.finish();
        } else {

            try {
                Intent intent = new Intent(getActivity(), LogInActivity.class);
                startActivity(intent);
                getActivity().finish();
            } catch (NullPointerException e){
                /** vf: Crashlytics 242 context was null replacing with get activity**/
                e.printStackTrace();
            }
        }
    }

    private static String Request(String url) throws IOException {

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
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
                        if(result.getString("error").equalsIgnoreCase("Invalid cookie. Use the `generate_auth_cookie` method.")){
                            logOut();
                        } else {
                            Toast.makeText(context ,result.getString("error"),Toast.LENGTH_LONG).show();
                        }
                    } else if (result.getString("status").equals("ok")) {
                        editor.putString(Constants.FULL_NAME,result.getString("first_name"));
                        editor.putString(Constants.USERS_EMAIL,result.getString("user_email"));
                        editor.putString(Constants.FACEBOOK_URL,result.getString("facebook"));
                        editor.putString(Constants.TWITTER_URL,result.getString("twitter"));

                        if (result.getString("wishlist_public").equals("1")) {
                            //public
                            editor.putBoolean(Constants.WISH_LIST_SETTINGS,true);
                        } else {
                            //private
                            editor.putBoolean(Constants.WISH_LIST_SETTINGS,false);
                        }
                        editor.apply();
                        progressDialog.dismiss();

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if(getActivity() != null)Toast.makeText(getActivity(), Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
            }
        }
    }
}
