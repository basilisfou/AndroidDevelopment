package Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.CloudieNetwork.GadgetFlow.R;
import com.example.vasilis.TheGadgetFlow.ActivityDetails;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import Adapter.AdapterListItem;
import Helper.Constants;
import Helper.GD;
import Model.Model_Gadget.GadgetItem;
import Model.Model_Gadget.MainRss;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import RSS.RestClient.SearchRestApi;
import Utils.CommonUtils;
import View.DividerItemDecorationHomeFeed;
import View.DividerItemDecorationWishList;

public class FragmentSearch extends Fragment {
    private SwipeRefreshLayout    swipeRefreshLayout;
    private RecyclerView          mRecyclerView;
    private ArrayList<GadgetItem> mList;
    private AdapterListItem       mAdapter;
    private GridLayoutManager     mLayoutManager;
    private String searchText;
    private ImageButton anchor;
    private RelativeLayout rl_nothing_found;
    private final static String TAG = "FragmentSearchTag";
    private GD gd = GD.get();
    private String cookie;
    private SharedPreferences sharedPreferences;
    private okhttp3.OkHttpClient client;
    public boolean isRefreshing = false;
    private static OnScrolledListener mCallback;
    private ProgressDialog waitingDialog;
    private boolean isLoading = false;
    private CountDownTimer mCountDown = new CountDownTimer(30000, 30000) {

        @Override
        public void onTick(long millisUntilFinished) {

        }
        @Override
        public void onFinish() {
            if (mLayoutManager.findLastVisibleItemPosition()> 10 )
                anchor.setVisibility(View.VISIBLE);
        }
    };
    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int pos = intent.getIntExtra("position",0);
            boolean isSaved = intent.getBooleanExtra("isSaved",false);
//            Log.d(TAG,"LOCAL BROADCAST RECEIVER:    pos" + pos  + ", isSaved: " + isSaved);
            GadgetItem item = mAdapter.getItem(pos);
            item.isSaved = isSaved;
            mAdapter.setItem(pos, item);
        }
    };

    /** API 23 >= Marshmallow Attach Listeners from the MainActivity.class **/
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity mainActivity;
        if(context instanceof Activity){
            mainActivity = (Activity)context;
            try {
                mCallback = (OnScrolledListener) mainActivity;

            } catch (ClassCastException e) {
                throw new ClassCastException(mainActivity.toString()
                        + " ");
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnScrolledListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " ");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = new okhttp3.OkHttpClient();
        sharedPreferences = getActivity().getSharedPreferences("gadgetflow", 0);// 0 - for private mode
        cookie = sharedPreferences.getString(Constants.COOKIE, null);
        savedInstanceState = getArguments();
        if (savedInstanceState != null) {
            searchText = savedInstanceState.getString("key");

        }
        //for the search button in the action bar
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent , Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_search, parent , false);
        waitingDialog = new ProgressDialog(getActivity());
       /** RecyclerView customization**/
        mRecyclerView = (RecyclerView) v.findViewById(R.id.listItem);
        swipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.listItem_home_swipe_refresh_layout);
        anchor =(ImageButton)v.findViewById(R.id.anchor); //anchor
        rl_nothing_found = (RelativeLayout)v.findViewById(R.id.rl_nothing_found);

        mRecyclerView.setHasFixedSize(true);
        if(getActivity().getResources().getBoolean(R.bool.isTabletX ) || getActivity().getResources().getBoolean(R.bool.isTabletl)) {
            mLayoutManager = new GridLayoutManager(getActivity(),2);
        }else{
            mLayoutManager = new GridLayoutManager(getActivity(),1);
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
        if(getActivity().getResources().getBoolean(R.bool.isTabletX ) || getActivity().getResources().getBoolean(R.bool.isTabletl)) {
            Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider_home_feed);
            mRecyclerView.addItemDecoration(new DividerItemDecorationWishList(20, 2,dividerDrawable));
        }else{
            Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider_home_feed);
            mRecyclerView.addItemDecoration(new DividerItemDecorationHomeFeed(dividerDrawable));

        }
        mRecyclerView.getItemAnimator().setMoveDuration(5000);


        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                int newPage = page + 1;
                getGadget(newPage);
                isRefreshing = false;
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        anchor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //go to start
                mRecyclerView.smoothScrollToPosition(0);
                anchor.setVisibility(View.GONE);
            }
        });

        showDialog(getActivity(), "Loading...", false);

        mList = new ArrayList<>();

        if(cookie != null){
//            Log.d(TAG,"fetching Wish List");
            fetchWishList();
        } else {
            getGadget(1);
        }
        if(cookie !=null) {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, new IntentFilter(Constants.LOCAL_BROADCAST_RECEIVER));
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //the button search is setting to visible
        menu.findItem(R.id.action_search).setVisible(true);
        menu.findItem(R.id.action_share).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_search:
