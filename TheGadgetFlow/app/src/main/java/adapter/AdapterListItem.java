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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import Helper.Constants;
import Helper.GD;
import Helper.MemoryCache;
import Helper.ScalingUtilities;
import Model.Model_Gadget.GadgetItem;
import Model.CategoryItem;
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
public class AdapterListItem extends RecyclerView.Adapter<AdapterListItem.MainHolder>  {
    private String cookie;
    ArrayList<GadgetItem> mListItem;
    ArrayList<CategoryItem> mListCategories;
    Fragment fragmentActivity;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    public static OnItemClickListener listener;// Define listener member variable
    public OnDataChangeListener mOnDataChangeListener;
    private Context context;
    private int cellwidth;
    private int cellHeight;
    private okhttp3.OkHttpClient client;
    private MemoryCache mc = MemoryCache.get();
    Resources res;
    Drawable drawableSave,drawableUnSaved;
    GD gd = GD.get();
    private final static String TAG = "AdapterListItem";
    private HashMap<String,String> mList;
    private ProgressDialog waitingDialog;

    public class MainHolder extends RecyclerView.ViewHolder {

        public ProgressBar pgLoading;
        public TextView tv_title;
        public ImageView imgView;
        public TextView tv_date;
        public TextView tv_price;
        public TextView tv_category;
        public TextView tv_save;
        public LinearLayout rl_save;
        public RelativeLayout rl_share;
        public ImageView saveBTN;
        public TextView tv_share;

        /** **/
        public MainHolder(final View itemView) {
            super(itemView);
            // Find the TextView in the LinearLayout
            tv_title = ((TextView) itemView.findViewById(R.id.title));
            tv_date = ((TextView) itemView.findViewById(R.id.tv_date));
            pgLoading = (ProgressBar) itemView.findViewById(R.id.progress_bar);
            imgView = (ImageView) itemView.findViewById(R.id.imgItem);
            tv_price = (TextView) itemView.findViewById(R.id.tv_gadget_price);
            rl_save = (LinearLayout)itemView.findViewById(R.id.btn_save_feed);
            rl_share = (RelativeLayout)itemView.findViewById(R.id.btn_share_feed);
            saveBTN = (ImageView)itemView.findViewById(R.id.imageViewSave);
            tv_save = (TextView) itemView.findViewById(R.id.tv_save);
            tv_category = (TextView)itemView.findViewById(R.id.tv_gadget_category);
            tv_share = (TextView)itemView.findViewById(R.id.tv_share);

            tv_category.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Regular.ttf"));
            tv_title.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
            tv_price.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
            tv_save.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));
            tv_share.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/OpenSans-Semibold.ttf"));

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

    public AdapterListItem(Context c, Fragment fragmentActivity, ArrayList<GadgetItem> pListGadget, ProgressDialog waitingDialog) {

        this.fragmentActivity = fragmentActivity;
        this.mListItem = pListGadget;
        this.context = c;
        this.waitingDialog = waitingDialog;

        pref = context.getSharedPreferences("gadgetflow", 0);// 0 - for private mode
        editor = pref.edit();
        this.cookie = pref.getString(Constants.COOKIE, null);

        client = new okhttp3.OkHttpClient();
        res = context.getResources();
        drawableSave = res.getDrawable(R.drawable.anim_save);
        drawableUnSaved = res.getDrawable(R.drawable.save_feed);
        mList = new HashMap<>();
        try {
            this.mListCategories = gd.getCategories().getCategories();
            for (CategoryItem NavItem : mListCategories) {
                mList.put(NavItem.getId(),NavItem.getTitle());
            }
        } catch ( NullPointerException e){
            /** vf: Crashlytics 225 **/
            e.printStackTrace();
        }
    }

