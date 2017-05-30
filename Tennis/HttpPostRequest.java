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
import CommonUtils.Constants;

/**
 * Created by Vasilis Fouroulis on 14/02/2017. Love
 * ASYNC TASK CLASS THAT SUPPORTS POST REQUESTS
 */

public class HttpPostRequest extends AsyncTask<String,Integer,Object> {

    private final String TAG = getClass().getSimpleName();
    private String endPoint;
    private int responseCode = 0;
    private MyAsyncTaskListener mListener;

    public HttpPostRequest(MyAsyncTaskListener listener){
        this.mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.onPreExecutelistener();
    }

    @Override
    protected Object doInBackground(String... params) {
        URL url;
        String response = "";

        try {
            endPoint = params[0];
            url = new URL(Constants.SERVER_URL + endPoint);
            Log.d(TAG,"****************************************************************************************************************************************************************************************");
            Log.d(TAG,"requestURL: " + Constants.SERVER_URL + endPoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setReadTimeout(15000);
            connection.setConnectTimeout(15000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "" + "application/json");
            connection.setDoInput(true);
            connection.setDoOutput(true);

            String body = params[1];
            Log.d(TAG,endPoint+ " BODY: " + body);
            OutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            outputStream.write(body.getBytes());
            outputStream.flush();

            responseCode = connection.getResponseCode();

            if(responseCode == HttpsURLConnection.HTTP_OK){
                String line;
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = bufferedReader.readLine()) != null){response+=line;}
                Log.d(TAG,endPoint + " RESPONSE: " + response);
            } else {
                response = "";
                Log.w(TAG,endPoint + " ERROR : " + responseCode);
            }
        } catch (Exception e ){
            e.printStackTrace();
        }

        return response;
    }

    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        mListener.onPostExecutelistener(o.toString(),responseCode);
    }

    public interface MyAsyncTaskListener {
        void onPreExecutelistener();
        void onPostExecutelistener(String result, int responseCode);
    }

}
