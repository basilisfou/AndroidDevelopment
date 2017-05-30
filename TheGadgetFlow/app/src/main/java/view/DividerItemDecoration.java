package View;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private int mSizeGridSpacingPx; // the size of the divider
    private int mGridSize = 1; // the size of the grid
    private boolean mNeedLeftSpacing = false;

    public DividerItemDecoration(int gridSpacingPx, int GridSize) {
        mSizeGridSpacingPx = gridSpacingPx;
        mGridSize = GridSize;
    }
    @Override
    public void getItemOffsets(Rect outRect, View view,  RecyclerView parent, RecyclerView.State state) {

            int itemPosition;
            int frameWidth = (int) ((parent.getWidth() - (float) mSizeGridSpacingPx * (mGridSize - 1)) / mGridSize);
            int padding = parent.getWidth() / mGridSize - frameWidth;
            itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
            if (itemPosition < mGridSize) {
                outRect.top = 0;
            } else {
                outRect.top = mSizeGridSpacingPx;
            }
            if (itemPosition % mGridSize == 0) {
                outRect.left = 0;
                outRect.right = padding;
                mNeedLeftSpacing = true;
            } else if ((itemPosition + 1) % mGridSize == 0) {
                mNeedLeftSpacing = false;
                outRect.right = 0;
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

