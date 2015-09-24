package edu.kit.tm.pseprak2.alushare.view.adapter;

import android.view.MenuItem;
import android.view.View;

/**
 * Represent the ItemClickListener of the ChatRecyclerItemViewHolder
 *
 * Created by Arthur Anselm on 15.08.15.
 */
public interface ChatItemClickListener {

    /**
     * For normal clicks on the speech bubble.
     *
     * @param view View containing listener.
     * @param fileId   fileID of the object.
     */
    void onItemClick(View view, long fileId);

    /**
     * For normal clicks on the not sent imageView.
     *
     * @param view View containing listener
     * @param dataId dataID of the object.
     */
    void onReSendClick(View view, long dataId);

    boolean onPopUpClick(MenuItem item, long dataId);
}
