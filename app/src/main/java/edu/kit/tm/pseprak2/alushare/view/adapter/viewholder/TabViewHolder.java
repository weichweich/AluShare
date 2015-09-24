package edu.kit.tm.pseprak2.alushare.view.adapter.viewholder;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;

import edu.kit.tm.pseprak2.alushare.view.adapter.ItemClickListener;

/**
 * ViewHolder class for items in the Tabs
 * Created by niklas on 04.07.15.
 */
public abstract class TabViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {
    private static final String TAG = "TabViewHolder";
    private ItemClickListener itemClickListener;
    private long itemID;

    /**
     * Initilizes Viewholder
     *
     * @param itemView View of the listitem.
     */
    public TabViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
        itemView.setOnCreateContextMenuListener(this);
        itemClickListener = null;
        itemID = -1;
    }

    /**
     * Zur erstellung des ContextMenüs
     * @param menu
     * @param v
     * @param menuInfo
     */
    public abstract void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo);

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        if (itemClickListener != null) {
            itemClickListener.onItemClick(v, itemID);
        }
    }

    /**
     * Sets the click listener of the item.
     *
     * @param itemListener
     */
    public void setItemListener(ItemClickListener itemListener) {
        this.itemClickListener = itemListener;
    }

    /**
     * @param id
     */
    public void setItemID(long id) {
        this.itemID = id;
    }

}
