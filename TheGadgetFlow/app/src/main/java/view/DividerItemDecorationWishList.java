package View;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import Helper.GD;
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

/**
 * Draws item dividers
 * that are expected from a vertical list implementation, such as
 * ListView.
 */
public class DividerItemDecorationWishList extends RecyclerView.ItemDecoration {

    private int mSizeGridSpacingPx; // the size of the divider
    private int mGridSize; // the size of the grid
    private boolean mNeedLeftSpacing = false;
    private Drawable mDivider;
    private GD gd = GD.get();
    private static final String TAG = "DividerItemWishList";

    public DividerItemDecorationWishList(int gridSpacingPx, int GridSize,Drawable divider) {
        mSizeGridSpacingPx = gridSpacingPx;
        mGridSize = GridSize;
        mDivider = divider;

    }
    @Override
    public void getItemOffsets(Rect outRect, View view,  RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int itemPosition;
        int itemCount = state.getItemCount();
//        if(gd.printLogs)Log.d(TAG,"itemCount: " + itemCount);
        int frameWidth = (int) ((parent.getWidth() - (float) mSizeGridSpacingPx * (mGridSize - 1)) / mGridSize);
        int padding = parent.getWidth() / mGridSize - frameWidth;
        itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();

        outRect.top = mSizeGridSpacingPx;

        if (itemPosition % mGridSize == 0) {
            outRect.left = mSizeGridSpacingPx;
            outRect.right = padding;
            mNeedLeftSpacing = true;
        } else if ((itemPosition + 1) % mGridSize == 0) {
            mNeedLeftSpacing = false;
            outRect.right = mSizeGridSpacingPx;
            outRect.left = padding;
        } else if (mNeedLeftSpacing) {
            mNeedLeftSpacing = false;
            outRect.left = mSizeGridSpacingPx - padding;
            if ((itemPosition + 2) % mGridSize == 0) {
                outRect.right = mSizeGridSpacingPx - padding;
            } else {
                outRect.right = mSizeGridSpacingPx / 2;
            }
        } else if ((itemPosition + 2) % mGridSize == 0) {
            mNeedLeftSpacing = false;
            outRect.left = mSizeGridSpacingPx / 2;
            outRect.right = mSizeGridSpacingPx - padding;
        } else {
            mNeedLeftSpacing = false;
            outRect.left = mSizeGridSpacingPx / 2;
            outRect.right = mSizeGridSpacingPx / 2;
        }
        outRect.bottom = 0;

    }
}

