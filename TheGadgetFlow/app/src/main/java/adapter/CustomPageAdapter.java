package Adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.CloudieNetwork.GadgetFlow.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import Helper.ScalingUtilities;

/**
 * This adapter is for the View Pager in Activity Details
 */
public class CustomPageAdapter extends PagerAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    private ArrayList<String> images;
    private ProgressBar mProgressBar;
    private int cellSize, cellHeight;
    private String cookie;


    public CustomPageAdapter(Context pContext, ArrayList<String> images, ProgressBar progressBar,String cookie){
        this.mContext = pContext;
        mLayoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.images = images;
        this.cookie = cookie;

        for(int i =0;i< images.size();i++){
           //Log.d("ACTIVITY_DETAILS",images.get(i));
            //remove the last element of the image gallery
            if(i == images.size()-1){
                images.remove(i);
            }
        }
        this.mProgressBar = progressBar;
        this.cellSize = ScalingUtilities.getScreenWidth(pContext);

        if(pContext.getResources().getBoolean(R.bool.isTabletX)){
            cellHeight = 600;
        }else if(pContext.getResources().getBoolean(R.bool.isTabletl)){
            cellHeight = 450;
        } else{
            cellHeight = 300;
        }

    }
    //This method should return the number of views available, i.e., number of pages to
    // be displayed/created in the ViewPager.
    @Override
    public int getCount() {
        return images.size();
    }
    /*
    checks whether the View passed to it (representing the page) is associated with that key or not.
     */
    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == (object);
    }

    @Override
    public Object instantiateItem(final ViewGroup container, int position) {
        View itemView = mLayoutInflater.inflate(R.layout.pager_item, container, false);
        ImageView imageView = (ImageView) itemView.findViewById(R.id.imageView);
        if( cookie == null ) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
        Glide.with(mContext).load(images.get(position))
                .override(cellSize,cellSize)
                .fitCenter()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mProgressBar.setVisibility(View.INVISIBLE);
                        return false;
                    }
                }).into(imageView);

        container.addView(itemView);

        return itemView;
    }
    //Removes the page from the container for the given position
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}
