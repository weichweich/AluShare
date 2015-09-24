package edu.kit.tm.pseprak2.alushare.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import edu.kit.tm.pseprak2.alushare.R;

/**
 * Item Decorater für RecyclerViews.
 */
public class Divider extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

    /**
     * Konstruktor
     * @param context Context
     */
    public Divider(Context context) {
        mDivider = ContextCompat.getDrawable(context, R.drawable.list_divider);
    }

    /**
     * Erzeugt den Divider für sämtliche RecyclerViews
     * @param c Canvas Element welche gezeichnet werden soll.
     * @param parent RecyclerView
     * @param state
     */
    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
