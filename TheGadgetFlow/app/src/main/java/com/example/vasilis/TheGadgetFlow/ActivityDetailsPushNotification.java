package com.example.vasilis.TheGadgetFlow;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.CloudieNetwork.GadgetFlow.R;
import com.facebook.login.LoginManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import Adapter.CustomPageAdapter;
import Helper.Constants;
import Helper.GD;
import Helper.MemoryCache;
import Model.Model_Gadget.GadgetItem;
import Model.Model_Gadget.MainRss;
import Model.Model_Image_Gallery.Rss;
import Model.Model_Image_Gallery.imageGalleryItem;
import RSS.ApiService.GetGadgetByID;
import RSS.RestClient.RestClientImageGallery;
import Utils.CommonUtils;
import View.CirclePageIndicator;
import io.intercom.android.sdk.Intercom;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import retrofit.SimpleXmlConverterFactory;

/*
******************************
*  8         8    888888888  *
*   8       8     8          *
*    8     8      888888888  *
*     8   8       8          *
*      8 8        8          *
*       8     .   8         .*
******************************
* Vasilis Fouroulis
 */
public class ActivityDetailsPushNotification extends AppCompatActivity {

	private static final String PRODUCT_ID_KEY = "product_id_key";
	private static final String TAG = "ACTIVITY_DETAILS_PUSH";
	private Toolbar mToolbar;
	private GadgetItem gadgetItem;
	private boolean isPressedFirstTime = false;
	private String cookie,id;
	private ViewPager mViewPager;
	private CustomPageAdapter mCustomPageAdapter;
	private CirclePageIndicator mIndicator;
	private ProgressBar mProgressBar;
	private Boolean removedFromWishList;
	private RelativeLayout addtoWishList, buyNow;
	private TextView addToWishListTextView;
	private ImageView addToWishListImageView;
	private ProgressDialog dialog, DialogAddRemoveItem;
	private okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
	private TextView tv_buy_now;
	private boolean productIsAdded;
	private SharedPreferences.Editor editor;
	private SharedPreferences pref;
	private MemoryCache mc = MemoryCache.get();
	private GD gd = GD.get();
	private boolean isSaved;
	private Activity activity;
	private Typeface openSans;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gadget_flow_detail);
		openSans = Typeface.createFromAsset(this.getAssets(), "fonts/OpenSans-Regular.ttf");
		StringBuffer URL;
		activity = this;

		pref = this.getSharedPreferences("gadgetflow", 0);// 0 - for private mode
		editor = pref.edit();
		cookie = pref.getString(Constants.COOKIE, cookie);

		Log.d(TAG,"ONCREATE");

		/***********************************************************************************************
		 * vf: Views Declaration
		 ***********************************************************************************************/
		addtoWishList = (RelativeLayout) findViewById(R.id.btnaddtowishlist);
		addToWishListTextView =(TextView)findViewById(R.id.btnaddtowishlist_tv);
		addToWishListTextView.setTypeface(openSans);
		addToWishListImageView = (ImageView)findViewById(R.id.btnaddtowishlist_iv);
		buyNow = (RelativeLayout)findViewById(R.id.btnBuyNow);
		tv_buy_now = (TextView)findViewById(R.id.tv_buy_now);
		tv_buy_now.setTypeface(openSans);
		mProgressBar = (ProgressBar)findViewById(R.id.pbLoadingDescription);
		dialog = new ProgressDialog(this);
		DialogAddRemoveItem = new ProgressDialog(this);

		initToolbar();

		getProductWithID(getIntent());

		//button buy now
		buyNow.setOnClickListener(new CenterOnClick());

		//button add to wish list
		addtoWishList.setOnClickListener(new CenterOnClick());
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		menu.findItem(R.id.action_search).setVisible(false);
		menu.findItem(R.id.action_share).setVisible(true);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar actions click
		switch (item.getItemId()) {
			case R.id.action_search:
				return false;
			case R.id.action_share:
				doShare();
				return true;
			case android.R.id.home:
				ActivityDetailsPushNotification.this.finish();
				ActivityDetailsPushNotification.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

				return true;
			default:
				return super.onOptionsItemSelected(item);

		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		this.finish();
		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}

	@Override
	public void onPause(){
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		gadgetItem = null;
		mc = null;
		activity = null;
	}

	private void getProductWithID(Intent intent) {
		String id;
		if(intent != null){
			id = intent.getExtras().getString(PRODUCT_ID_KEY);

			if( id!= null) {
				getGadget(id);
			}
		}
	}

	private void getGadget(String id) {
		if(Utils.CommonUtils.isNetworkAvailable(this)) {
			Call<MainRss> call;

			Retrofit restAdapter = new Retrofit.Builder()
					.baseUrl(Constants.BASE_URL)
					.addConverterFactory(SimpleXmlConverterFactory.create())
					.build();

			GetGadgetByID apiService = restAdapter.create(GetGadgetByID.class);
			call = apiService.getGadget(id);

			dialog.setMessage("Loading...");
			dialog.show();

			call.enqueue(new Callback<MainRss>() {
				@Override
				public void onResponse(Response<MainRss> response, Retrofit retrofit) {
					if (response.isSuccess()) {
						MainRss mainRss = response.body();
						ArrayList<GadgetItem> items = mainRss.getmChannel().getItems();
						displayGadget(items.get(0));
//                        Log.d(TAG,"onResponse successful");
					}
				}

				@Override
				public void onFailure(Throwable t) {
					Log.e(TAG,"onResponse failure");
					Log.e(TAG, t.toString());
					dialog.dismiss();
					ActivityDetailsPushNotification.this.finish();
				}
			});
		} else {
			Toast.makeText(this,"No internet! Please check your network", Toast.LENGTH_LONG).show();
		}
	}

	private void displayGadget(GadgetItem item) {
		gadgetItem = item;
		gadgetItem.gadget_buyLink = CommonUtils.getBuyLink(gadgetItem.encoded);
		StringBuffer URL = new StringBuffer(gadgetItem.gadget_link).append("?feed=single_gallery_feed");

		//Create the URL to add or remove product and Fetch wish list
		if(cookie != null){
			String[] part = gadgetItem.guid.split("=");

			if(part.length == 1){
				id = part[0];
			} else if(part.length == 2){
				id = part[1];
			}

			/** Fetch wish list */
			if(CommonUtils.isNetworkAvailable(this) ){

				new requestWishList().execute(Constants.GET_WISH_LIST.concat(cookie));
			} else {
				//No internet
				Toast.makeText(this,"No internet! Please check your network", Toast.LENGTH_LONG).show();
			}
		} else {
            dialog.dismiss();
        }

		if(CommonUtils.isNetworkAvailable(this)) {
			RestClientImageGallery restClient = new RestClientImageGallery();
			Call<Rss> call = restClient.getApiService().getGadgets(URL.toString());
			call.enqueue(new Callback<Rss>() {
				@Override
				public void onResponse(Response<Rss> response, Retrofit retrofit) {
					if (response.isSuccess()) {
						Rss rss = response.body();

						imageGalleryItem item = rss.getmChannel().getItem();

						mCustomPageAdapter = new CustomPageAdapter(getApplication(), CommonUtils.getImages(item.getEncoded()),mProgressBar,cookie);
						mViewPager = (ViewPager) findViewById(R.id.view_pager);
						mViewPager.setAdapter(mCustomPageAdapter);
						mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
						mIndicator.setViewPager(mViewPager);
					}
				}

				@Override
				public void onFailure(Throwable t) {
					Toast.makeText(ActivityDetailsPushNotification.this, "No internet! Please check your network", Toast.LENGTH_LONG).show();
				}
			});
		} else {
			Toast.makeText(ActivityDetailsPushNotification.this, "No internet! Please check your network", Toast.LENGTH_LONG).show();
		}

		Typeface montserrat = Typeface.createFromAsset(this.getAssets(), "fonts/Montserrat-Regular.ttf");

		//Populate Gadget flow description - title
		if (gadgetItem != null) {
			if(CommonUtils.getPrice(gadgetItem.encoded) != null) {
				((TextView) mToolbar.findViewById(R.id.toolbar_title)).setText(CommonUtils.getPrice(gadgetItem.encoded));
			} else {
				((TextView) mToolbar.findViewById(R.id.toolbar_title)).setText(" " + gadgetItem.gadget_title);
			}
			((TextView)findViewById(R.id.tvGadgetTitle)).setText(gadgetItem.gadget_title);
			((TextView)findViewById(R.id.tvGadgetTitle)).setTypeface(montserrat);
			if(CommonUtils.getDescription(gadgetItem.encoded) != null) {
				((TextView) findViewById(R.id.tvDescriptionItem)).setText(CommonUtils.getDescription(gadgetItem.encoded));
				((TextView) findViewById(R.id.tvDescriptionItem)).setMovementMethod(new ScrollingMovementMethod());
				((TextView) findViewById(R.id.tvDescriptionItem)).setTypeface(openSans);
			}
			(findViewById(R.id.lnDetails)).setVisibility(View.VISIBLE);
		}
	}

	private void initToolbar() {
		mToolbar = (Toolbar)findViewById(R.id.toolbar);
		(mToolbar.findViewById(R.id.toolbar_title)).setVisibility(View.VISIBLE);
		((TextView)mToolbar.findViewById(R.id.toolbar_title)).setTypeface(openSans);

		//hide logo
		ImageView logo = (ImageView) mToolbar.findViewById(R.id.logo);
		logo.setVisibility(View.GONE);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);//disable toolbar title
	}

	private void doShare(){
		final ProgressDialog mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(android.R.attr.progressBarStyleSmallInverse);
		mProgressDialog.setMessage("Generating...");
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();

		// TODO Auto-generated method stub
		String shareData = gadgetItem.gadget_link + "\nHey,Look what an amazing gadget I've found,\nDownload this awesome application powered by The Gadget Flow";
		shareData = gadgetItem.gadget_link;
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, shareData);
		startActivity(Intent.createChooser(intent, "Share Via"));
		mProgressDialog.dismiss();
	}

	private String Request(String url) throws IOException {

		okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
		okhttp3.Response response = client.newCall(request).execute();
		return response.body().string();
	}

	private void changeButton(boolean remove){
		if(remove){
			addToWishListTextView.setText(getResources().getText(R.string.gadget_details_saved_wishlist));
			addToWishListImageView.setBackgroundResource(R.drawable.added);
			Resources res = getResources();
			Drawable drawable = res.getDrawable(R.drawable.selector_btn_default_3);
			addtoWishList.setBackgroundDrawable(drawable);
			productIsAdded = true;
		} else {
			addToWishListTextView.setText(getResources().getText(R.string.gadget_details_save_wishlist));
			addToWishListImageView.setBackgroundResource(R.drawable.not_added);
			//// TODO: 10/9/2016 listener 
			Resources res = getResources();
			Drawable drawable = res.getDrawable(R.drawable.selector_btn_default_2);
			addtoWishList.setBackgroundDrawable(drawable);
			productIsAdded = false;
		}
	}

	private void logOut() {
		// sign out - delete token from the device
		editor.clear();
		editor.commit();
		//sing out - clear facebook session
		try {
			LoginManager.getInstance().logOut();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//Clear gd of wish list
		if (mc.wishList != null) {
			mc.wishList.clear();
		}

		gd.items = "";

		Intercom.client().reset(); //log out from Iterncom


		Intent intent = new Intent(this, LogInActivity.class);
		startActivity(intent);
		this.finish();
	}

	public void openUrlOnExternalBrowser(String url){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}

	private class CenterOnClick implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btnBuyNow:
					isPressedFirstTime =true;
//					Bundle mBundle  = new Bundle();
//					mBundle.putSerializable("data", gadgetItem);
//					Intent fragmentDetail = new Intent(ActivityDetailsPushNotification.this, GadgetFlowWeb.class);
//					fragmentDetail.putExtra("data", mBundle);
//					ActivityDetailsPushNotification.this.startActivity(fragmentDetail);
//					ActivityDetailsPushNotification.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
					openUrlOnExternalBrowser(gadgetItem.gadget_buyLink);
					break;
				case R.id.btnaddtowishlist:
					if(cookie == null){
						Intent intent = new Intent(ActivityDetailsPushNotification.this , LogInActivity.class);
						startActivity(intent);
					} else {
						if(productIsAdded){
							//remove item from wish list - alter button (add to wish list)
							isSaved =false;
							if(CommonUtils.isNetworkAvailable(getApplicationContext()) ){
								new addRemoveProduct().execute(Constants.REMOVE_ITEM_FROM_WISH_LIST
										.replace("@itemid@",id)
										.replace("@usercok@",cookie));
								changeButton(false);
								removedFromWishList = true;
//								Log.d(TAG,"Item ID=="+id);
								/**remove gd item from wish list**/
								if(mc.wishList!=null){
									GadgetItem removeGadget = null;
									for(GadgetItem item : mc.wishList) {

										if (item.gadget_title.equals(gadgetItem.gadget_title)) {
											removeGadget = item;
										}
									}

									if(removeGadget!=null){
										mc.wishList.remove(removeGadget);
									}
								}
							} else{
								//No internet
								Toast.makeText(ActivityDetailsPushNotification.this,"No internet! Please check your network", Toast.LENGTH_LONG).show();
							}

						}else{
							/**add item to wish list - alter button (remove from wish list)**/
							isSaved =true;
							if(CommonUtils.isNetworkAvailable(getApplicationContext()) ){
								new addRemoveProduct().execute(Constants.ADD_ITEM_TO_WISH_LIST
										.replace("@itemid@",id)
										.replace("@usercok@",cookie));
								if(mc.wishList!=null)
									mc.wishList.add(0, gadgetItem);
								changeButton(true);
							} else{
								//No internet
								Toast.makeText(ActivityDetailsPushNotification.this,"No internet! Please check your network", Toast.LENGTH_LONG).show();
							}

						}
					}
					break;
				default:
					break;
			}
		}
	}

	private class requestWishList2 extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... strings) {
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
		protected void onPreExecute(){
			super.onPreExecute();

		}

		@Override
		protected void onPostExecute(JSONObject result) {
			try {
				if(result.getString("status").equals("error")){
					DialogAddRemoveItem.dismiss();
				} else if(result.getString("status").equals("OK")) {
					gd.items = result.getString("items");
					DialogAddRemoveItem.dismiss();
				}
			} catch (JSONException e) {
				e.printStackTrace();
				DialogAddRemoveItem.dismiss();
				if( activity!= null)Toast.makeText(activity, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();

			} catch (Exception e){
				e.printStackTrace();
				DialogAddRemoveItem.dismiss();
				if( activity!= null)Toast.makeText(activity, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
			}
		}
	}

	private class requestWishList extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... strings) {
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
		protected void onPreExecute(){
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(JSONObject result) {
			try {
				if(result.getString("status").equals("error")){
					dialog.dismiss();
					//Log out
					logOut();
				} else if(result.getString("status").equals("OK")) {
					if(result.toString().contains(id)) {
						changeButton(true);
					}
					if(dialog !=null && dialog.isShowing()){
						dialog.dismiss();
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
				dialog.dismiss();
				Toast.makeText(ActivityDetailsPushNotification.this, "No internet! Please check your network",Toast.LENGTH_LONG).show();

			}

		}
	}

	private class addRemoveProduct extends AsyncTask<String, Void, JSONObject> {

		@Override
		protected JSONObject doInBackground(String... strings) {
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
		protected void onPreExecute(){
			super.onPreExecute();

			DialogAddRemoveItem.setMessage("Loading...");
			DialogAddRemoveItem.show();
		}
		@Override
		protected void onPostExecute(JSONObject result) {
			try {
				if(result.getString("status").equals("error")){
					logOut();
				} else if(result.getString("status").equals("OK")) {
					//Log.d("billy",result.toString());
					//change the button if the product already exist in the wish list

					new requestWishList2().execute(Constants.GET_WISH_LIST.concat(cookie));

//					Intent intent = new Intent(Constants.LOCAL_BROADCAST_RECEIVER);
//					// You can also include some extra data.
//					intent.putExtra("position", position);
//					intent.putExtra("isSaved", isSaved);
//					LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				DialogAddRemoveItem.dismiss();
				if( activity!= null)Toast.makeText(activity, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();

			} catch(Exception e) {
				e.printStackTrace();
				DialogAddRemoveItem.dismiss();
				if( activity!= null)Toast.makeText(activity, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
			}
		}
	}
}
