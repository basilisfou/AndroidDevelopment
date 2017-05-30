package Fragments;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
//import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.CloudieNetwork.GadgetFlow.R;

import JSON.RestClient;
import Adapter.AdapterCategories;
import Helper.Constants;
import Helper.GD;
import Model.Categories;
import Model.CategoryItem;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import View.CategoryItemDecoration;

/**
 * Created by Vassilis Fouroulis  on 3/10/2015.
 */
public class FragmentCategories extends Fragment {

    private ImageButton ib_categories;
    private Categories Categories_;
    private RecyclerView mRecycler;
    private AdapterCategories mAdapter;
    private LinearLayoutManager layoutManager;
    private final static String TAG = "FragmentCategoriesT";
    private onCategoryClickedListener mOnCategoryClickedListener;
    private GD gd = GD.get();
    private View view;
    private Context context;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;

        try {
            mOnCategoryClickedListener = (onCategoryClickedListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onCategoryClickedListener");
        }
    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        Activity activity = null;
        if(c instanceof Activity){
            activity = (Activity) c;
        }
        context = activity;
        try {
            mOnCategoryClickedListener = (onCategoryClickedListener)activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement onCategoryClickedListener");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup parent , Bundle savedInstanceState) {

        //initialize the view
        view = inflater.inflate(R.layout.fragment_categories, parent, false);

        ib_categories = (ImageButton)getActivity().findViewById(R.id.categories);
        mRecycler = (RecyclerView)view.findViewById(R.id.categories_list);

        // get categories
        if(Utils.CommonUtils.isNetworkAvailable(getActivity())) {
            if(gd.getCategories() == null){
//                Log.d(TAG,"load from internet");
                /** Asynchronous rest Api call and call back taking the categories **/
                getCat();
            }
        } else {
            //No internet
            Toast.makeText(getActivity(),"No internet! Please check your network", Toast.LENGTH_LONG).show();
        }


        if(gd.getCategories() != null) {
            displayCat();
        }

        return view;
    }

    /** opening a new Item from the category list **/
    public void openListFragment(int ItemPosition) {

        CategoryItem item = gd.getCategories().getCategories().get(ItemPosition);
        item.setSortingValuel(null);
        gd.setFragmentListItem(item);
        mOnCategoryClickedListener.onCategoryClicked(item);
        Fragment lFragment = new FragmentListItems();
        FragmentManager lFragmentManager = getActivity().getFragmentManager();
        //send data to the
        Bundle mBundle = new Bundle();
        mBundle.putParcelable("Item", item);
        mBundle.putBoolean("openFromCategory", true);

        lFragment.setArguments(mBundle);
        //To the fragment
        try {
            lFragmentManager.beginTransaction().replace(R.id.frame_container, lFragment).commit();
        } catch (IllegalStateException ignored){

        }
    }

    @Override
     public void onPause(){
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        gd.setFragmentName(this.getClass().getSimpleName());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_search:

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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Interface that listens to the clicks of the categories ,
     * Implemented by Main Activity , used by this Fragment
     */
    public interface onCategoryClickedListener{
        void onCategoryClicked(CategoryItem item);
    }

    public void getCat(){
        /** Asynchronous rest Api call and call back taking the categories **/
        RestClient restClient = new RestClient();
        Call<Categories> callback = restClient.getApiService().getCategories();
        callback.enqueue(new Callback<Categories>() {
            @Override
            public void onResponse(Response<Categories> response, Retrofit retrofit) {
                Categories_ = response.body();
                gd.setCategories(Categories_);
                displayCat();
            }

            @Override
            public void onFailure(Throwable t) {
//                Log.e(TAG,t.toString());
                if(context != null)Toast.makeText(context, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
            }
        });
    }
    public void displayCat(){
        if(gd.getCategories() != null && gd.getCategories().getCategories() != null) {
            /** vf: craslitics 117 and 243 using context from onAttach and not getActivity**/
            mAdapter = new AdapterCategories(context, gd.getCategories().getCategories(), this);
            mRecycler.setAdapter(mAdapter);
            mAdapter.setOnItemClickListener(new AdapterCategories.OnItemClickListener() {

                //click in a category
                @Override
                public void onItemClick(View view, int position) {
                    ib_categories.setImageResource(R.drawable.categories);
                    openListFragment(position);
                }
            });

            layoutManager = new LinearLayoutManager(context);
            mRecycler.setLayoutManager(layoutManager);
            mRecycler.addItemDecoration(new CategoryItemDecoration());
        }
    }
}

