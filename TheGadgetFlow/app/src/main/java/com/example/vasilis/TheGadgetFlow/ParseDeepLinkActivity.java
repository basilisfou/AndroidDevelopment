package com.example.vasilis.TheGadgetFlow;

import android.app.Activity;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by vasilis Fouroulis on 20/4/2017.
 */

public class ParseDeepLinkActivity extends Activity {
    public static final String TAG                 = "ParseDeepLinkActivity";

    public static final String FETCH_PRODUCT_BY_ID = "product_id";
    public static final String PAGE_CATEGORIES     = "page_categories";
    public static final String PAGE_DISCOUNT       = "page_discounts";

    private static final String PRODUCT_ID_KEY      = "product_id_key";
    private static final String PAGE_CATEGORIES_KEY = "page_categories_key";
    private static final String PAGE_DISCOUNT_KEY   = "page_discount_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG,"ONCREATE");

        Intent intent = getIntent();
        if (intent == null || intent.getData() == null) {
            finish();
        }

        openDeepLink(intent.getData());

        // Finish this activity
        finish();
    }

    private void openDeepLink(Uri deepLink) {
        String host = deepLink.getHost();
        Log.d(TAG,host);
        if (host.contains(FETCH_PRODUCT_BY_ID)) {
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

            String[] strings = host.split("=");
            Log.d(TAG,"ID=" + strings[1]);
            if(strings[1] != null ) {
                Intent intent = new Intent(this, ActivityDetailsPushNotification.class);
                intent.putExtra(PRODUCT_ID_KEY, strings[1]);


                //vf: if main activity is open start ActivityDetailsPushNotification else start new taskBuilder with LogInActivity and ActivityDetailsPushNotification
                if(((MyGadgetFlow)getApplicationContext()).isMainActivityActive()){
                    startActivity(intent);
                    Log.d(TAG,"isMainActivityActive Start ActivityDetailsPushNotification" );
                } else {
                    stackBuilder.addNextIntentWithParentStack(new Intent(this, LogInActivity.class));
                    stackBuilder.addNextIntent(intent);
                    stackBuilder.startActivities();
                }

            } else {
                startActivity(new Intent(this, LogInActivity.class));
            }

        } else if(PAGE_CATEGORIES.equals(host)){
            Log.d(TAG,"*************************PAGE CAT");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(PAGE_CATEGORIES_KEY,true);
            startActivity(intent);
        } else if (PAGE_DISCOUNT.equals(host)){
            Log.d(TAG,"*************************PAGE_DISCOUNT");
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(PAGE_DISCOUNT_KEY,true);
            startActivity(intent);
        } else {
            // Fall back to the main activity
            startActivity(new Intent(this, LogInActivity.class));
        }
    }
}
