package services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.veriah.tennisquare.MainActivity;
import com.veriah.tennisquare.R;
import com.veriah.tennisquare.challenge.ViewChallenge;
import com.veriah.tennisquare.eshop.EshopActivity;
import com.veriah.tennisquare.eshop.EshopWebView;
import com.veriah.tennisquare.pushNotifications.GamePushNotificationMap;
import com.veriah.tennisquare.pushNotifications.ViewPushNotificationChallenge;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import CommonUtils.Constants;
import receivers.NotificationActionReceiver;


/** Created by Vasilis Fouroulis on 03/02/2017. **/

public class FirebaseService extends FirebaseMessagingService {
    private static final String TAG = "FirebaseService";
    private static final int NEWS = 0;
    private static final int CHALLENGE = 1;
    private static final int BOOKING = 2;
    private static final int BEFORE_GAME = 3;
    private static final int SHOP = 4;

    //INTENT FOR BROADCAST ACTIONS
    private static final String CHALLENGE_ID = "challenge_id";
    private static final String BROADCAST_INTENT_ACCEPT = "accept_challenge_broadcast_action";
    private static final String BROADCAST_INTENT_CANCEL = "cancel_challenge_broadcast_action";

    //INTENT FOR GAME DETAILS << MAP >>
    private static final String INTENT_LAT = "lat";
    private static final String INTENT_LON = "LON";
    private static final String CORT_NAME = "name";
    private static final String SNIPPET = "snippet";

    //INTENT FOR CHALLENGE DETAILS (VIEW CHALLENGE - VIEW BOOKING)
    private static final String IS_CHALLENGE_BOOL = "is_challenge";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "onMessageReceived ");

        // Check if message contains a data payload.
