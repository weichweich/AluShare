package edu.kit.tm.pseprak2.alushare.model;

import android.content.Context;
import android.util.Log;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


import edu.kit.tm.pseprak2.alushare.R;

/**
 * Holds all necessary information about a data that is currently
 * generated or got loaded from a data source.
 */
public class Data {
    private static final String TAG = "Data";

    private long id = -1;
    private String networkChatID = "";
    private String text = "";
    private ASFile file;
    private HashMap<Long, DataState> receiverStateMap;
    private Timestamp timestamp;
    private Contact sender;
    private List<Contact> receivers;

    /**
     * Constructor that's only used by the {@link edu.kit.tm.pseprak2.alushare.model.helper.Helper}
     * classes to create an instance out of the information that's stored in a data source
     * (e.g SQLite Database).
     *
     * @param id Unique identifier that was set by the data source.
     * @param networkChatID Unique identifier of the chat that was set by the data source.
     * @param sender The sender of this data.
     * @param receiver List of all receivers that are receiving this data.
     * @param receiverStateMap State of this data for each receiver stored in this hashmap.
     * @param timestamp Timestamp when this data was created.
     * @param text Text that this data contains.
     * @param file ASFile that this data contains.
     */
    public Data(long id, String networkChatID, Contact sender, List<Contact> receiver, HashMap<Long, DataState> receiverStateMap, Timestamp timestamp, String text, ASFile file) {
        this(sender, receiver, receiverStateMap, text, file);
        setId(id);
        setTimestamp(timestamp);
        setNetworkChatID(networkChatID);
        setReceiverStateMap(receiverStateMap);
    }

    /**
     * Constructor that's only used by the {@link edu.kit.tm.pseprak2.alushare.model.helper.Helper}
     * classes to create an instance out of the information that's stored in a data source
     * (e.g SQLite Database).
     *
     * @param id Unique identifier that was set by the data source.
     * @param networkChatID Unique identifier of the chat that was set by the data source.
     * @param sender The sender of this data.
     * @param receiver List of all receivers that are receiving this data.
     * @param receiverStateMap State of this data for each receiver stored in this hashmap.
     * @param timestamp Timestamp when this data was created.
     * @param text Text that this data contains.
     */
    public Data(long id, String networkChatID, Contact sender, List<Contact> receiver, HashMap<Long, DataState> receiverStateMap, Timestamp timestamp, String text) {
        this(sender, receiver, receiverStateMap, text);
        setId(id);
        setTimestamp(timestamp);
        setNetworkChatID(networkChatID);
        setReceiverStateMap(receiverStateMap);
    }

    /**
     * Constructor that's only used by the {@link edu.kit.tm.pseprak2.alushare.model.helper.Helper}
     * classes to create an instance out of the information that's stored in a data source
     * (e.g SQLite Database).
     *
     * @param id Unique identifier that was set by the data source.
     * @param networkChatID Unique identifier of the chat that was set by the data source.
     * @param sender The sender of this data.
     * @param receiver List of all receivers that are receiving this data.
     * @param receiverStateMap State of this data for each receiver stored in this hashmap.
     * @param timestamp Timestamp when this data was created.
     * @param file ASFile that this data contains.
     */
    public Data(long id, String networkChatID, Contact sender, List<Contact> receiver, HashMap<Long, DataState> receiverStateMap, Timestamp timestamp, ASFile file) {
        this(sender, receiver, receiverStateMap, file);
        setId(id);
        setTimestamp(timestamp);
        setNetworkChatID(networkChatID);
        setReceiverStateMap(receiverStateMap);
    }

    /**
     * Constructor that creates an instance by given sender, receiver list, receiver state map, text and asfile.
     *
     * @param sender The sender of this data.
     * @param receiverStateMap State of this data for each receiver stored in this hashmap.
     * @param text Text that this data contains.
     * @param file ASFile that this data contains.
     */
    public Data(Contact sender, List<Contact> receivers, HashMap<Long, DataState> receiverStateMap, String text, ASFile file) {
        this(sender, receivers, receiverStateMap);
        setText(text);
        setFile(file);
    }

    /**
     * Constructor that creates an instance by given sender, receiver list, receiver state map and text.
     *
     * @param sender The sender of this data.
     * @param receiverStateMap State of this data for each receiver stored in this hashmap.
     * @param text Text that this data contains.
     */
    public Data(Contact sender, List<Contact> receivers, HashMap<Long, DataState> receiverStateMap, String text) {
        this(sender, receivers, receiverStateMap);
        setText(text);
    }

