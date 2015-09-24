package edu.kit.tm.pseprak2.alushare.view.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.ChatTabRecyclerItemViewHolder;

/**
 * RecyclerAdapter for ChatTab
 */
public class ChatTabRecyclerAdapter extends TabAdapter<Chat> {
    private Fragment context;
    private String networkingID;
    private Contact ownContact;

    /**
     * {@inheritDoc}
     */
    public ChatTabRecyclerAdapter(Fragment context) {
        this.context = context;
        this.networkingID = "";
        this.ownContact = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context tContext = parent.getContext();
        View view = LayoutInflater.from(tContext).inflate(R.layout.recycler_item_chat_tab, parent, false);
        return ChatTabRecyclerItemViewHolder.newInstance(view, this.context.getActivity());
    }

    /**
     * Updated Chat
     * @param netID Netzwerk ID des Chats
     * @param data neues Dataobjekt
     */
    public void updateChat(String netID, Data data) {
        List<Chat> chatList = getList();
        for (Chat chat : getList()) {
            if (chat.getNetworkChatID().equals(netID)) {
                int pos = chatList.indexOf(chat);
                chat.addData(data);
                chatList.set(pos, chat);
                notifyDataSetChanged();
                moveItem(pos, 0);
                break;
            }
        }
    }

    /**
     * Updated Chat mit Data Objekt
     * @param data modifiziertes Data Objekt
     */
    public void updateChat(Data data) {
        List<Chat> chatList = getList();
        String netID = data.getNetworkChatID();
        for (Chat chat : getList()) {
            if (chat.getNetworkChatID().equals(netID)) {
                int pos = chatList.indexOf(chat);
                chat.getDataObjects().clear();
                chat.addData(data);
                notifyItemChanged(pos);
                break;
            }
        }
    }

    /**
     * Adds data to the ViewHolder. Shows an image, the last data received and the chat name
     *
     * @param viewHolder List item.
     * @param position   Position of the list item.
     */
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        ChatTabRecyclerItemViewHolder holder = (ChatTabRecyclerItemViewHolder) viewHolder;
        Chat chat = getList().get(position);
        final String id = chat.getNetworkChatID();

        holder.setItemTitle(chat.getTitle());

        if (!chat.getReceivers().isEmpty()) {
            if (chat.isGroupChat()) {
                holder.setItemImage(R.drawable.ic_group_black_24dp);
                holder.isGroup();
                if (ownContact != null && chat.isAdmin(ownContact)) {
                    holder.isAdmin();
                }
            } else {
                List<Contact> c = chat.getReceivers();
                c.remove(ownContact);
                if (c.size() == 1) {
                    Contact contact = c.get(0);
                    holder.setItemImage(contact.getPicture(context.getActivity().getApplicationContext()));
                }

            }
        }
        Data last = chat.getLastData();
        if (last != null) {
            DataState state;
            try {

                state = last.getState(ownContact);
                if (last.received() && state != null && !state.wasRead()) {
                    holder.setUnread();
                } else {
                    holder.setRead();
                }

                holder.setDate(last.getTimestampString(context.getContext()));
            } catch (Exception e) {
                Log.e("ChatTabAdapter", e.toString());
            }


        }
        if (last == null) {
            holder.setItemLastMessage(context.getString(R.string.no_message_received));
        } else if (last.getFile() == null) {
            holder.setItemLastMessage(last.getText());
        } else if (last.getText().isEmpty()) {
            holder.setItemLastMessage(last.getFile().getASName());
        }
        holder.setItemListener(getItemListener());
        holder.setItemID(getList().indexOf(chat));
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                networkingID = id;
                return false;
            }
        });

    }

    /**
     * Setzt ID des Chat Objekts
     *
     * @return Chat Identifier
     */
    public String getId() {
        return networkingID;
    }

    /**
     * Setzt eigenen Kontakt
     *
     * @param contact Eigener Kontakt
     */
    public void setOwnContact(Contact contact) {
        this.ownContact = contact;
    }

    /**
     * Gibt Chat ID für postion zurück
     * @param pos Position in Liste.
     * @return Netzwerkadresse des Chats
     */
    public String getChatIdentByPos(long pos) {
        return getList().get((int) pos).getNetworkChatID();
    }

    /**
     * Updates DataSet, notifys ViewHolder when Data changed.
     *
     * @param id modified item
     */
    public void updateData(String id, Chat newChat) {
        Chat chat = null;
        for (Chat c : getList()) {
            if (c.getNetworkChatID().equals(id)) {
                chat = c;
                break;
            }
        }
        if (chat != null) {
            int pos = getList().indexOf(chat);
            if (pos != -1) {
                getList().set(pos, newChat);
                notifyItemChanged(pos);
            }
        }
    }
}