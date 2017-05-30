package Utils;

import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.example.vasilis.TheGadgetFlow.ActivityDetails;
import com.example.vasilis.TheGadgetFlow.ActivityDetailsPushNotification;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.intercom.android.sdk.Intercom;
import io.intercom.android.sdk.identity.Registration;


public class CommonUtils {

	private static final String TAG = "CommonUtils";
//	public static void showDialog(ProgressDialog waitingDialog,Activity ctx, String message, boolean cancelable) {
//		if (waitingDialog != null) {
//			hideDialog(waitingDialog);
//			waitingDialog = null;
//		}
//		if(ctx != null) {/** vf : craslytics 224**/
//			waitingDialog = new ProgressDialog(ctx);
//			waitingDialog.setMessage(message);
//			waitingDialog.setCancelable(cancelable);
//			try {
//				waitingDialog.show();
//			} catch (Exception e){
//				e.printStackTrace();
//			}
//		}
//	}
//
//
//	public static void hideDialog(ProgressDialog waitingDialog) {
//		if (waitingDialog != null && waitingDialog.isShowing())
//			waitingDialog.hide();
//	}
/**
	// Returns the URI path to the Bitmap displayed in specified ImageView
	public static Uri getLocalBitmapUri(ImageView imageView) {
	    // Extract Bitmap from ImageView drawable
	    Drawable drawable = imageView.getDrawable();
	    Bitmap bmp = null;
	    if (drawable instanceof BitmapDrawable){
	       bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
	    } else {
	       return null;
	    }
	    // Store image to default external storage directory
	    Uri bmpUri = null;
	    try {
	        File file =  new File(Environment.getExternalStoragePublicDirectory(  
	            Environment.DIRECTORY_DOWNLOADS), "share_image_" + System.currentTimeMillis() + ".png");
	        file.getParentFile().mkdirs();
	        
	        FileOutputStream out = new FileOutputStream(file);
	        bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
	        out.close();
	        bmpUri = Uri.fromFile(file);
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	    return bmpUri;
	}*/
/**
	public static String getCategories(List<String> categories)
	{
		String result = "";
		for(String name : categories)
		{
			
			result += name.replace("Gift Ideas Starting at ", "") + "\n";
		}
		return result;
	}
	*/
	public static String getPubDate(String strDate) {
		if(strDate == null || strDate == "")
			return "";
		
		SimpleDateFormat  format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z",Locale.ENGLISH);
		try {  
		    Date date = format.parse(strDate);
//			Log.d("new","date: " + date);
		    if(date == null)
		    	return "";
		    format.applyPattern("dd/MM/yyyy");
		    return format.format(date);
		} catch (ParseException e) {  
		    // TODO Auto-generated catch block  
		    e.printStackTrace();
			return null;
		}
		

	}

