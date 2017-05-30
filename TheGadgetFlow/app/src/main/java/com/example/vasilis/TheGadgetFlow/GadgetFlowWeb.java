package com.example.vasilis.TheGadgetFlow;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import com.CloudieNetwork.GadgetFlow.R;
import Model.Model_Gadget.GadgetItem;

public class GadgetFlowWeb extends AppCompatActivity {

	/*** Declare variable **/
	private GadgetItem mData;
	private ImageButton mBtnReload,mBtnNext,mBtnPrev;
	private WebView mWebview;
	private boolean mIsLoadFinish = false;
	private String action_bar_title, web_url;
	private String[] settings_data;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gadget_flow_web);
		savedInstanceState = getIntent().getExtras().getBundle("data");
		/*************************************************************************************************
		 *       vf :Toolbar customization
		 **************************************************************************************************/
		Toolbar mToolbar = (Toolbar)findViewById(R.id.toolbar);
		setSupportActionBar(mToolbar);
		TextView mTitles = (TextView) mToolbar.findViewById(R.id.toolbar_title);
		mTitles.setVisibility(View.VISIBLE);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);//disable toolbar title
		mToolbar.setNavigationIcon(R.drawable.ic_action_back);


		if (savedInstanceState != null) {
			mData = (GadgetItem) savedInstanceState.getSerializable("data");
			settings_data = savedInstanceState.getStringArray("settings");

			//Mode Buy Item
			if(mData!= null) {
				action_bar_title = mData.gadget_title;
				web_url = mData.gadget_buyLink;
			} else if(settings_data != null){
				action_bar_title = settings_data[0];
				web_url = settings_data[1];

			}
		}
		if(action_bar_title.length() < 25 ){
			mTitles.setText(action_bar_title);
		} else {
			String new_title ="";
			for(int i = 0; i < 25; i++) {

				new_title = new_title + action_bar_title.charAt(i);
			}
			new_title = new_title + " ...";
			mTitles.setText(new_title);
		}


		/*
		 * vf: initialize the widgets
		 */
		if (mData != null || settings_data!= null) {
			((WebView) findViewById(R.id.wvBuyItem)).loadUrl(web_url);
		}


		mBtnReload = (ImageButton) findViewById(R.id.btnReload);
		mBtnReload.setOnClickListener(new CenterOnClick());

		mBtnPrev = (ImageButton) findViewById(R.id.btnPrev);
		mBtnPrev.setOnClickListener(new CenterOnClick());

		mBtnNext = (ImageButton) findViewById(R.id.btnNext);
		mBtnNext.setOnClickListener(new CenterOnClick());

		ImageButton mBtnOpenIn = (ImageButton) findViewById(R.id.btnOpenIn);
		mBtnOpenIn.setOnClickListener(new CenterOnClick());

		mWebview = ((WebView) findViewById(R.id.wvBuyItem));
		WebSettings webSettings = mWebview.getSettings();
		webSettings.setLoadWithOverviewMode(true);
		webSettings.setUseWideViewPort(true);
		webSettings.setBuiltInZoomControls(true);
		webSettings.setJavaScriptEnabled(true);
		webSettings.setSupportZoom(false);

		((WebView) findViewById(R.id.wvBuyItem)).setWebViewClient(new myWebClient());

		((WebView) findViewById(R.id.wvBuyItem)).loadUrl(web_url);

	}

	private void openWithBrowser() {
		final CharSequence[] items = { "Open in Browser", "Cancel" };
		AlertDialog.Builder builder = new AlertDialog.Builder(GadgetFlowWeb.this);
		builder.setTitle(web_url);
		builder.setItems(items, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				if (items[item].equals("Cancel")) {
					dialog.dismiss();
				} else {
					Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(web_url));
					startActivity(browserIntent);
				}
			}
		});
		builder.show();
	}

	public class myWebClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			super.onPageStarted(view, url, favicon);
			findViewById(R.id.pbLoadingContentWebView).setVisibility(View.VISIBLE);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;

		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			findViewById(R.id.pbLoadingContentWebView).setVisibility(View.INVISIBLE);
			mIsLoadFinish = true;
			enableControllerButton();
		}

		@Override
		public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
			super.onReceivedError(view, errorCode, description, failingUrl);
			// CommonUtils.showToast(getApplicationContext(), description,
			// Toast.LENGTH_SHORT);
			findViewById(R.id.pbLoadingContentWebView).setVisibility(View.INVISIBLE);
			enableControllerButton();
		}
	}

	private void enableControllerButton() {
		if (mIsLoadFinish) {
			mBtnReload.setEnabled(true);
			if (mWebview.canGoBack()) {
				mBtnPrev.setEnabled(true);
				mBtnPrev.setAlpha(1.0f);
			} else {
				mBtnPrev.setEnabled(false);
				mBtnPrev.setAlpha(0.5f);
			}
			if (mWebview.canGoForward()) {
				mBtnNext.setEnabled(true);
				mBtnNext.setAlpha(1.0f);
			} else {
				mBtnNext.setEnabled(false);
				mBtnNext.setAlpha(0.5f);
			}
		} else {
			mBtnPrev.setEnabled(false);
			mBtnPrev.setAlpha(0.5f);
			mBtnNext.setEnabled(false);
			mBtnNext.setAlpha(0.5f);
		}
	}

	private class CenterOnClick implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btnReload:
				mWebview.reload();
				enableControllerButton();
				break;
			case R.id.btnPrev:
				mWebview.goBack();
				enableControllerButton();
				break;
			case R.id.btnNext:
				mWebview.goForward();
				enableControllerButton();
				break;
			case R.id.btnOpenIn:
				openWithBrowser();
				break;
			default:
				break;
			}
		}
	}


	@Override
	public void onBackPressed() {
		super.onBackPressed();
		mWebview.stopLoading();
		this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		menu.findItem(R.id.action_search).setVisible(false);
		menu.findItem(R.id.action_share).setVisible(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// Handle action bar actions click
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				GadgetFlowWeb.this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
				return false;
			default:
				return super.onOptionsItemSelected(item);

		}
	}
}
