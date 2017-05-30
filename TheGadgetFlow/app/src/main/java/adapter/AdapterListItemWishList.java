package Adapter;


import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.CloudieNetwork.GadgetFlow.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.example.vasilis.TheGadgetFlow.LogInActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import Helper.Constants;
import Helper.GD;
import Helper.ScalingUtilities;
import Model.Model_Gadget.GadgetItem;
import Utils.CommonUtils;

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
public class AdapterListItemWishList extends RecyclerView.Adapter<AdapterListItemWishList.MainHolder>  {
    private String cookie;
    ArrayList<GadgetItem> mListItem;
    Fragment fragmentActivity;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    public static OnItemClickListener listener;// Define listener member variable
    private Context c;
    private int cellwidth;
    private int cellHeight;
    private okhttp3.OkHttpClient client;
    Resources res;
    Drawable drawableSave,drawableUnSaved;
    GD gd = GD.get();
    private final static String TAG = "AdapterListItem";
    private String itemID;
    private ProgressDialog waitingDialog;

    public class MainHolder extends RecyclerView.ViewHolder {

        public ProgressBar pgLoading;
        public TextView tv_title;
        public ImageView imgView;
        public TextView tv_price;
        public TextView tv_save;
        public RelativeLayout rl_save;
        public ImageView saveBTN;


