package edu.kit.tm.pseprak2.alushare.view.adapter.viewholder;

import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import edu.kit.tm.pseprak2.alushare.R;

/**
 * ViewHolder for RecyclerView showing file items.
 */
public class FileTabRecyclerItemViewHolder extends TabViewHolder {

    private final TextView mItemFilePath;
    private final TextView mItemSender;
    private final ImageView mItemImage;
    private MenuInflater inf;

    /**
     * Initializes Viewholder, sets Views
     *
     * @param parent         Parten view
     * @param itemTextPath   TextView path
     * @param itemTextSender Textview sender
     * @param itemImage      Textview image
     */
    public FileTabRecyclerItemViewHolder(final View parent, TextView itemTextPath, TextView itemTextSender, ImageView itemImage, MenuInflater inf) {
        super(parent);
        this.mItemFilePath = itemTextPath;
        this.mItemSender = itemTextSender;
        this.mItemImage = itemImage;
        this.inf = inf;
    }

    /**
     * Creates an Instance of the ViewHolder
     *
     * @param parent Parent View
     * @return A new instance of FileTabRecyclerItem
     */
    public static FileTabRecyclerItemViewHolder newInstance(View parent, MenuInflater inflater) {
        TextView itemTextTitle = (TextView) parent.findViewById(R.id.file_path);
        TextView itemTextLastMessage = (TextView) parent.findViewById(R.id.file_from);
        ImageView itemImage = (ImageView) parent.findViewById(R.id.image);
        return new FileTabRecyclerItemViewHolder(parent, itemTextTitle, itemTextLastMessage, itemImage, inflater);
    }

    /**
     * Provides path text.
     *
     * @param text Path text.
     */
    public void setItemPath(CharSequence text) {
        mItemFilePath.setText(text);
    }

    /**
     * Provides sender text.
     *
     * @param text Sender text.
     */
    public void setItemSender(CharSequence text) {
        mItemSender.setText(text);
    }

    /**
     * Provides file image
     *
     */
    public ImageView getItemImageFile(){
        return mItemImage;
    }

    /**
     * Setzt context menu
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        inf.inflate(R.menu.context_tab_file, menu);
    }
}