package View;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by vfour_000 on 4/10/2015.
 */
public class CategoryItemDecoration extends RecyclerView.ItemDecoration {

    private int mSizeGridSpacingPx = 1; // the size of the divider


    @Override
    public void getItemOffsets(Rect outRect, View view,  RecyclerView parent, RecyclerView.State state) {
        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        outRect.bottom = mSizeGridSpacingPx;

        if(itemPosition == 59){
            outRect.bottom = 0;
        }
    }
}