	public static boolean isNetworkAvailable(Context ctx){
		try {
			ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(
			        Context.CONNECTIVITY_SERVICE);

			    NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			    if (wifiNetwork != null && wifiNetwork.isConnected()) {
					//Log.d(TAG,"CommonUtils , cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI)");
			      return true;
			    }

			    NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			    if (mobileNetwork != null && mobileNetwork.isConnected()) {
					//Log.d(TAG,"CommonUtils, cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)");
			      return true;
			    }

			    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			    if (activeNetwork != null && activeNetwork.isConnected()) {
					//Log.d(TAG,"CommonUtils, cm.getActiveNetworkInfo()");

					return true;
			    }
			//Log.d(TAG,"CommonUtils, no internet");
			    return false;
		} catch (Exception e) {
//			Log.e(TAG,e.toString());
			return false;
		}

	}
	public static Bitmap resizeAndCrop(Bitmap srcBitmap, Point resizeToSize, Rect cropWithSize){
		Bitmap resized = null;
		try
		{
			resized = Bitmap.createScaledBitmap(srcBitmap, resizeToSize.x, resizeToSize.y, true);
//			return resized;
			Bitmap resultBm = Bitmap.createBitmap(resized, cropWithSize.top, cropWithSize.left, cropWithSize.bottom, cropWithSize.right);
			return resultBm;
		}
		catch(Exception ex){
			
		}
		return resized;
	}
		public static Rect getImageSize(Point fromSize, Point expectedSize, boolean isSearch){
			Rect rect = new Rect();
		    //    CGSize expectedSize = CGSizeMake(768, 500);
		    int diffWidth = expectedSize.x - fromSize.x;
		    int diffHeight = expectedSize.y - fromSize.y;

		    if(diffWidth > 0) {
		        if(diffHeight > 0) {
		            if(diffWidth > diffHeight) {
		                rect.bottom = fromSize.x + diffWidth;
		                rect.right = fromSize.y  + diffWidth;
		                rect.top = 0;
		                rect.left = (int) ((float)(diffWidth - diffHeight)/2);
//		                rect.size.width = fromSize.width + diffWidth;
//		                rect.size.height = fromSize.height  + diffWidth;
//		                rect.origin.x = 0;
//		                rect.origin.y = (float)(diffWidth - diffHeight)/2;
		            } else if(diffWidth < diffHeight) {
		            	 rect.bottom = fromSize.x + diffHeight;
			                rect.right = fromSize.y  + diffHeight;
			                rect.top = (int)(diffHeight - diffWidth)/2;
			                rect.left = 0;
//		                rect.size.width = fromSize.width + diffHeight;
//		                rect.size.height = fromSize.height  + diffHeight;
//		                rect.origin.x = (float)(diffHeight - diffWidth)/2;
//		                rect.origin.y = 0;
		            } else {
		            	rect.bottom = fromSize.x + diffWidth;
		                rect.right = fromSize.y  + diffWidth;
		                rect.top = 0;
		                rect.left = 0;
//		                rect.size.width = fromSize.width + diffWidth;
//		                rect.size.height = fromSize.height  + diffWidth;
//		                rect.origin.x = 0;
//		                rect.origin.y = 0;
		            }
		        } else if(diffHeight < 0) {
		            rect.bottom = fromSize.x + diffWidth;
		            rect.right = fromSize.y  + diffWidth;
		            rect.top = 0;
		            rect.left = (int)(diffWidth + Math.abs(diffHeight)) / 2;
//		            rect.size.width = fromSize.width + diffWidth;
//		            rect.size.height = fromSize.height  + diffWidth;
//		            rect.origin.x = 0;
//		            rect.origin.y = (float)(diffWidth + fabsf(diffHeight)) / 2;
		        } else {
		        	rect.bottom = fromSize.x + diffWidth;
		            rect.right = fromSize.y  + diffWidth;
		            rect.top = 0;
		            rect.left = (int)(diffWidth / 2);
//		            rect.size.width = fromSize.width + diffWidth;
//		            rect.size.height = fromSize.height  + diffWidth;
//		            rect.origin.x = 0;
//		            rect.origin.y = (float)(diffWidth / 2);
		        }
		    } else if(diffWidth < 0) {
		        if(diffHeight > 0) {
		            if(Math.abs(diffWidth) > diffHeight) {
		            	rect.bottom = fromSize.x + diffHeight;
		                rect.right = fromSize.y  + diffHeight;
		                rect.top = (int)(Math.abs(diffWidth) - diffHeight) / 2;
		                rect.left = 0;
//		                rect.size.width = fromSize.width + diffHeight;
//		                rect.size.height = fromSize.height  + diffHeight;
//		                rect.origin.x = (float)(fabsf(diffWidth) - diffHeight) / 2;
//		                rect.origin.y = 0;
		            } else if(Math.abs(diffWidth) < diffHeight) {
		            	rect.bottom = fromSize.x + diffHeight;
		                rect.right = fromSize.y  + diffHeight;
		                rect.top = (int)(diffHeight + Math.abs(diffWidth))/2;
		                rect.left = 0;
//		                rect.size.width = fromSize.width + diffHeight;
//		                rect.size.height = fromSize.height  + diffHeight;
//		                rect.origin.x = (float)(diffHeight + fabsf(diffWidth))/2;
//		                rect.origin.y = 0;
		            } else {
		            	rect.bottom = fromSize.x + diffHeight;
		                rect.right = fromSize.y  + diffHeight;
		                rect.top = (int)(diffHeight + Math.abs(diffWidth))/2;
		                rect.left = 0;
//		                rect.size.width = fromSize.width + diffHeight;
//		                rect.size.height = fromSize.height  + diffHeight;
//		                rect.origin.x = (float)(diffHeight + fabsf(diffWidth))/2;
//		                rect.origin.y = 0;
		            }
		        } else if(diffHeight < 0) {
		            if(Math.abs(diffWidth) > Math.abs(diffHeight)) {
		            	rect.bottom = fromSize.x - Math.abs(diffHeight);
		                rect.right = fromSize.y  - Math.abs(diffHeight);
		                rect.top = (int)(Math.abs(diffWidth) - Math.abs(diffHeight)) / 2;
		                rect.left = 0;
//		                rect.size.width = fromSize.width - fabsf(diffHeight);
//		                rect.size.height = fromSize.height  - fabsf(diffHeight);
//		                rect.origin.x = (float)(fabsf(diffWidth) - fabsf(diffHeight)) / 2;
//		                rect.origin.y = 0;
		            } else if(Math.abs(diffWidth) < Math.abs(diffHeight)) {
		            	rect.bottom = fromSize.x - Math.abs(diffWidth);
		                rect.right = fromSize.y  - Math.abs(diffWidth);
		                rect.top = 0;
		                rect.left = (int)(Math.abs(diffHeight) - Math.abs(diffWidth))/2;
//		                rect.size.width = fromSize.width - fabsf(diffWidth);
//		                rect.size.height = fromSize.height  - fabsf(diffWidth);
//		                rect.origin.x = 0;
//		                rect.origin.y = (float)(fabsf(diffHeight) - fabsf(diffWidth))/2;
		            } else {
		            	rect.bottom = fromSize.x - Math.abs(diffHeight);
		                rect.right = fromSize.y  - Math.abs(diffHeight);
		                rect.top = 0;
		                rect.left = 0;
//		                rect.size.width = fromSize.width - fabsf(diffHeight);
//		                rect.size.height = fromSize.height  - fabsf(diffHeight);
//		                rect.origin.x = 0;
//		                rect.origin.y = 0;
		            }
		        } else {
		        	rect.bottom = expectedSize.x;
		            rect.right = fromSize.y;
		            rect.top = Math.abs(diffWidth) /2 ;
		            rect.left = 0;
//		            rect.size.width = expectedSize.width;
//		            rect.size.height = fromSize.height;
//		            rect.origin.x = fabsf(diffWidth) /2 ;
//		            rect.origin.y = 0;
		        }
		    } else {
		        if(diffHeight > 0) {
		        	rect.bottom = fromSize.x + diffHeight;
		            rect.right = fromSize.y  + diffHeight;
		            rect.top = (int)(diffHeight - diffWidth)/2;
		            rect.left = 0;
//		            rect.size.width = fromSize.width + diffHeight;
//		            rect.size.height = fromSize.height  + diffHeight;
//		            rect.origin.x = (float)(diffHeight - diffWidth)/2;
//		            rect.origin.y = 0;
		        } else if(diffHeight < 0) {
		        	rect.bottom = fromSize.x;
		            rect.right = expectedSize.y;
		            rect.top = 0;
		            rect.left = (int)(Math.abs(diffHeight)) / 2;
//		            rect.size.width = fromSize.width;
//		            rect.size.height = expectedSize.height;
//		            rect.origin.x = 0;
//		            rect.origin.y = (float)(fabsf(diffHeight)) / 2;
		        } else {
		        	rect.bottom = expectedSize.x;
		        	rect.right = expectedSize.y;
		            rect.top = 0;
		            rect.left = 0;
//		            rect.size = expectedSize;
//		            rect.origin.x = 0;
//		            rect.origin.y = 0;
		        }
		    }
		    if(isSearch){
		    	float ratio = (float)(float)expectedSize.y/(float)fromSize.y;
		    	rect.bottom = (int)(ratio * (float)fromSize.x);
		    
		    }
		    return rect;
		}

