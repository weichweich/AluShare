package edu.kit.tm.pseprak2.alushare.presenter;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.AluObserver;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.NetworkingService;
import edu.kit.tm.pseprak2.alushare.view.adapter.ContactTabRecyclerAdapter;
import edu.kit.tm.pseprak2.alushare.view.fragments.ContactTabFragment;

/**
 * @author Niklas Sänger
 *         Presenter for the ContactTab fragment
 */
public class ContactTabPresenter implements AluObserver<Contact> {
    private ContactHelper contactHelper;
    private ChatHelper chatHelper;
    private ContactTabFragment view;
    private ContactTabRecyclerAdapter adapter;

    /**
     * Constructor. Initializies SQLContext
     */
    public ContactTabPresenter(ContactTabFragment view, ContactTabRecyclerAdapter adapter) {
        if (view == null || adapter == null) {
            throw new IllegalArgumentException();
        }
        this.view = view;
        this.adapter = adapter;

        contactHelper = HelperFactory.getContacHelper(view.getActivity().getApplicationContext());
        contactHelper.addObserver(this);

        chatHelper = HelperFactory.getChatHelper(view.getActivity().getApplicationContext());
    }


    public String startChat(long id) {
        Contact contact = contactHelper.getContactByID(id);
        Contact self = contactHelper.getSelf();

        Context appContext = view.getActivity().getApplicationContext();
        String chatID = NetworkingService.getNewNetworkChatID(appContext);

        List<Contact> receiver = new ArrayList<>();
        receiver.add(self);
        receiver.add(contact);

        List<Chat> chats = chatHelper.getChatsByContactID(contact.getId());
        Chat chat;
        if (chats.isEmpty()) {
            chat = new Chat(chatID, contact.getName(view.getActivity()), receiver);
            chatHelper.insert(chat);
        } else {
            chat = chats.get(0);
        }

        return chat.getNetworkChatID();
    }

    public String getChatID(long id) {
        Log.d(getClass().toString(), "" + id);
        List<Chat> chats = chatHelper.getChatsByContactID(id);
        if (chats.size() == 0) {
            return null;
        }
        return chats.get(0).getNetworkChatID();
    }

    /**
     * Returns current list of contacts.
     *
     * @return contact list.
     */
    public List<Contact> getContactList() {
        return contactHelper.getContacts(Integer.MAX_VALUE,0);
    }

    /**
     * Return filterd list of contacts
     *
     * @param query Parameter to filter the list.
     * @return filtered list.
     */
    public List<Contact> getContactList(String query) {
        query = query.toLowerCase();
        final List<Contact> filterdList = new ArrayList<>();
        for (Contact contact : contactHelper.getContacts()) {

            if (contact.getName(view.getActivity()).toLowerCase().contains(query)) {
                filterdList.add(contact);
            }
        }
        return filterdList;
    }

    public boolean removeContact(long id) {
        Contact c = contactHelper.getContactByID(id);
        view.getActivity().getApplicationContext().getContentResolver()
                .delete(ContactsContract.Data.CONTENT_URI, ContactsContract.Data._ID + "=?", new String[]{String.valueOf(getDataId(c.getSystemContactId(view.getActivity().getApplicationContext())))});

        if (!contactHelper.isContactInAnyChat(id)) {
            contactHelper.delete(c);
            return true;
        } else {
            List<Chat> chats = chatHelper.getChatsByContactID(id);
            for(Chat ch : chats) {
                ch.setTitle(c.getNetworkingId());
                chatHelper.update(ch);
            }
            c.setLookUpKey("");
            contactHelper.update(c);
            return false;
        }
    }

    public long getDataId(long systemContactId) {
        long dataId = -1;
        Cursor findContact = view.getActivity().getApplicationContext().getContentResolver().query(ContactsContract.Data.CONTENT_URI,
                new String[]{ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Email.LABEL, ContactsContract.CommonDataKinds.Email.ADDRESS},
                ContactsContract.Data.CONTACT_ID + "=?" + " AND "
                        + ContactsContract.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "'",
                new String[]{String.valueOf(systemContactId)}, null);


        if (findContact.moveToFirst()) {
            if (("Alushare").equals(findContact.getString(findContact.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL)))) {
                dataId = findContact.getLong(findContact.getColumnIndex(ContactsContract.Data._ID));
                return dataId;

            } else {
                while (findContact.moveToNext()) {
                    if (("Alushare").equals(findContact.getString(findContact.getColumnIndex(ContactsContract.CommonDataKinds.Email.LABEL)))) {
                        dataId = findContact.getLong(findContact.getColumnIndex(ContactsContract.Data._ID));
                        return dataId;
                    }


                }
            }
        }
        return dataId;
    }

    public void updateContactNotFound(long contactId) {
        Contact contact = contactHelper.getContactByID(contactId);
        contact.setLookUpKey("");
        contactHelper.update(contact);
    }

    @Override
    public void updated(Contact data) {
        this.update(data);
    }

    @Override
    public void inserted(Contact data) {
        this.update(data);
    }

    @Override
    public void removed(Contact data) {
        this.update(data);
    }

    private void update(Contact contact) {
        if (view != null && view.getActivity() != null) {
            view.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.updateDataSet(getContactList());
                }
            });
        }
    }
}
