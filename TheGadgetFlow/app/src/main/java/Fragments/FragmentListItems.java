package Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;


import com.example.vasilis.TheGadgetFlow.ActivityDetails;

import com.CloudieNetwork.GadgetFlow.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import Adapter.AdapterListItem;
import Adapter.CustomSpinnerAdapter;
import Helper.Constants;
import Helper.GD;
import Model.Model_Gadget.GadgetItem;
import Model.Model_Gadget.MainRss;
import Model.CategoryItem;
import Model.SpinnerItem;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import RSS.RestClient.RestClientList;
import RSS.RestClient.RestClientSortingCategory;
import Utils.CommonUtils;
import View.DividerItemDecorationHomeFeed;
import View.DividerItemDecorationWishList;

/**
 * Created by vasilis fouroulis on 30/6/2015.
 */
public class FragmentListItems extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ArrayList<GadgetItem> mList;
    private AdapterListItem mAdapter;
    private GridLayoutManager mLayoutManager;
    private ImageButton anchor;
    private String categoryId,sortingValue;
    private final static String TAG = "FragmentListItemsTag";
    private RelativeLayout dropDownMenu;
    private Fragment fragment;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private GD gd = GD.get();
    private Spinner mSpinner;
    private CustomSpinnerAdapter customSpinnerAdapter;
    private ArrayList<SpinnerItem> CustomListViewValuesArr = new ArrayList<>();
    private okhttp3.OkHttpClient client;
    private String cookie;
    public boolean isRefreshing = false;
    private CategoryItem item;
    private Activity activity;
    private ProgressDialog waitingDialog;
    private boolean isOpenFromCategory = false;

    @Override
    public void onAttach(Activity act) {
        super.onAttach(act);
        activity = act;
    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        Activity act = null;
        if(c instanceof Activity){
            act = (Activity) c;
        }
        activity = act;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstanceState = getArguments();
        client = new okhttp3.OkHttpClient();
        fragment = getActivity().getFragmentManager().findFragmentById(R.id.frame_container);
        //getting the category item
        if (savedInstanceState != null) {

            item = savedInstanceState.getParcelable("Item");
            isOpenFromCategory = savedInstanceState.getBoolean("openFromCategory",false);
//            Log.d(TAG,"isOpenFromCategory" + isOpenFromCategory);
            if(item != null ) {
                categoryId = item.getId();
                sortingValue = item.getSortingValuel();

            }
        }

//        Log.d(TAG,"INSIDE ON CREATE");
        sharedPreferences = getActivity().getSharedPreferences("gadgetflow", 0);// 0 - for private mode
        editor = sharedPreferences.edit();
        setHasOptionsMenu(true);
        cookie = sharedPreferences.getString(Constants.COOKIE, null);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent , Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list_item, parent , false);
        Log.d(TAG,"INSIDE ON onCreateView");

        mRecyclerView       = (RecyclerView) v.findViewById(R.id.listItem);
        swipeRefreshLayout  = (SwipeRefreshLayout)v.findViewById(R.id.listItem_home_swipe_refresh_layout);
        dropDownMenu        = (RelativeLayout) v.findViewById(R.id.dropdown_categories);
        mSpinner            = (Spinner)v.findViewById(R.id.spinner_categories);
        anchor              = (ImageButton)v.findViewById(R.id.anchor);


        if(getActivity().getResources().getBoolean(R.bool.isTabletX ) || getActivity().getResources().getBoolean(R.bool.isTabletl)) {
            mLayoutManager = new GridLayoutManager(getActivity(),2);
        } else {
            mLayoutManager = new GridLayoutManager(getActivity(),1);
        }

        activity = getActivity();
        mRecyclerView.setLayoutManager(mLayoutManager);

        if(getActivity().getResources().getBoolean(R.bool.isTabletX ) || getActivity().getResources().getBoolean(R.bool.isTabletl)) {
            Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider_home_feed);
            mRecyclerView.addItemDecoration(new DividerItemDecorationWishList(20, 2,dividerDrawable));
        } else {
            Drawable dividerDrawable = ContextCompat.getDrawable(getActivity(), R.drawable.divider_home_feed);
            mRecyclerView.addItemDecoration(new DividerItemDecorationHomeFeed(dividerDrawable));
        }

        setSpinnerData();

        customSpinnerAdapter = new CustomSpinnerAdapter(getActivity(),R.layout.spinner_item,CustomListViewValuesArr);
        mSpinner.setAdapter(customSpinnerAdapter);

        if( sortingValue != null ) {
            if (sortingValue.equals("date_asc"))   {mSpinner.setSelection(1,false);}
            if (sortingValue.equals("price_desc")) {mSpinner.setSelection(2,false);}
            if (sortingValue.equals("price_asc"))  {mSpinner.setSelection(3,false);}
        } else {
            mSpinner.setSelection(0,false);
        }

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
//                Log.d(TAG,"isOpenFromCategory");
                if(!isOpenFromCategory){
                    itemSelect(pos);
                }
