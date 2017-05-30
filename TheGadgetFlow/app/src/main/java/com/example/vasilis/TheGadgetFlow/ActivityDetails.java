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
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
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

import Adapter.CustomPageAdapter;
import Helper.Constants;
import Helper.GD;
import Helper.MemoryCache;
import Model.Model_Gadget.GadgetItem;
import Model.Model_Image_Gallery.Rss;
import Model.Model_Image_Gallery.imageGalleryItem;
import io.intercom.android.sdk.Intercom;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;
import RSS.RestClient.RestClientImageGallery;
import Utils.CommonUtils;
import View.CirclePageIndicator;

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
public class ActivityDetails extends AppCompatActivity {
	GadgetItem mData;
	boolean isPressedFirstTime = false;
	private String cookie,id;
	private imageGalleryItem item;
	private ViewPager mViewPager;
	private CustomPageAdapter mCustomPageAdapter;
	private CirclePageIndicator mIndicator;
	private ProgressBar mProgressBar;
	private Boolean removedFromWishList;
	private RelativeLayout addtoWishList, buyNow;
	private TextView addToWishListTextView;
	private ImageView addToWishListImageView;
	private ProgressDialog DialogFetchWishList, DialogAddRemoveItem;
	private okhttp3.OkHttpClient client = new okhttp3.OkHttpClient();
	private TextView tv_buy_now;
	private boolean productIsAdded;
	private static final String TAG = "ACTIVITY_DETAILS";
	private SharedPreferences.Editor editor;
	private SharedPreferences pref;
	private MemoryCache mc = MemoryCache.get();
	private GD gd = GD.get();
	private int position;
	private String className;
	private boolean isSaved;
	private Activity activity;

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(intent, requestCode);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gadget_flow_detail);
		Typeface openSans = Typeface.createFromAsset(this.getAssets(), "fonts/OpenSans-Regular.ttf");
		Typeface montserrat = Typeface.createFromAsset(this.getAssets(), "fonts/Montserrat-Regular.ttf");
		ImageView logo;
		StringBuffer URL;
		activity = this;
		/**********************************************************************************************
		 * Session
		 **********************************************************************************************/
		pref = this.getSharedPreferences("gadgetflow", 0);// 0 - for private mode
		editor = pref.edit();
		cookie = pref.getString(Constants.COOKIE, cookie);

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
		DialogFetchWishList = new ProgressDialog(this);
		DialogAddRemoveItem = new ProgressDialog(this);
		/*************************************************************************************************
		* vf :Toolbar customization
		**************************************************************************************************/
		Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar);
		(mToolbar.findViewById(R.id.toolbar_title)).setVisibility(View.VISIBLE);
		((TextView)mToolbar.findViewById(R.id.toolbar_title)).setTypeface(openSans);

		//hide logo
		logo = (ImageView) mToolbar.findViewById(R.id.logo);
		logo.setVisibility(View.GONE);
		setSupportActionBar(mToolbar);
		getSupportActionBar().setDisplayShowTitleEnabled(false);//disable toolbar title