    /**
     * Constructor that creates an instance by given sender, receiver list, receiver state map and asfile.
     *
     * @param sender The sender of this data.
     * @param receiverStateMap State of this data for each receiver stored in this hashmap.
     * @param file ASFile that this data contains.
     */
    public Data(Contact sender, List<Contact> receivers, HashMap<Long, DataState> receiverStateMap, ASFile file) {
        this(sender, receivers, receiverStateMap);
        setFile(file);
    }

    private Data(Contact sender, List<Contact> receivers, HashMap<Long, DataState> receiverStateMap) {
        setSender(sender);
        setReceivers(receivers);
        setReceiverStateMap(receiverStateMap);
    }

    /**
     * Clones data object and returns a new reference.
     *
     *  @param data data object to clone.
     */
    public Data(Data data) {
        this(data.getId(), data.getNetworkChatID(), data.getSender(), data.getReceivers(), data.receiverStateMap, data.getTimestamp(), data.getText(), data.getFile());
        this.receiverStateMap = new HashMap<>(data.getState());
    }

    /**
     * Gets the receiver list of this data.
     * @return List of all receivers of the data.
     */
    public List<Contact> getReceivers() {
        return receivers;
    }

    /**
     * Sets the receiver list.
     * @param tmp the receiver list to set.
     */
    public void setReceivers(List<Contact> tmp) {
        if (tmp != null) {
            List<Contact> receivers = new ArrayList<>(tmp);
            Iterator<Contact> itr = receivers.iterator();
            while (itr.hasNext()) {
                if (itr.next().equals(sender)) {
                    itr.remove();
                }
            }
            this.receivers = receivers;
        }
    }

    /**
     * Gets the sender of this data.
     * @return the contact of the sender.
     */
    public Contact getSender() {
        return sender;
    }

    /**
     * Sets the sender of this data.
     * @param sender sender cotact of this data.
     */
    public void setSender(Contact sender) {
        this.sender = sender;
    }

    /**
     * Sets the identifier of the chat in which this data is stored.
     * @param chatId unique identifier of a chat.
     */
    public void setNetworkChatID(String chatId) {
        this.networkChatID = chatId;
    }

    /**
     * Gets the unique identifier of the chat that this data is stored.
      * @return the unique identifier of the chat.
     */
    public String getNetworkChatID() {
        return this.networkChatID;
    }

    /**
     * Sets the unique identifier of the data.
     * @param id the unique identifier of the data.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Gets the unique identifier of this data.
     * @return the unique identifier of this data.
     */
    public long getId() {
        return this.id;
    }

    /**
     * Sets the text.
     * @param text text to set.
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the text.
     * @return the text.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Gets the ASFile that was sent with this data.
     * @return an asfile.
     */
    public ASFile getFile() {
        return this.file;
    }

    /**
     * Sets the asfile that was sent with this data.
     * @param file the asfile.
     */
    public void setFile(ASFile file) {
        this.file = file;
        if (file != null) {
            file.setReceived(received());
        }
    }

    /**
     * Returns the datastate for this data with a given receiver. The receivers id is used as
     * a key in a hashmap.
     * @param receiver the receiver to use as a key for the hashmap.
     * @return datasate for the given receiver and this data.
     */
    public DataState getState(Contact receiver) {
        if (receiverStateMap == null || !receiverStateMap.containsKey(receiver.getId())) {
            return null;
        }
        return this.receiverStateMap.get(receiver.getId());
    }

    /**
     * Gets the devastate hashmap.
     * @return hasmap of all datastates. Keys are the id of all receivers.
     */
    public HashMap<Long, DataState> getState() {
        return new HashMap<Long, DataState>(this.receiverStateMap);
    }

    /**
     * Gets the timestamp when this data was created.
     * @return the timestamp.
     */
    public Timestamp getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp when this data was created.
     * @param timestamp the timestamp to set.
     */
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Returns the set timestamp in a readable format like HH:MM (e.G. 10:45).
     * @return the formatted timestamp as a string.
     */
    public String getTimestampString(Context context) {
        Calendar today = Calendar.getInstance();
        Calendar lastDate = Calendar.getInstance();
        lastDate.setTime(new Date(timestamp.getTime()));
        SimpleDateFormat format;
        String lastReceivedMessage = "defualt";
        if (today.get(Calendar.DAY_OF_YEAR) == lastDate.get(Calendar.DAY_OF_YEAR)) { // Letzte Nachricht heute
            format = new SimpleDateFormat("HH:mm");
            lastReceivedMessage = format.format(lastDate.getTime());
        } else if (today.get(Calendar.DAY_OF_YEAR) - lastDate.get(Calendar.DAY_OF_YEAR) == 1) { // Letzte Nachricht gestern
            lastReceivedMessage = context.getString(R.string.yesterday);
        } else {
            format = new SimpleDateFormat("dd.MM.yy");
            lastReceivedMessage = format.format(lastDate.getTime());
        }

        return lastReceivedMessage;
    }