        /** **/
        public MainHolder(final View itemView) {
            super(itemView);
            // Find the TextView in the LinearLayout
            tv_title = ((TextView) itemView.findViewById(R.id.title));
            pgLoading = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            imgView = (ImageView) itemView.findViewById(R.id.imgItem);
            tv_price = (TextView) itemView.findViewById(R.id.price);
            rl_save = (RelativeLayout)itemView.findViewById(R.id.btn_save_feed);
            saveBTN = (ImageView)itemView.findViewById(R.id.imageViewSave);
            tv_save = (TextView) itemView.findViewById(R.id.tv_save);

            tv_title.setTypeface(Typeface.createFromAsset(c.getAssets(), "fonts/OpenSans-Semibold.ttf"));
            tv_price.setTypeface(Typeface.createFromAsset(c.getAssets(), "fonts/OpenSans-Semibold.ttf"));
            tv_save.setTypeface(Typeface.createFromAsset(c.getAssets(), "fonts/OpenSans-Semibold.ttf"));

            imgView.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if (listener != null)
                        listener.onItemClick(imgView, getAdapterPosition());
                }
            });
        }

    }

    public AdapterListItemWishList(Context c, Fragment fragmentActivity, ArrayList<GadgetItem> pListGadget,ProgressDialog waitingDialog) {

        this.fragmentActivity = fragmentActivity;
        this.mListItem = pListGadget;
        this.waitingDialog = waitingDialog;
        this.c = c;
        this.pref = c.getSharedPreferences("gadgetflow", 0);// 0 - for private mode
        editor = pref.edit();
        this.cookie = pref.getString(Constants.COOKIE, null);
        client = new okhttp3.OkHttpClient();
        res = c.getResources();
        drawableSave = res.getDrawable(R.drawable.anim_save);
        drawableUnSaved = res.getDrawable(R.drawable.save_feed);
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public MainHolder onCreateViewHolder(ViewGroup viewGroup, int ViewType){

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_item_wishlist, viewGroup, false);
        MainHolder cvh0 = new MainHolder(v);
        return cvh0;
    }

    @Override
    public void onBindViewHolder(final MainHolder cvh, final int position) {

        final GadgetItem item = mListItem.get(position);
        item.gadget_price = CommonUtils.getPrice(item.encoded);
        cvh.tv_title.setText(item.gadget_title);
        cellwidth = Integer.parseInt(CommonUtils.getWidth(item.encoded));
        cellHeight = Integer.parseInt(CommonUtils.getHeight(item.encoded));
        final String[] part = item.guid.split("=");
        String id = null;
        if(part.length == 1){
            id = part[0];
        } else if(part.length == 2){
            id = part[1];
        }
        item.id = id;

        if(cookie != null ) {
            if(id !=null && gd.items != null) {
                if (gd.items.contains(id)) {
                    item.isSaved = true;
                } else {
                    item.isSaved = false;
                }
            }

            if(item.isSaved){
              cvh.saveBTN.setBackgroundDrawable(drawableSave);
              cvh.tv_save.setText("SAVED");
          }else{
              cvh.saveBTN.setBackgroundDrawable(drawableUnSaved);
              cvh.tv_save.setText("SAVE");
          }
        } else{
            cvh.saveBTN.setBackgroundDrawable(drawableUnSaved);
            cvh.tv_save.setText("SAVE");
        }

        cvh.rl_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cookie != null){
                    //Save or Unsave
                    if(item.isSaved){
                        if(CommonUtils.isNetworkAvailable(c)){
                            cvh.saveBTN.setBackgroundDrawable(drawableUnSaved);
                            cvh.tv_save.setText("SAVE");
                            item.isSaved = false;
                            new addRemoveProduct().execute(Constants.REMOVE_ITEM_FROM_WISH_LIST.replace("@itemid@",item.id).replace("@usercok@",cookie));

                        } else {
                            //No internet
                            Toast.makeText(c,"No internet! Please check your network", Toast.LENGTH_LONG).show();
                        }

                    } else {

                        if(CommonUtils.isNetworkAvailable(c) ){
                            cvh.saveBTN.setBackgroundDrawable(drawableSave);
                            cvh.tv_save.setText("SAVED");
                            item.isSaved = true;
                            itemID = item.id;
                            new addRemoveProduct().execute(Constants.ADD_ITEM_TO_WISH_LIST.replace("@itemid@",item.id).replace("@usercok@",cookie));

                        } else{
                            //No internet
                            Toast.makeText(c,"No internet! Please check your network", Toast.LENGTH_LONG).show();
                        }
                    }

                }else {
                    Intent intent = new Intent(c, LogInActivity.class);
                    c.startActivity(intent);
                }
            }
        });

        cvh.pgLoading.setVisibility(View.VISIBLE);
        cvh.imgView.setImageDrawable(null);
        cvh.tv_price.setText(item.gadget_price);

        //Listener
        item.gadget_image = CommonUtils.getImage(item.encoded);
        Glide.with(fragmentActivity).load(item.gadget_image)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .override(cellwidth,cellHeight)
                .centerCrop()
                .fitCenter()
                .into((new GlideDrawableImageViewTarget(cvh.imgView) {
                    @Override
                    public void onResourceReady(GlideDrawable drawable, GlideAnimation anim) {
                        super.onResourceReady(drawable, anim);
                        cvh.pgLoading.setVisibility(View.GONE);
                    }
                }));

    }
    // Return the size of  dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {

        return mListItem.size();
    }

    /***************************************************************************** Methods    **************************************************************************************/
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }


    private String Request(String url) throws IOException {

        okhttp3.Request request = new okhttp3.Request.Builder().url(url).build();
        okhttp3.Response response = client.newCall(request).execute();
        return response.body().string();
    }


    /***************************************************************************** Interfaces **************************************************************************************/
    public interface OnItemClickListener {
        void onItemClick(View itemView, int position);
    }


    /***************************************************************************** Web Services **************************************************************************************/
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
        protected void onPostExecute(JSONObject result) {
            try {
//                Log.d(TAG,result.toString());
                if(result.getString("status").equals("error")){

                } else if(result.getString("status").equals("OK")) {
                    //Log.d("billy",result.toString());
                    //change the button if the product already exist in the wish list
                    new requestWishList().execute(Constants.GET_WISH_LIST.concat(cookie));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                hideDialog();

            } catch(Exception e) {
                e.printStackTrace();
                hideDialog();
            }
        }
    }
    /************************************************************** GET WISH LIST JSON - ON POST EXECUTE REMOVE ITEM ***********************************************************************/
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

                } else if(result.getString("status").equals("OK")) {

                }
            } catch (JSONException e) {
                e.printStackTrace();

            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void showDialog(Activity ctx, String message, boolean cancelable) {
        if (waitingDialog != null) {
            hideDialog();
            waitingDialog = null;
        }
        if(ctx != null) {/** vf : craslytics 224**/
            waitingDialog = new ProgressDialog(ctx);
            waitingDialog.setMessage(message);
            waitingDialog.setCancelable(cancelable);
            try {
                waitingDialog.show();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }


    public void hideDialog() {
        if (waitingDialog != null && waitingDialog.isShowing())
            waitingDialog.hide();
    }

}