//        if (remoteMessage.getData().size() > 0) {
//            Log.d(TAG, "Message data: " + remoteMessage.getData());
//            Log.d(TAG, "to: " + remoteMessage.getTo() + " , from: " + remoteMessage.getFrom());
//            sendNotification(remoteMessage.getData());
//        }
    }

    private void sendNotification(Map<String, String> data) {

        NotificationCompat.Builder notificationBuilder = null;
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Log.d(TAG,"notificationType: " + data.get("notificationType"));
        switch (Integer.parseInt(data.get("notificationType"))) {
            case NEWS:
                notificationBuilder = createGeneralNotification(data);
                break;
            case CHALLENGE:
                notificationBuilder = createNotificationChallenge(data);
                break;
            case BOOKING:
                notificationBuilder = createNotificationBooking(data);
                break;
            case BEFORE_GAME:
                notificationBuilder = createBeforeGameNotification(data);
                break;
            case SHOP:
                notificationBuilder = createShopNotification(data);
                break;
        }

        if(notificationBuilder != null) notificationManager.notify(Integer.parseInt(data.get("pushId")), notificationBuilder.build());
    }

    private NotificationCompat.Builder createGeneralNotification(Map<String, String> data) {
        Intent intent = new Intent(this, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(getColor(R.color.colorPrimary))
                .setContentTitle(data.get("title"))
                .setContentText(data.get("body"))
                .setSound(setSound())
                .setAutoCancel(true)
                .setStyle(new android.support.v7.app.NotificationCompat.BigTextStyle().bigText(data.get("body")))
                .setContentIntent(pendingIntent);
    }

    /** Pick the sound of the notification from raw folder and assign it in the Notification.Builder **/
    private Uri setSound(){
        Uri sound = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.pop);
        return sound;
    }

    /**
     * Actions For Buttons of notifications for challenge , Notification Type 1
     * @param data
     * @return Notification Builder
     */
    private NotificationCompat.Builder createNotificationChallenge(Map<String, String> data){

        PendingIntent actionAcceptPI;
        PendingIntent actionDenyPI;
        PendingIntent ViewChallengePI;

        Intent intentAccept = new Intent(this, NotificationActionReceiver.class);
        Intent intentDeny   = new Intent(this, NotificationActionReceiver.class);
        Intent intentView   = new Intent(this, ViewPushNotificationChallenge.class);

        intentView.putExtra(IS_CHALLENGE_BOOL,true);
        intentView.putExtra(CHALLENGE_ID,data.get("challengeId"));

        ViewChallengePI = PendingIntent.getActivity(this, 0 , intentView, PendingIntent.FLAG_ONE_SHOT);

        intentAccept.putExtra(BROADCAST_INTENT_ACCEPT,true);
        intentAccept.putExtra(CHALLENGE_ID,data.get("challengeId"));

        intentDeny.putExtra(BROADCAST_INTENT_ACCEPT,false);
        intentDeny.putExtra(CHALLENGE_ID,data.get("challengeId"));

        actionAcceptPI = PendingIntent.getBroadcast(this, 0 , intentAccept, PendingIntent.FLAG_UPDATE_CURRENT);
        actionDenyPI = PendingIntent.getBroadcast(this, 1 , intentDeny, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action accept =
                new NotificationCompat.Action.Builder(R.drawable.ic_accept_notification,getString(R.string.notification_challenge_accept),actionAcceptPI).build();

        NotificationCompat.Action deny =
                new NotificationCompat.Action.Builder(R.drawable.ic_deny_notification,getString(R.string.notification_challenge_deny),actionDenyPI).build();

        Bitmap imageBitmap = getCroppedBitmap(getBitmapFromURL(data.get("url")));

        NotificationCompat.Style style = new android.support.v7.app.NotificationCompat.BigTextStyle()
                .bigText(data.get("body"))
                .setBigContentTitle(data.get("title"))
                .setSummaryText(getString(R.string.notification_summary_challenge));

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(imageBitmap)
                .setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                .setContentTitle(data.get("title"))
                .setContentText(data.get("body"))
                .setSound(setSound())
                .setAutoCancel(true)
                .setStyle(style)
                .setContentIntent(ViewChallengePI)
                .addAction(deny)
                .addAction(accept);
    }

    /**
     * Create interactive push notification for booking , Action cancel Booking
     * @param data
     * @return Notification Builder
     */
    private NotificationCompat.Builder createNotificationBooking(Map<String, String> data){

        Intent intent = new Intent(this, ViewPushNotificationChallenge.class);
        intent.putExtra(IS_CHALLENGE_BOOL,false);
        intent.putExtra(CHALLENGE_ID,data.get("challengeId"));

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 , intent, PendingIntent.FLAG_ONE_SHOT);

        Bitmap imageBitmap = getCroppedBitmap(getBitmapFromURL(data.get("url")));

        Intent intentCancel = new Intent(this, NotificationActionReceiver.class);
        intentCancel.putExtra(BROADCAST_INTENT_CANCEL,true);
        intentCancel.putExtra(CHALLENGE_ID,data.get("challengeId"));

        PendingIntent actionCancelPI = PendingIntent.getBroadcast(this, 2 , intentCancel, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action cancel =
                new NotificationCompat.Action.Builder(R.drawable.ic_deny_notification,getString(R.string.notification_booking_cancel),actionCancelPI).build();


        NotificationCompat.Style style = new android.support.v7.app.NotificationCompat.BigTextStyle()
                .bigText(data.get("body"))
                .setBigContentTitle(data.get("title"))
                .setSummaryText(getString(R.string.notification_summary_booking));

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(imageBitmap)
                .setColor(getColor(R.color.colorPrimary))
                .setContentTitle(data.get("title"))
                .setContentText(data.get("body"))
                .setSound(setSound())
                .setAutoCancel(true)
                .setStyle(style)
                .setContentIntent(pendingIntent)
                .addAction(cancel);
    }

    /**
     * create interactive push notification before Game , Action Map
     */
    private NotificationCompat.Builder createBeforeGameNotification(Map<String, String> data){

        Intent intentMap = new Intent(this, GamePushNotificationMap.class);
        intentMap.putExtra(INTENT_LAT,data.get("gpslat"));
        intentMap.putExtra(INTENT_LON,data.get("gpslon"));
        intentMap.putExtra(CORT_NAME,data.get("court"));
        intentMap.putExtra(SNIPPET,data.get("body"));


        Bitmap imageBitmap = getCroppedBitmap(getBitmapFromURL(data.get("url")));

        PendingIntent mapPI = PendingIntent.getActivity(this, 1 , intentMap, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Action map =
                new NotificationCompat.Action.Builder(R.drawable.ic_map_game,getString(R.string.notification_action_map),mapPI).build();

        NotificationCompat.Style style = new android.support.v7.app.NotificationCompat.BigTextStyle()
                .bigText(data.get("body"))
                .setBigContentTitle(data.get("title"))
                .setSummaryText(getString(R.string.notification_summary_game));

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setLargeIcon(imageBitmap)
                .setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                .setContentTitle(data.get("title"))
                .setContentText(data.get("body"))
                .setSound(setSound())
                .setAutoCancel(true)
                .setContentIntent(mapPI)
                .setStyle(style)
                .addAction(map);
    }

    /**
     * create interactive push notification before Game , Action Map
     */
    private NotificationCompat.Builder createShopNotification(Map<String, String> data){

        Intent intentMap = new Intent(this, EshopWebView.class);

        PendingIntent mapPI = PendingIntent.getActivity(this, 1 , intentMap, PendingIntent.FLAG_ONE_SHOT);

        Bitmap imageBitmap = getBitmapFromURL(data.get("url"));

        return new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                .setContentTitle(data.get("title"))
                .setContentText(data.get("body"))
                .setSound(setSound())
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(imageBitmap))
                .setContentIntent(mapPI);
    }

    /**
     * Download Image from the server
     * @param src
     * @return
     */
    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.no_photo);
        }
    }

    /**
     * Crop image to be circle and scale
     * @param bitmap
     * @return
     */
    public Bitmap getCroppedBitmap(Bitmap bitmap) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(bitmap.getWidth() / 2, bitmap.getHeight() / 2,
                bitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        Bitmap _bmp = Bitmap.createScaledBitmap(output, 200, 200, false);
        return _bmp;

    }

}