//                Log.d(TAG,"mSpinner.setOnItemSelectedListener:" + pos);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        //search items
        mRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(mLayoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {

                int newPage = page + 1;
//                Log.d(TAG,"totalItemsCount:         " + totalItemsCount);
//                Log.d(TAG,"newPage:                 " + newPage);
//                Log.d(TAG,"isRefreshing:            " + isRefreshing);
                getGadget(categoryId,sortingValue,newPage);
                isRefreshing = false;
//                Log.e(TAG, "****************************************************************************************************************************");
//                Log.e(TAG, "Inside onLoadMore, new page " + newPage + ", totalItemCount : " + (totalItemsCount + 10) + ", is refreshing: " + isRefreshing);
//                Log.e(TAG, "****************************************************************************************************************************");
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });
        /**image button <<go to start >> click*/
        anchor.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mRecyclerView.smoothScrollToPosition(0);
                anchor.setVisibility(View.GONE);
            }
        });
        showDialog(getActivity(), "Loading...", false);
        mList = new ArrayList<>();
        getGadget(categoryId,sortingValue,1);

        if(cookie !=null) {
            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mMessageReceiver, new IntentFilter(Constants.LOCAL_BROADCAST_RECEIVER));
        }

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        //the button search is setting to visible
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_share).setVisible(false);
        menu.findItem(R.id.action_filter).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_search:
                return false;
            case R.id.action_share:
                return false;
            case R.id.action_filter:
                isOpenFromCategory = false;
                if(dropDownMenu.getVisibility() == View.GONE) {
                    dropDownMenu.setVisibility(View.VISIBLE);
                } else if(dropDownMenu.getVisibility() == View.VISIBLE) {
                    dropDownMenu.setVisibility(View.GONE);
                }
                return true;
            case android.R.id.home:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mCountDown.start();
//        Log.d(TAG,"onPause");
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        gd.setFragmentName(this.getClass().getSimpleName());
        mCountDown.cancel();
        mCountDown = null;
        mList = null;
        fragment = null;
        activity = null;
//        Log.d(TAG,"onDestroy");
        if(cookie != null){
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mMessageReceiver);
        }

        hideDialog();
    }

    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int pos = intent.getIntExtra("position",0);
            boolean isSaved = intent.getBooleanExtra("isSaved",false);
