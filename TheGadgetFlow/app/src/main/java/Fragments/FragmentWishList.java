package Fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.CloudieNetwork.GadgetFlow.R;
import com.example.vasilis.TheGadgetFlow.ActivityDetails;
import com.example.vasilis.TheGadgetFlow.LogInActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import Adapter.AdapterListItemWishList;
import Helper.Constants;
import Helper.FontCache;
import Helper.GD;
import Model.Model_Gadget.GadgetItem;
import Model.Model_Gadget.MainRss;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import RSS.RestClient.RestClientWishList;
import Utils.CommonUtils;
import View.DividerItemDecorationWishList;

/** Created by vasilis fouroulis on 21/12/2015. */
public class FragmentWishList extends Fragment {
    private RecyclerView mRecyclerView;
    private AdapterListItemWishList mAdapter;
    private GridLayoutManager mLayoutManager;
    private ImageButton anchor;
    private final static String TAG = FragmentWishList.class.getSimpleName();
    int loadedPage;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Context context;
    private String  username,cookie;
    private Typeface font;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private GD gd = GD.get();
    private static final long timeout = 300000;
    private okhttp3.OkHttpClient client;
    private ArrayList<GadgetItem> mList;
    private ProgressDialog waitingDialog;
    private boolean isLoading = false;
    private CountDownTimer mCountDown = new CountDownTimer(timeout, timeout) {

        @Override
        public void onTick(long millisUntilFinished) {
//            Log.d(TAG,"onTick:: " + millisUntilFinished );

        }

        @Override
        public void onFinish() {
            if (mLayoutManager.findLastVisibleItemPosition()> 10 ) {

                if(anchor!=null) {
                    anchor.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref = getActivity().getSharedPreferences("gadgetflow", 0);// 0 - for private mode
        editor = pref.edit();
        loadedPage = 1; // the first page of the rss feedF

        cookie = pref.getString(Constants.COOKIE, cookie);
        if(cookie != null){
            username = pref.getString(Constants.USER_NAME, username);
        }

        font = Typeface.createFromAsset(getActivity().getAssets(), "fonts/OpenSans-Regular.ttf");
        client = new okhttp3.OkHttpClient();
        context = getActivity();
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent , Bundle savedInstanceState) {


        if(cookie == null){

            View v = inflater.inflate(R.layout.fragment_wishlist, parent , false);
            Button log_in_button = (Button)v.findViewById(R.id.button_wish_list_button);
            TextView text_login_in = (TextView)v.findViewById(R.id.text_login_in);
            text_login_in.setTypeface(FontCache.get("OpenSans-Bold.ttf",getActivity()));
            TextView wish_list_string_02 = (TextView)v.findViewById(R.id.wish_list_string_02);
            wish_list_string_02.setTypeface(FontCache.get("OpenSans-Regular.ttf",getActivity()));
            log_in_button.setTypeface(FontCache.get("SanFrancisco-Bold.ttf",getActivity()));
            log_in_button.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, LogInActivity.class);
                    startActivity(intent);
                }
            });

            return v;

        } else {
            View v = inflater.inflate(R.layout.fragment_list_item_wishlist, parent , false);

            swipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.listItem_home_swipe_refresh_layout);
            mRecyclerView = (RecyclerView) v.findViewById(R.id.listItem_home);
            mLayoutManager = new GridLayoutManager(getActivity(),2);
            mRecyclerView.setLayoutManager(mLayoutManager);
            Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider_home_feed);
            mRecyclerView.addItemDecoration(new DividerItemDecorationWishList(20, 2,dividerDrawable));
            anchor = (ImageButton)v.findViewById(R.id.anchor_home); //anchor

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshContent();
                }
            });

            /** image button <<go to start >> click */
            anchor.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mRecyclerView.smoothScrollToPosition(0);
                    anchor.setVisibility(View.GONE);
                }
            });

            return v;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        menu.findItem(R.id.action_search).setVisible(false);
        if(cookie != null)
            menu.findItem(R.id.action_share).setVisible(true);
        else
            menu.findItem(R.id.action_share).setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_search:
                return false;
            case R.id.action_share:
                if(cookie == null){
                    return false;
                } else {
                    if(mList != null ) {
                        if(mList.size() > 0) {
                            shareWishList();
                        } else {
                            Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.alert_dialog_empty_wish_list),Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getActivity(),getActivity().getResources().getString(R.string.alert_dialog_empty_wish_list),Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }

            case android.R.id.home:

                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mCountDown != null) {
            mCountDown.cancel();
            mCountDown = null;
        }

        gd.setFragmentName(this.getClass().getSimpleName());

        hideDialog();
    }

    @Override
    public void onResume() {
        super.onResume();



        if(cookie!=null){

            if(CommonUtils.isNetworkAvailable(getActivity()) ){

                if(mList!=null) {
                    mList.clear();
                }
                showDialog(getActivity(), "Loading...", false);
                fetchWishList();
            } else{
                //No internet
                Toast.makeText(getActivity(),"No internet! Please check your network", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void getGadget(int page,String username) {

        //if there is internet
        if(CommonUtils.isNetworkAvailable(getActivity())) {
            isLoading = true;

            RestClientWishList restClientList = new RestClientWishList();
            Call<MainRss> call = restClientList.getGadgetApiService().getWishList(username,page);
            call.enqueue(new Callback<MainRss>() {
                @Override
                public void onResponse(Response<MainRss> response, Retrofit retrofit) {

                    if (response.isSuccess()) {
                        swipeRefreshLayout.setEnabled(true);
//                        Log.d(TAG,"********************************************************************");
//                        Log.d(TAG,"onResponse, success");
//                        Log.d(TAG,"********************************************************************");
                        MainRss mainRss = response.body();
                        ArrayList<GadgetItem> items = mainRss.getmChannel().getItems();
                        processResult(items);

                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    t.printStackTrace();
                    swipeRefreshLayout.setEnabled(true);
                    hideDialog();

                    if(CommonUtils.isNetworkAvailable(getActivity())) {
                        showPopUp();
                    }

                }
            });


        } else {
            //No internet
            Toast.makeText(getActivity(),"No internet! Please check your network", Toast.LENGTH_LONG).show();
            hideDialog();
        }
    }

    private void processResult(Object arg) {
        if(isAdded()){
            if(arg != null) {
                //items of the list - Gadget Items
//                Log.e(TAG,"******************************");
//                Log.e(TAG,"INSIDE PROCESS RESULT");
//                Log.e(TAG,"******************************");

                ArrayList<GadgetItem> items = (ArrayList<GadgetItem>) arg;

                if(mList == null)
                    mList  = new ArrayList<>();
                mList.addAll(items); // add all the items to the list of the fragment

                if(mAdapter == null) {
                    //setting the adapter
//                    Log.d(TAG,"mAdapter == null");
                    mAdapter = new AdapterListItemWishList(getActivity(),this,mList,waitingDialog );
                    mRecyclerView.setAdapter(mAdapter);

                    mAdapter.setOnItemClickListener(new AdapterListItemWishList.OnItemClickListener() {

                        @Override
                        public void onItemClick(View view, int position) {
                            onListViewItemClick(position);

                        }
                    });
                }
                mAdapter.notifyDataSetChanged();
            }
            hideDialog();

            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void onListViewItemClick(final int position) {
        try {
            GadgetItem data = mList.get(position);
            Bundle lbundle = new Bundle();
            lbundle.putSerializable("data", data);
            Intent fragmentDetail = new Intent(FragmentWishList.this.getActivity(), ActivityDetails.class);
            fragmentDetail.putExtra("data", lbundle);
            FragmentWishList.this.getActivity().startActivity(fragmentDetail);
            FragmentWishList.this.getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        } catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    public void shareWishList(){
        String shareData = "Hey,Check out my Wish List: \n http://thegadgetflow.com/user/".concat(username);

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareData);
        startActivity(Intent.createChooser(intent, "Share Via"));

    }

    private void refreshContent(){
        swipeRefreshLayout.setEnabled(false);
        showDialog(getActivity(),getActivity().getResources().getString(R.string.process_dialog_swipe_refresh),false);

//        Log.e(TAG,"******************************");
//        Log.e(TAG,"refreshContent");
//        Log.e(TAG,"******************************");

        if(mList!=null) {
            mList.clear();
        }

        getGadget(1,username);

        swipeRefreshLayout.setRefreshing(false);

    }

    public void showPopUp(){
        final Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.dialog_empty_wish_list);

        TextView dialogTitle = (TextView) dialog.findViewById(R.id.dialog_title);
        dialogTitle.setTypeface(FontCache.get("OpenSans-Bold.ttf",getActivity()));

        TextView body = (TextView) dialog.findViewById(R.id.dialog_tv_body_message);
        body.setTypeface(font);

        Button Ok = (Button) dialog.findViewById(R.id.chemail_ok_bt);

        Ok.setTypeface(font);
        Ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void fetchWishList(){
        /*********** Fetch wish list */
        if(CommonUtils.isNetworkAvailable(getActivity()) ){

            new requestWishList().execute(Constants.GET_WISH_LIST.concat(cookie));
        } else{
            //No internet
            Toast.makeText(getActivity(),"No internet! Please check your network", Toast.LENGTH_LONG).show();
            hideDialog();
        }
    }

    public void showDialog(Activity ctx, String message, boolean cancelable) {
        if (waitingDialog != null) {
            hideDialog();
            waitingDialog = null;
        }
        if(ctx != null) {/** vf : craslytics 224**/
            waitingDialog = new ProgressDialog(ctx);
            waitingDialog.setMessage(message);
            waitingDialog.setCancelable(cancelable);
            try {
                waitingDialog.show();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void hideDialog() {
        if (waitingDialog != null && waitingDialog.isShowing())
            waitingDialog.dismiss();
    }

    private class requestWishList extends AsyncTask<String, Void, JSONObject> {
        okhttp3.Response response;
        @Override
        protected JSONObject doInBackground(String... strings) {
            JSONObject jsonObject = null;
            try {
                okhttp3.Request request = new okhttp3.Request.Builder().url(strings[0]).build();
                response = client.newCall(request).execute();
                String jsonData = response.body().string();
                jsonObject= new JSONObject(jsonData);
            } catch (IOException | JSONException e) {
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
            if(response!=null && response.isSuccessful()){
                try {
                    if(result.getString("status").equals("error")){
                        hideDialog();
                    } else if(result.getString("status").equals("OK")) {
                        gd.items = result.getString("items");
                        getGadget(1,username);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideDialog();
                    if(getActivity() != null)Toast.makeText(getActivity(), Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
                } catch (Exception e){
                    e.printStackTrace();
                    hideDialog();
                    if(getActivity() != null)Toast.makeText(getActivity(), Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
                }
            } else {
                hideDialog();
                if(getActivity() != null)Toast.makeText(getActivity(), Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
            }
        }
    }
}