//                rightHeaderButtonClick();
                rl_nothing_found.setVisibility(View.GONE);
                return true;
            case R.id.action_share:
                return false;
            case android.R.id.home:

                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCountDown.start();
    }

    @Override
    public void onResume(){
        super.onResume();
        if(mCountDown != null) {
            mCountDown.cancel();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCountDown.cancel();
        mCountDown = null;
        mList = null;
        gd.setFragmentName(this.getClass().getSimpleName());
        if(cookie != null){
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        }

        hideDialog();
    }

    /** creating the list with the rss model*/
    public void getGadget(int loadedPage) {

        //if there is internet
        if(Utils.CommonUtils.isNetworkAvailable(getActivity())) {

            SearchRestApi restClient= new SearchRestApi();
            Call<MainRss> call = restClient.getGadgetApiService().getSearchedGadgets(searchText,loadedPage);
            call.enqueue(new Callback<MainRss>() {
                @Override
                public void onResponse(Response<MainRss> response, Retrofit retrofit) {
                    if (response.isSuccess()) {

                        MainRss mainRss = response.body();
                        ArrayList<GadgetItem> items = mainRss.getmChannel().getItems();
                        processResult(items);
                        swipeRefreshLayout.setEnabled(true);
                    }
                }
                @Override
                public void onFailure (Throwable t) {
                    swipeRefreshLayout.setEnabled(true);
                    t.getMessage();
                    //todo change display message
                    if (mList!=null && mList.size() == 0)displayNothingFound();

                    if(isAdded()){
                        hideDialog();
                    }
                }
            });
        } else {
            //No internet
            Toast.makeText(getActivity(),"No internet! Please check your network", Toast.LENGTH_LONG).show();
            hideDialog();
        }
    }
    /** Proccess Result - helper method for creating the list */
    private void processResult(Object arg) {
        if(isAdded()){
            if(arg != null) {
                //items of the list - Gadget Items
                ArrayList<GadgetItem> items = (ArrayList<GadgetItem>) arg;
                if(mList == null)
                    mList = new ArrayList<>();
                mList.addAll(items); // add all the items to the list of the fragment

                if(mAdapter == null) {
                    //setting the adapter
                    mAdapter = new AdapterListItem(getActivity(),this,mList,waitingDialog);
                    mRecyclerView.setAdapter(mAdapter);
                    mAdapter.setOnItemClickListener(new AdapterListItem.OnItemClickListener() {

                        @Override
                        public void onItemClick(View view, int position) {
                            onListViewItemClick(position);
                        }
                    });
                }
                mAdapter.notifyDataSetChanged();

            }
            isLoading = false;
            hideDialog();
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /** when an Item of the list is clicked */
    public void onListViewItemClick(final int position) {
        try {
            GadgetItem data = mList.get(position);
            Bundle lbundle = new Bundle();
            lbundle.putSerializable("data", data);
            lbundle.putInt("position",position);
            Intent fragmentDetail = new Intent(getActivity(), ActivityDetails.class);
            fragmentDetail.putExtra("data", lbundle);
            getActivity().startActivity(fragmentDetail);
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }catch (ArrayIndexOutOfBoundsException e){
            e.toString();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void displayNothingFound(){
        rl_nothing_found.setVisibility(View.VISIBLE);
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

    private void refreshContent(){
        showDialog(getActivity(),getActivity().getResources().getString(R.string.process_dialog_swipe_refresh),false);
        swipeRefreshLayout.setEnabled(false);

        if(mList !=null) {
            mList.clear();
        }

        isRefreshing = true;

        if(cookie != null){
            fetchWishList();
        } else {
            getGadget(1);
        }

//        if(gd.printLogs)Log.d(TAG,"*****************");
//        if(gd.printLogs)Log.d(TAG,"INSIDE ON REFRESH");
//        if(gd.printLogs)Log.d(TAG,"*****************");

        swipeRefreshLayout.setRefreshing(false);
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
        protected void onPostExecute(JSONObject result) {
            if (response!=null && response.isSuccessful()) {
                try {
//                if(gd.printLogs)Log.d("billy",result.toString());
                    if (result.getString("status").equals("error")) {
                        hideDialog();
                    } else if (result.getString("status").equals("OK")) {
                        gd.items = result.getString("items");
                        getGadget(1);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    hideDialog();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    hideDialog();
                    if (getActivity() != null)
                        Toast.makeText(getActivity(), Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
                }
            } else {
                hideDialog();
                if (getActivity() != null)
                    Toast.makeText(getActivity(), Constants.ERROR_MESSAGE, Toast.LENGTH_LONG).show();
            }
        }
    }

    public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
        // The minimum amount of items to have below your current scroll position
        // before loading more.
        private int visibleThreshold = 5;
        // The current offset index of data you have loaded
        private int currentPage = 0;
        // The total number of items in the dataset after the last load
        private int previousTotalItemCount = 0;
        // True if we are still waiting for the last set of data to load.
        private boolean loading = true;
        // Sets the starting page index
        private int startingPageIndex = 0;

        RecyclerView.LayoutManager mLayoutManager;

        public EndlessRecyclerViewScrollListener(LinearLayoutManager layoutManager) {
            this.mLayoutManager = layoutManager;
        }

        public EndlessRecyclerViewScrollListener(GridLayoutManager layoutManager) {
            this.mLayoutManager = layoutManager;
            visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
        }

        public EndlessRecyclerViewScrollListener(StaggeredGridLayoutManager layoutManager) {
            this.mLayoutManager = layoutManager;
            visibleThreshold = visibleThreshold * layoutManager.getSpanCount();
        }

        public int getLastVisibleItem(int[] lastVisibleItemPositions) {
            int maxSize = 0;
            for (int i = 0; i < lastVisibleItemPositions.length; i++) {
                if (i == 0) {
                    maxSize = lastVisibleItemPositions[i];
                }
                else if (lastVisibleItemPositions[i] > maxSize) {
                    maxSize = lastVisibleItemPositions[i];
                }
            }
            return maxSize;
        }

        // This happens many times a second during a scroll, so be wary of the code you place here.
        // We are given a few useful parameters to help us work out if we need to load some more data,
        // but first we check if we are waiting for the previous load to finish.
        @Override
        public void onScrolled(RecyclerView view, int dx, int dy) {
//            Log.e(TAG,"" + dy);

            if(mCallback != null && dy != 0)
                mCallback.onScrolledHome();

            int lastVisibleItemPosition = 0;
            int totalItemCount = mLayoutManager.getItemCount();

            if (mLayoutManager instanceof StaggeredGridLayoutManager) {
                int[] lastVisibleItemPositions = ((StaggeredGridLayoutManager) mLayoutManager).findLastVisibleItemPositions(null);
                // get maximum element within the list
                lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions);
            } else if (mLayoutManager instanceof LinearLayoutManager) {
                lastVisibleItemPosition = ((LinearLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            } else if (mLayoutManager instanceof GridLayoutManager) {
                lastVisibleItemPosition = ((GridLayoutManager) mLayoutManager).findLastVisibleItemPosition();
            }

            // If the total item count is zero and the previous isn't, assume the
            // list is invalidated and should be reset back to initial state
            if (totalItemCount < previousTotalItemCount) {
                this.currentPage = this.startingPageIndex;
                this.previousTotalItemCount = totalItemCount;
                if (totalItemCount == 0) {
                    this.loading = true;
                }
            }
            // If it’s still loading, we check to see if the dataset count has
            // changed, if so we conclude it has finished loading and update the current page
            // number and total item count.
            if (loading && (totalItemCount > previousTotalItemCount)) {
                loading = false;
                previousTotalItemCount = totalItemCount;
            }

            // If it isn’t currently loading, we check to see if we have breached
            // the visibleThreshold and need to reload more data.
            // If we do need to reload some more data, we execute onLoadMore to fetch the data.
            // threshold should reflect how many total columns there are too
            if (!loading && (lastVisibleItemPosition + visibleThreshold) > totalItemCount) {
                currentPage++;
                onLoadMore(currentPage, totalItemCount);
                loading = true;
            }
        }

        // Defines the process for actually loading more data based on page
        public abstract void onLoadMore(int page, int totalItemsCount);

    }
    // Container Activity must implement this interface
    public interface OnScrolledListener {
        void onScrolledHome();
    }
}
