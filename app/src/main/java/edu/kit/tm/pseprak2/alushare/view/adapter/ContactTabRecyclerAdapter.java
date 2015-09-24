package edu.kit.tm.pseprak2.alushare.view.adapter;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.ContactTabRecyclerItemViewHolder;
import edu.kit.tm.pseprak2.alushare.view.fragments.ContactTabFragment;

/**
 * RecyclerAdapter for ContactTab
 */
public class ContactTabRecyclerAdapter extends TabAdapter<Contact> {
    /**
     * Context of the parent Activity
     */
    private Fragment context;
    private long id;

    /**
     * Initializes contact tlist.
     */
    public ContactTabRecyclerAdapter(Fragment context) {
        this.context = context;
        id = -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context tContext = parent.getContext();
        View view = LayoutInflater.from(tContext).inflate(R.layout.recycler_item_contact_tab, parent, false);
        return ContactTabRecyclerItemViewHolder.newInstance(view, this.context.getActivity().getMenuInflater());
    }

    /**
     * Adds data to the ViewHolder. Shows an contact image and the contact name
     *
     * @param viewHolder List item.
     * @param position   Position of the list item.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ContactTabRecyclerItemViewHolder holder = (ContactTabRecyclerItemViewHolder) viewHolder;

        final Contact contact = getList().get(position);

        String itemText = contact.getName(context.getActivity());
        holder.setItemContactName(itemText);
        holder.setItemListener(getItemListener());
        holder.setItemID(contact.getId());
        holder.setItemContactImage(contact.getPicture(context.getActivity()));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                id = contact.getId();
                return false;
            }
        });
        holder.setAnonymousContact(contact.getLookUpKey().isEmpty());
    }

    /**
     * Gibt ID des gedrückten Contact Objekts zurück
     *
     * @return ID
     */
    public long getId() {
        return this.id;
    }

}