//		mToolbar.setNavigationIcon(R.drawable.ic_action_back);
		//Data extras
		if(getIntent() != null) {
			savedInstanceState = getIntent().getExtras().getBundle("data");
		}

		if(savedInstanceState != null) {
			mData = (GadgetItem) savedInstanceState.getSerializable("data");
			position = savedInstanceState.getInt("position");
			className = savedInstanceState.getString("className");
//			Log.d(TAG,"position == " + position + ", className == " + className);
			 //Getting the Images Of the Image Gallery
			mData.gadget_buyLink = CommonUtils.getBuyLink(mData.encoded);
			URL = new StringBuffer(mData.gadget_link).append("?feed=single_gallery_feed");
			//Log.d(TAG,URL.toString());
			//Create the URL to add or remove product and Fetch wish list
			if(cookie != null){
				String[] part = mData.guid.split("=");

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
			}

			if(CommonUtils.isNetworkAvailable(this)) {
				RestClientImageGallery restClient = new RestClientImageGallery();
				Call<Rss> call = restClient.getApiService().getGadgets(URL.toString());
				call.enqueue(new Callback<Rss>() {
					@Override
					public void onResponse(Response<Rss> response, Retrofit retrofit) {
						if (response.isSuccess()) {
							Rss rss = response.body();

							item = rss.getmChannel().getItem();

							/*************************************************************************************************
							 *       vf: View pager customization - Image Gallery
							 **************************************************************************************************/
							mCustomPageAdapter = new CustomPageAdapter(getApplication(), CommonUtils.getImages(item.getEncoded()),mProgressBar,cookie);
							mViewPager = (ViewPager) findViewById(R.id.view_pager);
							mViewPager.setAdapter(mCustomPageAdapter);
							mIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
							mIndicator.setViewPager(mViewPager);
						}
					}

					@Override
					public void onFailure(Throwable t) {
						Toast.makeText(ActivityDetails.this, "No internet! Please check your network", Toast.LENGTH_LONG).show();
					}
				});
			} else {
				Toast.makeText(ActivityDetails.this, "No internet! Please check your network", Toast.LENGTH_LONG).show();
			}
		}

		//Populate Gadget flow description - title
		if (mData != null) {
			((TextView)mToolbar.findViewById(R.id.toolbar_title)).setText(" " + mData.gadget_price);// changing the title of the action bar with the name of the item
			((TextView)findViewById(R.id.tvGadgetTitle)).setText(mData.gadget_title);
			((TextView)findViewById(R.id.tvGadgetTitle)).setTypeface(montserrat);
			((TextView)findViewById(R.id.tvDescriptionItem)).setText(CommonUtils.getDescription(mData.encoded));
			((TextView)findViewById(R.id.tvDescriptionItem)).setMovementMethod(new ScrollingMovementMethod());
			((TextView)findViewById(R.id.tvDescriptionItem)).setTypeface(openSans);
			(findViewById(R.id.lnDetails)).setVisibility(View.VISIBLE);
		}

		//button buy now
		buyNow.setOnClickListener(new CenterOnClick());

		//button add to wish list
		addtoWishList.setOnClickListener(new CenterOnClick());
	}
	@Override
	public void onBackPressed() {
		super.onBackPressed();

		mData = null;
		mc = null;
		this.finish();
		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

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
				ActivityDetails.this.finish();
				ActivityDetails.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

				return true;
			default:
				return super.onOptionsItemSelected(item);

		}
	}
	@Override
	public void onPause(){
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mData = null;
		mc = null;
		activity = null;
	}

	public void openUrlOnExternalBrowser(String url){
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}

	public void doShare(){
		final ProgressDialog mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setProgressStyle(android.R.attr.progressBarStyleSmallInverse);
		mProgressDialog.setMessage("Generating...");
		mProgressDialog.setCancelable(false);
		mProgressDialog.show();

		// TODO Auto-generated method stub
		String shareData = mData.gadget_link + "\nHey,Look what an amazing gadget I've found,\nDownload this awesome application powered by The Gadget Flow";
		shareData = mData.gadget_link;
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

	public void logOut() {
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

	private class CenterOnClick implements View.OnClickListener{

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.btnBuyNow:
					isPressedFirstTime =true;
//					 Bundle mBundle  = new Bundle();
//					 mBundle.putSerializable("data", mData);
//					 Intent fragmentDetail = new Intent(ActivityDetails.this, GadgetFlowWeb.class);
//					 fragmentDetail.putExtra("data", mBundle);
//					 ActivityDetails.this.startActivity(fragmentDetail);
//					 ActivityDetails.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
					openUrlOnExternalBrowser(mData.gadget_buyLink);
					break;
				case R.id.btnaddtowishlist:
					if(cookie == null){
						Intent intent = new Intent(ActivityDetails.this , LogInActivity.class);
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

										if (item.gadget_title.equals(mData.gadget_title)) {
											removeGadget = item;
										}
									}

									if(removeGadget!=null){
										mc.wishList.remove(removeGadget);
									}
								}
							} else{
								//No internet
								Toast.makeText(ActivityDetails.this,"No internet! Please check your network", Toast.LENGTH_LONG).show();
							}

						}else{
							/**add item to wish list - alter button (remove from wish list)**/
							isSaved =true;
							if(CommonUtils.isNetworkAvailable(getApplicationContext()) ){
								new addRemoveProduct().execute(Constants.ADD_ITEM_TO_WISH_LIST
										.replace("@itemid@",id)
										.replace("@usercok@",cookie));
								if(mc.wishList!=null)
									mc.wishList.add(0,mData);
								changeButton(true);
							} else{
								//No internet
								Toast.makeText(ActivityDetails.this,"No internet! Please check your network", Toast.LENGTH_LONG).show();
							}

						}
					}
					break;
				default:
					break;
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

					Intent intent = new Intent(Constants.LOCAL_BROADCAST_RECEIVER);
					// You can also include some extra data.
					intent.putExtra("position", position);
					intent.putExtra("isSaved", isSaved);
					LocalBroadcastManager.getInstance(activity).sendBroadcast(intent);
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

			DialogFetchWishList.setMessage("Loading...");
			DialogFetchWishList.show();
		}
		@Override
		protected void onPostExecute(JSONObject result) {
			try {
				if(result.getString("status").equals("error")){
					DialogFetchWishList.dismiss();
					//Log out
					logOut();
				} else if(result.getString("status").equals("OK")) {
					if(result.toString().contains(id)) {
						changeButton(true);
					}
					if(DialogFetchWishList!=null && DialogFetchWishList.isShowing()){
						DialogFetchWishList.dismiss();
					}

				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (Exception e){
				e.printStackTrace();
				DialogFetchWishList.dismiss();
				Toast.makeText(ActivityDetails.this, "No internet! Please check your network",Toast.LENGTH_LONG).show();

			}

		}
	}

}
