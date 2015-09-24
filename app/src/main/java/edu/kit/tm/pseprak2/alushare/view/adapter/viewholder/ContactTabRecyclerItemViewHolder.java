package edu.kit.tm.pseprak2.alushare.view.adapter.viewholder;

import android.graphics.Bitmap;
import android.media.Image;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import edu.kit.tm.pseprak2.alushare.R;

/**
 * ViewHolder for RecyclerView showing contact items.
 */
public class ContactTabRecyclerItemViewHolder extends TabViewHolder {

    private final TextView mItemTextContactName;
    private final ImageView mItemImageContact;
    private boolean mAnonymousContact;
    private MenuInflater inf;

    /**
     * Initializes Viewholder.
     *
     * @param parent              Parent view.
     * @param itemTextContactName Contact name textview.
     * @param itemContactPhoto    Contact image imageview.
     */
    public ContactTabRecyclerItemViewHolder(final View parent, TextView itemTextContactName, ImageView itemContactPhoto, MenuInflater inf) {
        super(parent);
        this.mItemTextContactName = itemTextContactName;
        this.mItemImageContact = itemContactPhoto;
        this.mAnonymousContact = false;
        this.inf = inf;
    }

    /**
     * Erzeugt Context Menu
     * @param menu
     * @param v
     * @param menuInfo
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(mAnonymousContact) {
            menu.add(R.id.context_tab_contact_group,R.id.context_tab_contact_link_to_contact, 0,R.string.link_to_contact);
        } else {
            menu.add(R.id.context_tab_contact_group,R.id.context_tab_show_in_contacts, 0, R.string.show_in_contacts);
        }
        inf.inflate(R.menu.context_tab_contact,menu);
    }

    /**
     * Creates a new ViewHolder instance.
     *
     * @param parent Parent view.
     * @return New ViewHolder instance.
     */
    public static ContactTabRecyclerItemViewHolder newInstance(View parent, MenuInflater inflater) {
        TextView itemTextContactName = (TextView) parent.findViewById(R.id.contact_name);
        ImageView itemImageContact = (ImageView) parent.findViewById(R.id.contact_photo);
        return new ContactTabRecyclerItemViewHolder(parent, itemTextContactName, itemImageContact, inflater);
    }

    /**
     * Provides contact name.
     *
     * @param text Contact name.
     */
    public void setItemContactName(CharSequence text) {
        mItemTextContactName.setText(text);
    }

    /**
     * Provides contact image
     *
     * @param photo Contact Image.
     */
    public void setItemContactImage(Bitmap photo) {
        mItemImageContact.setImageBitmap(photo);
    }

    /**
     * Setzt anonymen Kontakt
     * @param anonymousContact true/false
     */
    public void setAnonymousContact(boolean anonymousContact) {
        this.mAnonymousContact = anonymousContact;
    }
}