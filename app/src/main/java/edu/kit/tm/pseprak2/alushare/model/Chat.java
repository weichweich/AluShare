package edu.kit.tm.pseprak2.alushare.model;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds all necessary information about a chat that is currently
 * generated or got loaded from a data source.
 */
public class Chat {
    private static final String TAG = "Chat";
    private String title;
    private List<Data> data = new ArrayList<>();
    private List<Contact> receiver = new ArrayList<>();
    private String networkChatID;
    private boolean isDeleted = false;


    /**
     * Constructor to create a chat with given networkChatId, title, receiver list and isDeleted.
     * @param networkChatID unique identifier of the chat.
     * @param title title of the chat.
     * @param receiver list of all receivers that are in the chat.
     * @param isDeleted boolean to determine if chat got deleted.
     */
    public Chat(String networkChatID, String title, List<Contact> receiver, boolean isDeleted) {
        this(networkChatID, title, receiver);
        setIsDeleted(isDeleted);
    }

    /**
     * Constructor to create a chat with given networkChatId, title and receiver list.
     * @param networkChatID unique identifier of the chat.
     * @param title title of the chat.
     * @param receiver list of all receivers that are in the chat.
     */
    public Chat(String networkChatID, String title, List<Contact> receiver) {
        if (receiver.size() < 2) {
            Log.w("Chat", "Receiverlist sollte nicht kleiner als 2 sein!");
        }
        setTitle(title);
        addReceiver(receiver);
        setNetworkChatID(networkChatID);
    }

    /**
     * Gets the unique identifier of the chat.
     * @return unique identifier string if set, otherwise an empty string.
     */
    public String getNetworkChatID() {
        return this.networkChatID;
    }

    /**
     * Sets the unique identifier of the chat.
     * @param networkChatID unique identifier string to set.
     */
    public void setNetworkChatID(String networkChatID) {
        this.networkChatID = networkChatID;
    }

    /**
     * Gets the title of this chat.
     * @return title of the chat if set, otherwise an empty string.
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Gets the title of this chat with the given context.
     * @param context the application context.
     * @return returns the title if chat is a group chat otherwise the name of only receiver.
     */
    public String getTitle(Context context) {
        if (receiver.size() == 1) {
            return receiver.get(0).getName(context);
        } else {
            return this.title;
        }
    }

    /**
     * Sets the title of the chat.
     * @param title the title to set.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets a list of all data that is contained in this chat.
     * @return list of data objects.Can be empty.
     */
    public List<Data> getDataObjects() {
        return this.data;
    }

    /**
     * Gets a list of all reiceivers of this chat.
     * @return list of receivers. Can be empty.
     */
    public List<Contact> getReceivers() {
        return this.receiver;
    }

    /**
     * Adds a data object to the data list and sets the networkingChatId of the data object.
     * @param data the data to add to the list.
     */
    public void addData(Data data) {
        this.data.add(data);
        //data.setReceivers(new ArrayList<Contact>(this.receiver));
        data.setNetworkChatID(this.getNetworkChatID());
    }

    /**
     * Adds a data object to the data list at a given position and sets the networkingChatId of the data object.
     * @param data the data to add to the list.
     * @param position position of data in the list.
     */
    public void addData(int position, Data data) {
        this.data.add(position, data);
        //data.setReceivers(new ArrayList<Contact>(this.receiver));
        data.setNetworkChatID(this.getNetworkChatID());
    }

    /**
     * Adds a data list to the data list.
     * @param dataList list to add to the data list.
     */
    public void addData(List<Data> dataList) {
        for (Data data : dataList) {
            addData(data);
        }
    }

    /**
     * Adds a receiver to the receiver list.
     * @param contact receiver to add to the receiver list.
     */
    public void addReceiver(Contact contact) {
        this.receiver.add(contact);
    }

    /**
     * Adds a receiver list to the receiver list.
     * @param contact receiver list to add.
     */
    public void addReceiver(List<Contact> contact) {
        this.receiver.addAll(contact);
    }

    /**
     * Removes a receiver from the receiver list.
     * @param contact receiver to remove from the list.
     */
    public void removeReceiver(Contact contact) {
        Iterator<Contact> itr = receiver.iterator();
        while (itr.hasNext()) {
            if (itr.next().equals(contact)) {
                itr.remove();
            }
        }
    }

    /**
     * Removes a part of the receiver list with given list. Difference of both lists remains.
     * @param contacts list of contacts to remove from receiver list.
     */
    public void removeReceiver(List<Contact> contacts) {
        for (Contact c : contacts) {
            removeReceiver(c);
        }
    }

    /**
     * Method to determine if the chat is a group chat or not.
     * @return true if chat is a group chat, otherwise false.
     */
    public boolean isGroupChat() {
        return (receiver.size() > 2);
    }

    /**
     * Returns the latest data object from the data list.
     * @return latest data object from data list.
     */
    public Data getLastData() {
        if (data.size() > 0) {
            return data.get(data.size() - 1);
        }
        return null;
    }

    /**
     * Checks if the given contact is the creator the chat or not.
     * @param contact the contact to check on.
     * @return true if contact is the admin of the chat, otherwise false.
     */
    public boolean isAdmin(Contact contact) {
        return contact != null && isAdmin(contact.getNetworkingId());
    }

    /**
     * Checks if a contact is the creator the chat or not with a given networkingId.
     * @param nid networkingId.
     * @return true if networkingId is the admin of the chat, otherwise false.
     */
    public boolean isAdmin(String nid) {
        String[] splitedNID = networkChatID.split(":");
        if (splitedNID.length != 2) {
            Log.w(TAG, "Strange CNID! " + networkChatID);
        }
        return splitedNID[0].equals(nid);
    }

    /**
     * Sets a boolean if this chat got deleted.
     * @param isDeleted should be true if chat got deleted, otherwise false.
     */
    public void setIsDeleted(boolean isDeleted) {
        this.isDeleted = isDeleted;
    }

    /**
     * Gets boolean isDeleted.
     * @return true if chat got deleted, otherwise false.
     */
    public boolean isDeleted() {
        return isDeleted;
    }
}