    /**
     * Replace the contents of a view (invoked by the layout manager)
     */
    @Override
    public MainHolder onCreateViewHolder(ViewGroup viewGroup, int ViewType){

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_item, viewGroup, false);
        MainHolder cvh0 = new MainHolder(v);
        return cvh0;
    }

    @Override
    public void onBindViewHolder(final MainHolder cvh, int position) {

        final GadgetItem item = mListItem.get(position);
        item.gadget_price = CommonUtils.getPrice(item.encoded);
        cellwidth = Integer.parseInt(CommonUtils.getWidth(item.encoded));
        cellHeight = Integer.parseInt(CommonUtils.getHeight(item.encoded));
        cvh.tv_title.setText(item.gadget_title);
        String[] part = item.guid.split("=");
        String id = null, categoryName = null;

        if(item.catList != null ) {

            try {
                if (mList != null && mList.containsKey(item.catList.get(0).numberId)) {
                    categoryName = mList.get(item.catList.get(0).numberId);
                } else {
                    categoryName = "";
                }
            } catch (Exception e){
                e.printStackTrace();
                categoryName = "";
            }
        } else {
            categoryName = "";
        }

        cvh.tv_category.setText(categoryName);
        if(part.length == 1){
            id = part[0];
        } else if(part.length == 2){
            id = part[1];
        }
        item.id = id;

        if(cookie != null) {

            if(gd.items !=null && item != null && id != null) {
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
                        if(CommonUtils.isNetworkAvailable(context)){
                            cvh.saveBTN.setBackgroundDrawable(drawableUnSaved);
                            cvh.tv_save.setText("SAVE");
                            item.isSaved = false;
                            new addRemoveProduct().execute(Constants.REMOVE_ITEM_FROM_WISH_LIST.replace("@itemid@",item.id).replace("@usercok@",cookie));
                            /**remove gd item from wish list**/
                            if(mc.wishList != null){
                                GadgetItem removeGadget = null;
                                for(GadgetItem item : mc.wishList) {

                                    if (item.gadget_title.equals(item.gadget_title)) {
                                        removeGadget = item;
                                    }
                                }

                                if(removeGadget != null){
                                    mc.wishList.remove(removeGadget);
                                }
                            }
                        } else {
                            //No internet
                            Toast.makeText(context,"No internet! Please check your network", Toast.LENGTH_LONG).show();
                        }

                    } else {

                        if(CommonUtils.isNetworkAvailable(context) ){
                            cvh.saveBTN.setBackgroundDrawable(drawableSave);
                            cvh.tv_save.setText("SAVED");
                            item.isSaved = true;
                            new addRemoveProduct().execute(Constants.ADD_ITEM_TO_WISH_LIST.replace("@itemid@",item.id).replace("@usercok@",cookie));

                            if(mc.wishList != null)
                                mc.wishList.add(0,item);

                        } else {
                            //No internet
                            Toast.makeText(context,"No internet! Please check your network", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    Intent intent = new Intent(context, LogInActivity.class);
                    context.startActivity(intent);
                }
            }
        });

        cvh.rl_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doShare(item);
            }
        });
        cvh.pgLoading.setVisibility(View.VISIBLE);
        cvh.imgView.setImageDrawable(null);
        cvh.tv_price.setText(item.gadget_price);
        String dayMonth = getDayAndMonth(CommonUtils.getPubDate(item.gadget_pubDate));
        String dayOfWeek = null;

        try {
            dayOfWeek = CommonUtils.calculateDate(CommonUtils.getPubDate(item.gadget_pubDate));
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }

        //Listener
        changeDateTv(dayOfWeek + " " + dayMonth);
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

    public GadgetItem getItem(int pos) throws IndexOutOfBoundsException{

        return mListItem.get(pos);
    }

    public void setItem(int position, GadgetItem item){
        mListItem.set(position,item);
        notifyItemChanged(position);
    }

    /***************************************************************************** Methods    **************************************************************************************/
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    public void setOnDataChangeListener(OnDataChangeListener onDataChangeListener){mOnDataChangeListener = onDataChangeListener;}

    public void doShare(GadgetItem mData){
        final ProgressDialog mProgressDialog = new ProgressDialog(context);
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
        context.startActivity(Intent.createChooser(intent, "Share Via"));
        mProgressDialog.dismiss();
    }

    /** CHANGE THE DATE **/
    private void changeDateTv(String data) {

        if(mOnDataChangeListener != null){
            mOnDataChangeListener.onDataChanged(data);
        }
    }

    /*	VF: creating a item to show*/
    private String getDayAndMonth(String pDate){
        //Log.d("dateTag","inside fixdateTIme" +pDate);
        String argDate = "";
        char a;
        if (pDate != null) {
            for (int i = 0; i < 5; i++) {
                a = pDate.charAt(i);
                argDate = argDate + a;
            }
        }

        return argDate;
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

    public interface OnDataChangeListener{
        void onDataChanged(String date);
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
//                    logOut();

                } else if(result.getString("status").equals("OK")) {
                    new getWishList().execute(Constants.GET_WISH_LIST.concat(cookie));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if(context != null)Toast.makeText(context, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
            } catch(Exception e) {
                e.printStackTrace();
                if(context != null)Toast.makeText(context, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
            }
        }
    }

    /************************************************************** GET WISH LIST JSON - ON POST EXECUTE ADD ITEM ***********************************************************************/
    private class getWishList extends AsyncTask<String, Void, JSONObject> {

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
                    hideDialog();
                } else if(result.getString("status").equals("OK")) {
                    gd.items = result.getString("items");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                if(context != null)Toast.makeText(context, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
            } catch (Exception e){
                e.printStackTrace();
                if(context != null)Toast.makeText(context, Constants.ERROR_MESSAGE,Toast.LENGTH_LONG).show();
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

