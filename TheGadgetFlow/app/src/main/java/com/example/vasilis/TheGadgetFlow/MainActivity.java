package com.example.vasilis.TheGadgetFlow;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.CloudieNetwork.GadgetFlow.R;

import java.lang.reflect.Method;
import java.util.ArrayList;
import Fragments.FragmentListItemHome;
import Fragments.FragmentListItems;
import Fragments.FragmentSearch;
import Fragments.FragmentSettings;
import Fragments.FragmentSorting;
import Fragments.FragmentDiscount;
import Fragments.FragmentWishList;
import Fragments.FragmentCategories;
import Adapter.CustomSpinnerAdapter;
import Helper.Constants;
import Helper.GD;
import Model.CategoryItem;
import Model.SpinnerItem;

public class MainActivity extends AppCompatActivity implements
        FragmentCategories.onCategoryClickedListener ,
        FragmentListItemHome.OnScrolledListener,
        FragmentSearch.OnScrolledListener,
        FragmentSorting.OnScrolledListener {

    private ImageButton home, categories, trending, setting, myWishList;
    private LinearLayout ln_search;
    private EditText ed_search;
    private RelativeLayout spinner_container;
    private TextView mTitles;
    private Fragment lFragment;
    private InputMethodManager inputMethod;
    private ImageView imageView;
    private SharedPreferences.Editor editor;
    private SharedPreferences sharedPreferences;
    public static final String TAG = "MainActivityTag";
    public static final String HOME_FRAGMENT = FragmentListItemHome.class.getSimpleName();
    public static final String CATEGORIES_FRAGMENT = FragmentCategories.class.getSimpleName();
    public static final String DISCOUNT_FRAGMENT = FragmentDiscount.class.getSimpleName();
    public static final String FRAGMENT_WISH_LIST = FragmentWishList.class.getSimpleName();
    public static final String FRAGMENT_SETTINGS = FragmentSettings.class.getSimpleName();
    public static final String FRAGMENT_LIST_ITEM = FragmentListItems.class.getSimpleName();
    public static final String FRAGMENT_SORTING = FragmentSorting.class.getSimpleName();
    public static final String FRAGMENT_SEARCH = FragmentSearch.class.getSimpleName();
    private static final String PAGE_CATEGORIES_KEY = "page_categories_key";
    private static final String PAGE_DISCOUNT_KEY   = "page_discount_key";
    private GD gd = GD.get();
    private InputMethodManager imm;
    public ArrayList<SpinnerItem> CustomListViewValuesArr = new ArrayList<SpinnerItem>();
    private Spinner mSpinner;
    private CustomSpinnerAdapter customSpinnerAdapter;
    private int check=0;
    private okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("gadgetflow", 0);// 0 - for private mode
        editor = sharedPreferences.edit();

        ((MyGadgetFlow)getApplicationContext()).activityMainCreated();

        /** vf : Toolbar customization*/
        Toolbar actionbar = (Toolbar)findViewById(R.id.toolbar);
        mTitles = (TextView) actionbar.findViewById(R.id.toolbar_title);
        imageView = (ImageView)actionbar.findViewById(R.id.logo);
        setSupportActionBar(actionbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);//disable toolbar title

        home              = (ImageButton)     findViewById(R.id.home);
        categories        = (ImageButton)     findViewById(R.id.categories);
        trending          = (ImageButton)     findViewById(R.id.treding);
        myWishList        = (ImageButton)     findViewById(R.id.wishlist);
        setting           = (ImageButton)     findViewById(R.id.settings);
        ln_search         = (LinearLayout)    findViewById(R.id.ln_search_home); // the layout for searching
        ed_search         = (EditText)        findViewById(R.id.ed_search_home); // the text box for searching
        spinner_container = (RelativeLayout)  findViewById(R.id.spinner_container);
        mSpinner          = (Spinner)         findViewById(R.id.spinner);

        imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        Log.d(TAG,getIntent().toString());


        handleIntent(getIntent());

        /**Spinner **/
        setSpinnerData();
        customSpinnerAdapter = new CustomSpinnerAdapter(this,R.layout.spinner_item,CustomListViewValuesArr);
        mSpinner.setAdapter(customSpinnerAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                check=check+1;
                if(check > 1) {
                    onFilterSelect(pos);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        ed_search.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
        /** Overriding the soft keyboard for the search text box */
        ed_search.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View v, int Keycode, KeyEvent event) {

                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (Keycode == KeyEvent.KEYCODE_SEARCH) || (Keycode == KeyEvent.KEYCODE_ENTER)) {
                    inputMethod = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethod.hideSoftInputFromWindow(ed_search.getWindowToken(), 0);
                    String key = ed_search.getText().toString();
                    if (key != null && !key.isEmpty()) {
                        //a new fragment searching
                        Bundle mBundle = new Bundle();
                        mBundle.putString("key", key);
                        FragmentSearch nextFrag= new FragmentSearch();
                        nextFrag.setArguments(mBundle);

                        setColorNavigationButton("nothing");
                        getSupportActionBar().setDisplayShowHomeEnabled(false);
                        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                        imageView.setVisibility(View.GONE);
                        mTitles.setVisibility(View.VISIBLE);
                        mTitles.setText(key);

                        getFragmentManager().beginTransaction().replace(R.id.frame_container, nextFrag).commit();
                    }
                }

                return false;
            }
        });

        /*************************************** Navigation of bottom toolbar **************************************/
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getFragmentManager().findFragmentByTag(Constants.HOME_FRAGMENT)== null){
                    displayFragment(HOME_FRAGMENT);
                    setColorNavigationButton(HOME_FRAGMENT);
                }
            }
        });
        categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setColorNavigationButton(CATEGORIES_FRAGMENT);
                displayFragment(CATEGORIES_FRAGMENT);
            }
        });
        trending.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getFragmentManager().findFragmentByTag(Constants.TRENDINGS_FRAGMENT ) == null){
                    setColorNavigationButton(DISCOUNT_FRAGMENT);
                    displayFragment(DISCOUNT_FRAGMENT);
                }
            }
        });
        myWishList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getFragmentManager().findFragmentByTag(Constants.WISHLIST_FRAGMENT) == null){
                    setColorNavigationButton(FRAGMENT_WISH_LIST);
                    displayFragment(FRAGMENT_WISH_LIST);
                }
            }
        });
        setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(getFragmentManager().findFragmentByTag(Constants.SETTINGS_FRAGMENT) == null){
                    setColorNavigationButton(FRAGMENT_SETTINGS);
                    displayFragment(FRAGMENT_SETTINGS);
                }
            }
        });
    }

    /*** Called when invalidateOptionsMenu() is triggered */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_search).setVisible(false);
        menu.findItem(R.id.action_share).setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case android.R.id.home:

                if(spinner_container.getVisibility() == View.VISIBLE){
                    spinner_container.setVisibility(View.GONE);

                } else if(spinner_container.getVisibility() == View.GONE){
                    if(ln_search.getVisibility() == View.VISIBLE){
                        ln_search.setVisibility(View.GONE);

                        showKeyboard(false);
                    }
                    spinner_container.setVisibility(View.VISIBLE);
                }
                return true;
            case R.id.action_search:
                spinner_container.setVisibility(View.GONE);
                rightHeaderButtonClick();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();

        Fragment myFragment = getFragmentManager().findFragmentByTag(Constants.HOME_FRAGMENT);
        if (myFragment != null && myFragment.isVisible()) {
            this.finish();
        } else {
            setColorNavigationButton(HOME_FRAGMENT);
            displayFragment(HOME_FRAGMENT);
            mSpinner.setSelection(0,false);
        }
    }

    @Override
    public void onCategoryClicked(CategoryItem item) {
        mTitles.setText(item.getTitle());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ((MyGadgetFlow)getApplicationContext()).activityMainPaused();
    }

    private void handleIntent(Intent intent) {
        Log.d(TAG,"handleIntent");
        editor.putBoolean(Constants.HOME_FRAGMENT,false);
        editor.apply();

        if (intent.getBooleanExtra(Constants.LOG_IN_INTENT_TO_MAIN,false)){

            /**Loading Home Fragment and it's features - when sign in | skip | sign up **/
            setColorNavigationButton(HOME_FRAGMENT);
            displayFragment(HOME_FRAGMENT);
            Log.d(TAG,Constants.LOG_IN_INTENT_TO_MAIN);
        } else if(intent.getBooleanExtra(PAGE_CATEGORIES_KEY,false)){
            setColorNavigationButton(CATEGORIES_FRAGMENT);
            displayFragment(CATEGORIES_FRAGMENT);
            Log.d(TAG,PAGE_CATEGORIES_KEY);
        } else if(intent.getBooleanExtra(PAGE_DISCOUNT_KEY,false)){
            setColorNavigationButton(DISCOUNT_FRAGMENT);
            displayFragment(DISCOUNT_FRAGMENT);
            Log.d(TAG,PAGE_DISCOUNT_KEY);
        }
    }

    /** Button toolbar changing activated Button */
    public void setColorNavigationButton(String button){
//        Log.d(TAG,"button: " + button);
        if(spinner_container != null){
            spinner_container.setVisibility(View.GONE);
        }

        if(ln_search != null){
            ln_search.setVisibility(View.GONE);
        }

        if (button.equals(HOME_FRAGMENT)) {
            home.setImageResource(R.drawable.home_red);
            categories.setImageResource(R.drawable.categories);
            trending.setImageResource(R.drawable.discounts);
            myWishList.setImageResource(R.drawable.wishlist);
            setting.setImageResource(R.drawable.settings);
            mTitles.setVisibility(View.GONE);
            imageView.setVisibility(View.VISIBLE);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_filter);
            mSpinner.setSelection(0);
        } else if(button.equals(CATEGORIES_FRAGMENT)){
            home.setImageResource(R.drawable.home);
            categories.setImageResource(R.drawable.categories_red);
            trending.setImageResource(R.drawable.discounts);
            myWishList.setImageResource(R.drawable.wishlist);
            setting.setImageResource(R.drawable.settings);
            mTitles.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mTitles.setText(R.string.action_bar_categories);


        } else if(button.equals(DISCOUNT_FRAGMENT)){
            home.setImageResource(R.drawable.home);
            categories.setImageResource(R.drawable.categories);
            trending.setImageResource(R.drawable.discounts_red);
            myWishList.setImageResource(R.drawable.wishlist);
            setting.setImageResource(R.drawable.settings);
            mTitles.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mTitles.setText(R.string.action_bar_discounts);

        } else if(button.equals(FRAGMENT_WISH_LIST)){
            home.setImageResource(R.drawable.home);
            categories.setImageResource(R.drawable.categories);
            trending.setImageResource(R.drawable.discounts);
            myWishList.setImageResource(R.drawable.wishlist_red);
            setting.setImageResource(R.drawable.settings);
            mTitles.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            mTitles.setText(R.string.action_bar_wish_list);

        } else if(button.equals(FRAGMENT_SETTINGS)){
            home.setImageResource(R.drawable.home);
            categories.setImageResource(R.drawable.categories);
            trending.setImageResource(R.drawable.discounts);
            myWishList.setImageResource(R.drawable.wishlist);
            setting.setImageResource(R.drawable.settings_red);
            mTitles.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            mTitles.setText(R.string.action_bar_settings);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        } else if(button.equals("nothing")){
            home.setImageResource(R.drawable.home);
            categories.setImageResource(R.drawable.categories);
            trending.setImageResource(R.drawable.discounts);
            myWishList.setImageResource(R.drawable.wishlist);
            setting.setImageResource(R.drawable.settings);
        } else if(button.equals(FRAGMENT_SORTING)){
            home.setImageResource(R.drawable.home_red);
            categories.setImageResource(R.drawable.categories);
            trending.setImageResource(R.drawable.discounts);
            myWishList.setImageResource(R.drawable.wishlist);
            setting.setImageResource(R.drawable.settings);
        }
    }

    /**  showing the text box **/
    public void rightHeaderButtonClick() {
        if (ln_search.getVisibility() == View.VISIBLE) {
            ln_search.setVisibility(View.GONE);
            showKeyboard(false);
        } else if(ln_search.getVisibility() == View.GONE){
            ln_search.setVisibility(View.VISIBLE);
            ed_search.requestFocus();
            showKeyboard(true);
        }
    }

    /** showing the keyboard */
    public void showKeyboard(boolean isShow) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);

        if (isShow) {
//            ln_search.setVisibility(View.VISIBLE);
//            ed_search.requestFocus();
            // show keyboard
            imm.showSoftInput(ed_search, 0);

        } else {
            // hide keyboard
//            ln_search.setVisibility(View.GONE);
            imm.hideSoftInputFromWindow(ed_search.getWindowToken(), 0);

        }
    }

    /** Filters on click*/
    public void onFilterSelect(int positions){

        switch (positions){
            case 0:
                lFragment = getFragmentManager().findFragmentById(R.id.frame_container);
                lFragment = new FragmentListItemHome();
                getFragmentManager().beginTransaction().replace(R.id.frame_container, lFragment, Constants.HOME_FRAGMENT).commit();
                mTitles.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                spinner_container.setVisibility(View.GONE);
                break;
            case 1:
                Bundle bundle = new Bundle();
                bundle.putString(Constants.VALUE_SORTING,"date_asc");
                lFragment = getFragmentManager().findFragmentById(R.id.frame_container);
                lFragment = new FragmentSorting();
                lFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.frame_container, lFragment, Constants.OLDEST_PRODUCTS).commit();
                mTitles.setVisibility(View.VISIBLE);
                mTitles.setText(getResources().getString(R.string.oldest_products));
                imageView.setVisibility(View.GONE);
                spinner_container.setVisibility(View.GONE);

                break;
            case 2:
                bundle = new Bundle();
                bundle.putString(Constants.VALUE_SORTING, "price_desc");
                lFragment = getFragmentManager().findFragmentById(R.id.frame_container);
                lFragment = new FragmentSorting();
                lFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.frame_container, lFragment, Constants.HIGHEST_PRICE).commit();
                mTitles.setVisibility(View.VISIBLE);
                mTitles.setText(getResources().getString(R.string.highest_price));
                imageView.setVisibility(View.GONE);
                spinner_container.setVisibility(View.GONE);

                break;
            case 3:
                bundle = new Bundle();
                bundle.putString(Constants.VALUE_SORTING, "price_asc");
                lFragment = getFragmentManager().findFragmentById(R.id.frame_container);
                lFragment = new FragmentSorting();
                lFragment.setArguments(bundle);
                getFragmentManager().beginTransaction().replace(R.id.frame_container, lFragment, Constants.LOWEST_PRICE).commit();
                mTitles.setVisibility(View.VISIBLE);
                mTitles.setText(getResources().getString(R.string.lowest_price));
                imageView.setVisibility(View.GONE);
                spinner_container.setVisibility(View.GONE);
                break;
        }
    }

    /** Display Fragment Method - helps to load the right fragment **/
    private void displayFragment(String fragmentName){
        lFragment = getFragmentManager().findFragmentById(R.id.frame_container);

        if(fragmentName.equals(HOME_FRAGMENT)){

            lFragment = new FragmentListItemHome();
            getFragmentManager().beginTransaction().replace(R.id.frame_container, lFragment, Constants.HOME_FRAGMENT).commit();
            //Log.d(TAG,"displayFragment, TO: " + HOME_FRAGMENT);
        }else if(fragmentName.equals(CATEGORIES_FRAGMENT)){

            lFragment = new FragmentCategories();
            getFragmentManager().beginTransaction().replace(R.id.frame_container, lFragment, Constants.CATEGORIES_FRAGMENT).commit();
            //Log.d(TAG, "displayFragment + TO: " + CATEGORIES_FRAGMENT);

        }else if(fragmentName.equals(DISCOUNT_FRAGMENT)){
            lFragment = new FragmentDiscount();
            getFragmentManager().beginTransaction().replace(R.id.frame_container, lFragment, Constants.TRENDINGS_FRAGMENT).commit();
            //Log.d(TAG,"displayFragment + TO: " + DISCOUNT_FRAGMENT);
        }else if(fragmentName.equals(FRAGMENT_WISH_LIST)){
            lFragment = new FragmentWishList();
            getFragmentManager().beginTransaction().replace(R.id.frame_container, lFragment, Constants.WISHLIST_FRAGMENT).commit();
            //Log.d(TAG,"displayFragment + TO: " + FRAGMENT_WISH_LIST);
        }else if(fragmentName.equals(FRAGMENT_SETTINGS)){
            lFragment = new FragmentSettings();
            getFragmentManager().beginTransaction().replace(R.id.frame_container, lFragment, Constants.SETTINGS_FRAGMENT).commit();
            //Log.d(TAG,"displayFragment + TO: " + FRAGMENT_SETTINGS);
        }else if(fragmentName.equals(FRAGMENT_LIST_ITEM)){
            CategoryItem item = gd.getFragmentListItem();
            lFragment = new FragmentListItems();
            Bundle mBundle = new Bundle();
            mTitles.setText(item.getTitle());
            mBundle.putParcelable("Item", item);
            lFragment.setArguments(mBundle);
            getFragmentManager().beginTransaction().replace(R.id.frame_container ,lFragment ).commit();
        }else if(fragmentName.equals(FRAGMENT_SORTING)) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_filter);
            mTitles.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);

            if(gd.getValueStringSorting().equals("price_asc")){
                mTitles.setText(getResources().getString(R.string.lowest_price));
            } else if(gd.getValueStringSorting().equals("date_asc")){
                mTitles.setText(getResources().getString(R.string.oldest_products));

            } else if(gd.getValueStringSorting().equals("price_desc")){
                mTitles.setText(getResources().getString(R.string.highest_price));
            }


            Bundle bundle = new Bundle();
            bundle.putString(Constants.VALUE_SORTING, gd.getValueStringSorting());
            lFragment = getFragmentManager().findFragmentById(R.id.frame_container);
            lFragment = new FragmentSorting();
            lFragment.setArguments(bundle);
            //To the fragment
            getFragmentManager().beginTransaction().replace(R.id.frame_container, lFragment, Constants.LOWEST_PRICE).commit();

        } else if(fragmentName.equals(FRAGMENT_SEARCH)){
            Bundle mBundle = new Bundle();
            mBundle.putString("key", gd.getSearchKey());
            mTitles.setVisibility(View.VISIBLE);
            mTitles.setText(gd.getSearchKey());
            lFragment = new FragmentSearch();
            lFragment.setArguments(mBundle);
            getFragmentManager().beginTransaction().replace(R.id.frame_container, lFragment).commit();
        }

    }

    @Override
    public void onScrolledHome() {
        showKeyboard(false);
        ln_search.setVisibility(View.GONE);
    }

    /****** Function to set data in ArrayList *************/
    public void setSpinnerData() {
        String[] itemArray = getResources().getStringArray(R.array.spinner_array);

        for (int i = 0; i < itemArray.length; i++ ) {
            SpinnerItem sched = new SpinnerItem();
            /******* Firstly take data in model object ******/
            sched.setFilterName(itemArray[i]);
            CustomListViewValuesArr.add(sched);
        }
    }

    public void hideSpinnerDropDown(Spinner spinner) {
        try {
            Method method = Spinner.class.getDeclaredMethod("onDetachedFromWindow");
            method.setAccessible(true);
            method.invoke(spinner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