    /**
     * Returns a boolean if this data was received or sent.
     * @return true if data was received, otherwise false.
     */
    public boolean received() {
        return receiverStateMap.get(receivers.get(0).getId()).received();
    }

    /**
     * Calls resetSendingSate() on datastate for every receiver, if type is SENDING_FAILED.
     * <p/>
     * @return true if the datastate for one receiver is set to SENDING_FAILED, otherwise false.
     */
    public boolean resend() {
        boolean resend = false;
        for (Contact c : receivers) {
            DataState state = this.getState(c);
            if (state.getDataStateType() == DataState.Type.SENDING_FAILED) {
                state.resetSendingState();
                resend = true;
            }
        }
        return resend;
    }

    /**
     * Calls sendingFailed() on datastate for every receiver, if type is SENDING.
     */
    public void stopSending() {
        for (Contact c : receivers) {
            DataState state = this.getState(c);
            if (state.getDataStateType() == DataState.Type.SENDING) {
                state.sendingFailed();
            }
        }
    }

    /**
     * Checks if the datastate for every receiver is not set to SENDING_FAILED.
     * @return true, if the datastate for one receiver is set to SENDING_FAILED, otherwise false.
     */
    public boolean needsResend() {
        for (Contact c : receivers) {
            DataState state = this.getState(c);
            if (state.getDataStateType() == DataState.Type.SENDING_FAILED) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the datastate for every receiver is set to SENDING_SUCCESS.
     * @return true, if the datastate for every receiver is set to SENDING_SUCCESS, otherwise false.
     */
    public boolean sendingCompleted() {
        for (Contact c : receivers) {
            DataState state = this.getState(c);
            if (state.getDataStateType() != DataState.Type.SENDING_SUCCESS) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if the datastate for one receiver is set to SENDING.
     * @return true, if the datastate for one receiver is set to SENDING, otherwise false.
     */
    public void sendingStopped() {
        for (Contact c : receivers) {
            DataState state = this.getState(c);
            if (state.getDataStateType() == DataState.Type.SENDING) {
                state.sendingFailed();
            }
        }
    }

    /**
     * Checks if the datastate for one receiver is set to NOT_SENT.
     * @return true, if the datastate for one receiver is set to NOT_SENT, otherwise false.
     */
    public boolean wasNotSend() {
        for (Contact c : receivers) {
            DataState state = this.getState(c);
            if (state.getDataStateType() == DataState.Type.NOT_SENT) {
                return true;
            }
        }
        return false;

    }

    /**
     * Checks if given object is equal to this data.
     * @param other the object to compare with.
     * @return true if bothe are equal, otherwise false.
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (other.getClass() != getClass()) {
            return false;
        }
        if (!((Data) other).getText().equals(text)) {
            return false;
        }
        if (((Data) other).getTimestamp() == null) {
            if (timestamp != null) {
                return false;
            }
        } else if (!((Data) other).getTimestamp().equals(timestamp)) {
            return false;
        }
        if (((Data) other).getFile() == null) {
            if (file != null) {
                return false;
            }
        } else if (!((Data) other).getFile().equals(file)) {
            return false;
        }
        if (((Data) other).getSender() == null) {
            if (sender != null) {
                return false;
            }
        } else if (!((Data) other).getSender().equals(sender)) {
            return false;
        }
        if (!((Data) other).getReceivers().equals(receivers)) {
            return false;
        }
        return ((Data) other).getState().equals(getState());
    }

    /**
     * Sets the datastate hasmap.
     * @param stateReceiverMap the hashmap to set.
     */
    public void setReceiverStateMap(HashMap<Long, DataState> stateReceiverMap) {
        DataState lastState = null;
        for (Contact c : receivers) {
            if (stateReceiverMap.containsKey(c.getId())) {
                DataState curState = stateReceiverMap.get(c.getId());
                if (lastState == null) {
                    lastState = curState;
                } else if (curState.received() != lastState.received()) {
                    throw new IllegalArgumentException("Illegal type combination!");
                }
            } else {
                throw new IllegalArgumentException("The given receiver state map misses a contact!");
            }
        }

    this.receiverStateMap = stateReceiverMap;
    }
}
