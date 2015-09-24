package edu.kit.tm.pseprak2.alushare.view.adapter.viewholder;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import edu.kit.tm.pseprak2.alushare.R;

/**
 * ViewHolder for RecyclerView showing chat items.
 */
public class ChatTabRecyclerItemViewHolder extends TabViewHolder {

    private final TextView mItemTextTitle;
    private final TextView mItemTextLastMessage;
    private final ImageView mItemImage;
    private final TextView mItemTextDate;
    private MenuInflater inf;
    private boolean isAdmin;
    private boolean isGroup;

    /**
     * Initializes Viewholder, sets Views.
     *
     * @param parent              Parent view.
     * @param itemTextTitle       TextView title.
     * @param itemTextLastMessage TextView last message.
     * @param itemContactPhoto    Imageview contact photo.
     */
    public ChatTabRecyclerItemViewHolder(View parent, TextView itemTextTitle, TextView itemTextLastMessage, ImageView itemContactPhoto, TextView itemTextDate, MenuInflater inf) {
        super(parent);
        this.mItemTextTitle = itemTextTitle;
        this.mItemTextLastMessage = itemTextLastMessage;
        this.mItemImage = itemContactPhoto;
        this.mItemTextDate = itemTextDate;
        this.inf = inf;
    }

    /**
     * Creates an Instance of the ViewHolder
     *
     * @param parent Parent View
     * @return A new instance of ChatTabRecyclerItem
     */
    public static ChatTabRecyclerItemViewHolder newInstance(View parent, Activity ac) {
        TextView itemTextTitle = (TextView) parent.findViewById(R.id.text_title);
        TextView itemTextLastMessage = (TextView) parent.findViewById(R.id.text_lastMessage);
        TextView itemTextDate = (TextView) parent.findViewById(R.id.chat_item_last_date);
        ImageView itemImage = (ImageView) parent.findViewById(R.id.person_photo);
        return new ChatTabRecyclerItemViewHolder(parent, itemTextTitle, itemTextLastMessage, itemImage, itemTextDate, ac.getMenuInflater());
    }

    /**
     * Provides item title.
     *
     * @param text Item Title
     */
    public void setItemTitle(CharSequence text) {
        mItemTextTitle.setText(text);
    }

    /**
     * Provides last message.
     *
     * @param text Last message.
     */
    public void setItemLastMessage(CharSequence text) {
        mItemTextLastMessage.setText(text);
    }

    /**
     * Provides chat image
     *
     * @param photoID Chat Image
     */
    public void setItemImage(int photoID) {
        mItemImage.setImageResource(photoID);
    }

    /**
     * Setzt ImageView von Bitmap
     * @param photo Bitmap resource
     */
    public void setItemImage(Bitmap photo) {
        mItemImage.setImageBitmap(photo);
    }

    public void setUnread() {
        mItemTextTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mItemTextDate.setTypeface(Typeface.DEFAULT_BOLD);
    }

    public void setRead() {
        mItemTextDate.setTypeface(Typeface.DEFAULT);
        mItemTextTitle.setTypeface(Typeface.DEFAULT);
    }
    public void setDate(String date) {
        mItemTextDate.setText(date);
    }
    public void isAdmin() {
        isAdmin = true;
    }

    public void isGroup() {
        isGroup = true;
    }

    /**
     * Erzeugt Context Menu.. Manuell getesetet
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (isAdmin) {
            menu.add(R.id.context_tab_chat_group, R.id.context_tab_chat_rename, 0, R.string.rename_chat);
            menu.add(R.id.context_tab_chat_group, R.id.context_tab_chat_delete, 0, R.string.delete_group_chat_isAdmin);
        } else if (isGroup) {
            menu.add(R.id.context_tab_chat_group, R.id.context_tab_chat_delete, 0, R.string.delete_group_chat_default);
        } else {
            menu.add(R.id.context_tab_chat_group, R.id.context_tab_chat_delete, 0, R.string.delete);
        }
        inf.inflate(R.menu.context_tab_chat, menu);
    }
}