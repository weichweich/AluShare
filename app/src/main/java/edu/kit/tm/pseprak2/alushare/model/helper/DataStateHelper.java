package edu.kit.tm.pseprak2.alushare.model.helper;

import java.util.HashMap;
import java.util.concurrent.Semaphore;

import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;

/**
 * Created by dominik on 23.08.15.
 */
public abstract class DataStateHelper {

    /*
    public void update(DataState dataState, Data data) {
        Semaphore semaphore = ChatHelper.lockChat(data.getNetworkChatID());

        unsafeUpdate(dataState, data);
        semaphore.release();
    }

    public void insert(DataState dataState, Data data) {
        Semaphore semaphore = ChatHelper.lockChat(data.getNetworkChatID());

        unsafeInsert(dataState, data);
        semaphore.release();
    }

    public void delete(DataState dataState, Data data) {
        Semaphore semaphore = ChatHelper.lockChat(data.getNetworkChatID());

        unsafeDelete(dataState, data);
        semaphore.release();
    }
    */
    public abstract HashMap<Long, DataState> getStateByDataID(long dataID);
    public abstract void unsafeInsert(DataState dataState, Data data);
    public abstract void unsafeUpdate(DataState dataState, Data data);
    public abstract void unsafeDelete(DataState dataState, Data data);
}
