package WebServices;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import Helper.Constants;

/**
 * Created by Vasilis Fouroulis on 07/02/2017. This post request is a black box , can be used from many web services
 */

public class GenericPostRequest extends AsyncTask<String,Void,Object> {

    private int responseCode;
    private static final String TAG = GenericPostRequest.class.getSimpleName();
    private Delegate listener;

    public GenericPostRequest(Delegate listener) {
        this.listener = listener;
    }

    @Override
    public Object doInBackground(String[] params) {

        URL url;
        String response = "";
        String endPoint = params[0];
        String body = params[1];

        try {
            url = new URL(Constants.SERVER_URL + endPoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(15000);
            conn.setConnectTimeout(15000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "" + "application/json");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            Log.d(TAG,endPoint+ " BODY: " + body);
            OutputStream outputStream = new BufferedOutputStream(conn.getOutputStream());
            outputStream.write(body.getBytes());
            outputStream.flush();

            responseCode = conn.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                String line;
                BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((line=br.readLine()) != null) {
                    response+=line;
                }
                Log.d(TAG, ""+ response);

            } else {
                response="";
                Log.d(TAG, "response code: " + responseCode);
                listener.onErrorOccurred(""+responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.onErrorOccurred("Exception");
        }

        return response+"";
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        listener.onPreExecute();
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        listener.onTaskCompleted(o);
    }

    public interface Delegate {
        void onTaskCompleted(Object result);
        void onPreExecute();
        void onErrorOccurred(String responseCode);
    }
}