//            Log.d(TAG,"LOCAL BROADCAST RECEIVER:    pos" + pos  + ", isSaved: " + isSaved);
            //Update the item inside the adapter
            GadgetItem item = mAdapter.getItem(pos);
            item.isSaved = isSaved;
            mAdapter.setItem(pos, item);
        }
    };

    /**
     *
     * @param id of category
     * @param sortingValue desc | ans ...
     * @param loadedPage the value of paging
     */
    public void getGadget(String id, String sortingValue, int loadedPage) {
//        Log.d(TAG,"getGadget" + " ,id: " + id + " ,sortingValue: " + sortingValue + " ,loadedPage: " + loadedPage);
        //if there is internet
        if(Utils.CommonUtils.isNetworkAvailable(getActivity())) {
            Call<MainRss> call;

            if(sortingValue == null) {
                RestClientList restClientList = new RestClientList();
                call = restClientList.getApiService().getGadgets(id, loadedPage);
            } else {
                RestClientSortingCategory restClient = new RestClientSortingCategory();
                call = restClient.getApiService().getGadgets(id,loadedPage,sortingValue);
            }

            call.enqueue(new Callback<MainRss>() {
                @Override
                public void onResponse(Response<MainRss> response, Retrofit retrofit) {
                    if (response.isSuccess()) {
                        MainRss mainRss = response.body();
                        ArrayList<GadgetItem> items = mainRss.getmChannel().getItems();
                        processResult(items);
                        swipeRefreshLayout.setEnabled(true);
//                        Log.d(TAG,"onResponse successful");
                    }
                }

                @Override
                public void onFailure(Throwable t) {
                    swipeRefreshLayout.setEnabled(true);

                    if(getActivity() != null){
                        hideDialog();
                    }
                }
            });


        } else {

            if( getActivity() != null ) {
                Toast.makeText(getActivity(),"No internet! Please check your network", Toast.LENGTH_LONG).show();
                hideDialog();
            }

        }
    }
    /** Proccess Result - helper method for creating the list**/
    private void processResult(Object arg) {
//        Log.d(TAG,"processResult");
        if(isAdded()) {
            if (arg != null) {
                //items of the list - Gadget Items
                ArrayList<GadgetItem> items = (ArrayList<GadgetItem>) arg;
                if (mList == null)
                    mList = new ArrayList<>();
                mList.addAll(items); // add all the items to the list of the fragment
                if (mAdapter == null) {
                    //setting the adapter
                    mAdapter = new AdapterListItem(getActivity(), this, mList,waitingDialog);
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
            //changeFooterState(true);//hide the progress dialog
            hideDialog();
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /** when an Item of the list is clicked*/
    public void onListViewItemClick(final int position) {
        try {
            GadgetItem data = mList.get(position);
            Bundle lbundle = new Bundle();
            lbundle.putSerializable("data", data);
            lbundle.putInt("position",position);
            Intent intent = new Intent(getActivity(), ActivityDetails.class);
            intent.putExtra("data", lbundle);
            getActivity().startActivity(intent);
            getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    /** Anchor **/
    private CountDownTimer mCountDown = new CountDownTimer(300000, 300000) {

        @Override
        public void onTick(long millisUntilFinished) {

        }
        @Override
        public void onFinish() {
            if (mLayoutManager.findLastVisibleItemPosition()> 10 )
                anchor.setVisibility(View.VISIBLE);
        }
    };

    /****** Function to set data in ArrayList *************/
    public void setSpinnerData() {
        String[] itemArray = getResources().getStringArray(R.array.spinner_array);

        for (int i = 0; i < itemArray.length; i++ ) {
            SpinnerItem sched = new SpinnerItem();
            sched.setFilterName(itemArray[i]);
            CustomListViewValuesArr.add(sched);
        }
    }

    public void itemSelect(int positions){

        switch (positions){
            case 0:
                loadSortingPage(null);
//                Log.d(TAG,"sorting : null");
                break;
            case 1:
                loadSortingPage("date_asc");
//                Log.d(TAG,"date_asc");
                break;
            case 2:
                loadSortingPage("price_desc");
//                Log.d(TAG,"price_desc");
                break;
            case 3:
                loadSortingPage("price_asc");
//                Log.d(TAG,"price_asc");
                break;
        }
    }

    public void loadSortingPage(String sortingValue){
//        if(gd.printLogs)Log.d(TAG,"SORTING VALUE:" + sortingValue);
//        Log.d(TAG,"loadSortingPage: " + sortingValue );
        Fragment lFragment = new FragmentListItems();
        FragmentManager lFragmentManager;
        if(activity!= null) {
            lFragmentManager = activity.getFragmentManager(); //vf: Crashlytics 232 activity instead of getActivity
        } else {
            lFragmentManager = getActivity().getFragmentManager(); //vf: Crashlytics 235 else statement of getActivity
        }
        Bundle mBundle = new Bundle();
        item.setSortingValuel(sortingValue);
        mBundle.putParcelable("Item", item);
        mBundle.putBoolean("openFromCategory", true);
        lFragment.setArguments(mBundle);
        lFragmentManager.beginTransaction().replace(R.id.frame_container ,lFragment ).commitAllowingStateLoss(); //vf: Crashlytics 228 commitAllowingStateLoss()
    }

    public void fetchWishList(String sortingValue){
        /*********** Fetch wish list */
        if(CommonUtils.isNetworkAvailable(getActivity()) ){

            new requestWishList().execute(Constants.GET_WISH_LIST.concat(cookie),sortingValue);
        } else{
            //No internet
//            Log.d(TAG,"fetchWishList:   No internet!" );
            Toast.makeText(getActivity(),"No internet! Please check your network", Toast.LENGTH_LONG).show();
            hideDialog();
        }
    }

    private String Request(String url) throws IOException {

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private void refreshContent(){
        showDialog(getActivity(),getActivity().getResources().getString(R.string.process_dialog_swipe_refresh),false);
        swipeRefreshLayout.setEnabled(false);

        if(mList !=null) {
            mList.clear();
        }

        isRefreshing = true;

        if(cookie != null){
            fetchWishList(sortingValue);
        } else {
            getGadget(categoryId,sortingValue,1);
        }

//        Log.d(TAG,"*****************");
//        Log.d(TAG,"INSIDE ON REFRESH");
//        Log.d(TAG,"*****************");

        swipeRefreshLayout.setRefreshing(false);
    }

    public void showDialog(Activity ctx, String message, boolean cancelable) {
//        Log.d(TAG,"*****************");
//        Log.d(TAG,"showDialog");
//        Log.d(TAG,"*****************");
        if (waitingDialog != null) {
            hideDialog();
            waitingDialog = null;
        }
        if(ctx != null) { /** vf : craslytics 224**/
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
//        Log.d(TAG,"*****************");
//        Log.d(TAG,"hideDialog");
//        Log.d(TAG,"*****************");
        if (waitingDialog != null && waitingDialog.isShowing())
            waitingDialog.dismiss();
    }

    public static abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
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

    private class requestWishList extends AsyncTask<String, Void, JSONObject> {
        private String sortingValue;
        @Override
        protected JSONObject doInBackground(String... strings) {
            sortingValue = strings[1];
            JSONObject jsonObject = null;
            try {
                String jsonData = Request(strings[0]);
                jsonObject= new JSONObject(jsonData);
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return jsonObject;
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            try {
//                Log.d("billy",result.toString());
                if(result.getString("status").equals("error")){
                    hideDialog();
//                    Log.e(TAG,"request wish list " + result.getString("status"));
                } else if(result.getString("status").equals("OK")) {
//                    Log.d(TAG,"request wish list " + result.getString("status"));
                    gd.items = result.getString("items");
                    getGadget(categoryId, sortingValue, 1);
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
        }
    }
}
