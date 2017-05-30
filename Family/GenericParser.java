package JSONParsers;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Vasilis Fouroulis on 07/02/2017. Generic Parser , Client can Implement the interface for a custom parsing
 * else he must implement the abstract methods . This method can only Parse res and Response as String . Client can decide
 * this from the constructor.
 */

public abstract class GenericParser {

    private static final String TAG = "GenericParser";
    private boolean isCustomParser;
    private static OnCustomParseListener listener;

    public GenericParser(boolean isCustomParser){
        this.isCustomParser = isCustomParser;
    }

    public void processResult(Object result){
        boolean res;
        String response;

        try {

            JSONObject json = new JSONObject(result.toString());
            Log.d(TAG, "json: " + json.toString());

            if(isCustomParser){
                //implement interface
                if(listener!=null){
                    listener.onParseJSON(json);
                } else {
                    onErrorParsing("Something Went wrong"); //this should never happens
                }
            } else {
                //implement abstract classes
                res = json.getBoolean("res");

                if (res) {
                    response = json.getString("response");
                    onParseCompleted(response);

                } else {
                    response = json.getString("response");
                    onErrorMessage(response);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
            onErrorParsing("JSONException");
        }
    }


    public abstract void onErrorParsing(String response);

    public abstract void onErrorMessage(String response);

    public abstract void onParseCompleted(String response);

    public static void setOnCustomParseListener(OnCustomParseListener l){
        listener = l;
    }

    public interface OnCustomParseListener{
        void onParseJSON(JSONObject json) throws JSONException;
    }
}