	public static String getDescription(String content) {
		org.jsoup.nodes.Document doc = Jsoup.parse(content);
		Elements e = doc.select("p");
		return e.text();
	}

	/**
	 * Parsing the CDATA
	 */
	public static ArrayList<String> getImages(String description) {
		ArrayList<String> image = new ArrayList<>();

		Pattern titleFinder = Pattern.compile(
				"<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher = titleFinder.matcher(description);
		while (regexMatcher.find()) {

			image.add(regexMatcher.group(1));

		}

		return image;
	}

	public static String getImage(String description) {
		String image = null;
		Pattern titleFinder = Pattern.compile(
				"<img[^>]+src\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher = titleFinder.matcher(description);
		while (regexMatcher.find()) {
			image = regexMatcher.group(1);
		}

		return image;
	}
	public static String getPrice(String content) {
		String result = "$";
		org.jsoup.nodes.Document doc = Jsoup.parse(content);
		Elements e = doc.getElementsByClass("gf-price");
		if (e == null)
			return null;
		return result + e.text();
	}
	/** VF : Get width of the image **/
	public static String getWidth(String content) {
		String width = null;
		Pattern titleFinder = Pattern.compile(
				"<img[^>]+width\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher = titleFinder.matcher(content);
		while (regexMatcher.find()) {
			width = regexMatcher.group(1);
		}

		return width;
	}

	/** VF : Get height of the image **/
	public static String getHeight(String content) {
		String width = null;
		Pattern titleFinder = Pattern.compile(
				"<img[^>]+height\\s*=\\s*['\"]([^'\"]+)['\"][^>]*>",
				Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
		Matcher regexMatcher = titleFinder.matcher(content);
		while (regexMatcher.find()) {
			width = regexMatcher.group(1);
		}

		return width;
	}

	/**-------------------------------------------------------------------------------
	 * Method that calculate the Day of the week , EXAMPLE : 15/03/2016 Thursday
	 * @param itemDate from the RSS
	 * @return The day of the Week
	 *--------------------------------------------------------------------------------*/
	public static String calculateDate(String itemDate) throws ParseException {

		String LogTag = "dateTag";
//		Log.d(LogTag,"itemDate: "+itemDate);
		SimpleDateFormat formater = new SimpleDateFormat("dd/MM/yyyy");
		//Today
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 0);
		Date today = calendar.getTime();
		String TodayString = formater.format(today);
		//Yesterday
		calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, -1);
		Date Yesterday = calendar.getTime();
		String YesterdayString = formater.format(Yesterday);

		//Get day of the week
		Date theDate = formater.parse(itemDate);
		String dayOfWeek = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(theDate);

//		Log.d(LogTag,"Today: "+TodayString);
//		Log.d(LogTag,"Yesterday: "+YesterdayString);
//		Log.d(LogTag,"Day of the week: "+dayOfWeek);

		if(TodayString.equals(itemDate))
			return "TODAY";
		else if(YesterdayString.equals(itemDate))
			return "YESTERDAY";
		else
			return dayOfWeek.toUpperCase();
	}
	/**
	 * Title text of the product in one line
	 */
	public static String fixTitleText(int size , String itemTitle){
		boolean isCutted = false;
		String newTitle="";
		char c;

		if(itemTitle.length() > size) {
			isCutted = true;
			for (int i = 0; i < size; i++) {
				c = itemTitle.charAt(i);

				newTitle = newTitle + c;

			}
		} else
			newTitle = itemTitle;
		//Log.d("billy", newTitle);
		//Log.d("billy", itemTitle);
		if(isCutted)
			newTitle = newTitle + " ...";

		return newTitle;
	}

	/**
	 * cutting the price
	 */
	private static String cutPrice(String itemTitle){

		String newTitle="";
		for(char a : itemTitle.toCharArray()){
			if (a !='$'){
				newTitle = newTitle + a;
			} else
				break;
		}
		return newTitle;
	}

	public static String getBuyLink(String content) {
		org.jsoup.nodes.Document doc = Jsoup.parse(content);
		Elements e = doc.getElementsByClass("buybut");
		return e.attr("href");
	}

	/**
	 * Getting the Density of the phone
	 * @return
	 */
//	public static int getDisplayMetrics(){
//		DisplayMetrics metrics = context.getResources().getDisplayMetrics();
//		//Log.d("billy",String.valueOf(metrics.densityDpi));
//		return metrics.densityDpi;
//	}
	/**
	 * hide soft Keyboard
	 * @param pView
	 * @param pActivity

	public static void hideKeyboard(View pView, Activity pActivity) {
		if (pView == null) {
			pView = pActivity.getWindow().getCurrentFocus();
		}
		if (pView != null) {
			InputMethodManager imm = (InputMethodManager) pActivity
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			if (imm != null) {
				imm.hideSoftInputFromWindow(pView.getWindowToken(), 0);
			}
		}
	}
	*/


	/** Intercom Registration **/
	public static void successfulLogin(String userID,Context context) {

		Registration registration = Registration.create().withUserId(userID);
		Intercom.client().registerIdentifiedUser(registration);
		Intercom.client().handlePushMessage();
		Intercom.client().handlePushMessage(showPushMessage(context));

	}

	/** Intercom Custom TaskBuilder for push notifications **/
	private static TaskStackBuilder showPushMessage(Context context){

		Intent intent = new Intent (context, ActivityDetails.class);
		intent.putExtra("EmailId","you can Pass emailId here");
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
		stackBuilder.addParentStack(ActivityDetailsPushNotification.class);
		stackBuilder.addNextIntent(intent);

		return stackBuilder;
	}
}
