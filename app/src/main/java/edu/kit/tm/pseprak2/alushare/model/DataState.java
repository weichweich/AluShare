package edu.kit.tm.pseprak2.alushare.model;

import android.util.Log;

import java.util.HashMap;
import java.util.List;

public class DataState {
    public int getProgress() {
        return progress;
    }

    public enum Type {
        NOT_SENT,
        SENDING,
        SENDING_FAILED,
        SENDING_SUCCESS,
        RECEIVING,
        RECEIVED_READ,
        RECEIVED_UNREAD,
        UNKNOWN
    }

    private static final String TAG = "DataState";

    private Type dataStateType = Type.UNKNOWN;
    private int progress = -1;
    private final Contact receiver;

    public DataState(DataState state) {
        this.receiver = state.receiver;
        this.progress = state.progress;
        this.dataStateType = state.dataStateType;
    }

    public DataState(Contact receiver) {
        this.receiver = receiver;
    }

    public DataState(Contact receiver, Type stateType) {
        this.receiver = receiver;
        this.dataStateType = stateType;
        if (dataStateType == Type.RECEIVING || dataStateType == Type.SENDING) {
            progress = 0;
        }
    }

    public DataState(Contact receiver, Type stateType, int progress) {
        this.receiver = receiver;
        this.dataStateType = stateType;
        if (dataStateType == Type.RECEIVING || dataStateType == Type.SENDING) {
            this.progress = progress;
        }
    }

    public Type getDataStateType() {
        return dataStateType;
    }

    public boolean received() {
        return (dataStateType == Type.RECEIVED_READ
                || dataStateType == Type.RECEIVED_UNREAD
                || dataStateType == Type.RECEIVING);
    }

    /*
    public void dataWasRead() {
        if (dataStateType != Type.RECEIVED_UNREAD) {
            Log.w(TAG, "Only unread data can be wasRead! Current state is: " + dataStateType.name());
        } else {
            dataStateType = Type.RECEIVED_READ;
        }
    }
    */

    public void resetSendingState() {
        if (!this.received()) {
            dataStateType = Type.NOT_SENT;
        }
    }

    public void sendingStarted() {
        if (dataStateType != Type.NOT_SENT && dataStateType != Type.SENDING_FAILED) {
            Log.w(TAG, "Only data which should be send, but was not send, can be send! Current state is: " + dataStateType.name());
        } else {
            dataStateType = Type.SENDING;
            progress = 0;
        }
    }

    public void setProgress(int progress) {
        if (dataStateType != Type.SENDING && dataStateType != Type.RECEIVING) {
            Log.w(TAG, "Only sending or receiving data can make progress! Current state is: " + dataStateType.name());
        } else {
            this.progress = progress;
        }
    }

    /*
    public void addProgress(int deltaProgress) {
        this.setProgress(this.progress + deltaProgress);
    }
     */

    public Contact getReceiver() {
        return receiver;
    }

    public void sendingFinished() {
        if (dataStateType == Type.SENDING) {
            dataStateType = Type.SENDING_SUCCESS;
        } else {
            Log.w(TAG, "Only data which was send can be successful send! Current state is: " + dataStateType.name());
        }
    }

    public void sendingFailed() {
        if (dataStateType == Type.SENDING) {
            dataStateType = Type.SENDING_FAILED;
        } else {
            Log.w(TAG, "Only data which was send can fail to be send! Current state is: " + dataStateType.name());
        }
    }

    public boolean wasSendSuccessful() {
        return dataStateType == Type.SENDING_SUCCESS;
    }

    public boolean wasRead() {
        return dataStateType == Type.RECEIVED_READ;
    }

    public boolean wasFailedToSend() {
        return dataStateType == Type.SENDING_FAILED;
    }

    /*
    public boolean hasReceiveFinished() {
        if (dataStateType == Type.RECEIVING) {
            return false;
        } else if (dataStateType == Type.RECEIVED_READ || dataStateType == Type.RECEIVED_UNREAD) {
            return true;
        }
        Log.w(TAG, "Ask if received finished, but was send!");
        return false;
    }
    */

    public boolean setWasRead() {
        if (dataStateType == Type.RECEIVED_UNREAD) {
            dataStateType = Type.RECEIVED_READ;
            return true;
        } else if (dataStateType == Type.RECEIVING || dataStateType == Type.RECEIVED_READ) {
            return false;
        } else {
            Log.w(TAG, "Message was send, but tried to set wasRead!");
            return false;
        }
    }

    public static HashMap<Long, DataState> createStates(List<Contact> receivers, Type type) {
        HashMap<Long, DataState> map = new HashMap<Long, DataState>(receivers.size());
        for (Contact c: receivers) {
            map.put(c.getId(), new DataState(c, type));
        }
        return map;
    }
}

