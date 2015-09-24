package edu.kit.tm.pseprak2.alushare.model.helper;

import android.util.Log;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;

/**
 * Defines more method signatures that are needed for inserting/updating/deleting a {@link Data} object
 * into a data source.
 */
public abstract class DataHelper extends Helper<Data> {
    private static final String TAG = "DataHelper";

    /**
     * {@inheritDoc }
     */
    @Override
    public void update(Data data) {
        for (int i = 0; !ChatHelper.lockChat(data.getNetworkChatID()) && i < 3; i++) {
            Log.w(TAG, "Try " + i);
        }
        unsafeUpdate(data);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert(Data data) {
        for (int i = 0; !ChatHelper.lockChat(data.getNetworkChatID()) && i < 3; i++) {
            Log.w(TAG, "Try " + i);
        }
        unsafeInsert(data);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete(Data data) {
        for (int i = 0; !ChatHelper.lockChat(data.getNetworkChatID()) && i < 3; i++) {
            Log.w(TAG, "Try " + i);
        }
        unsafeDelete(data);
    }

    /**
     * Notifies all observers, that the given data object got updated in the data source and releases
     * the semaphore for the related chat.
     * @param data object that got updated.
     */
    public void finishedUpdatingData(Data data) {
        ChatHelper.releaseChat(data.getNetworkChatID());
        super.notifyUpdated(data);
    }

    /**
     * Notifies all observers, that the given data object got inserted into the data source and releases
     * the semaphore for the related chat.
     * @param data object that got updated.
     */
    public void finishedInsertingData(Data data) {
        ChatHelper.releaseChat(data.getNetworkChatID());
        super.notifyInserted(data);
    }

    /**
     * Notifies all observers, that the given data object got deleted from the data source and releases
     * the semaphore for the related chat.
     * @param data object that got updated.
     */
    public void finishedDeletingData(Data data) {
        ChatHelper.releaseChat(data.getNetworkChatID());
        super.notifyRemoved(data);
    }

    /**
     * Performs the updating of the given data object in a specific data source.
     * @param data object to update in the data source.
     */
    public abstract void unsafeUpdate(Data data);

    /**
     * Performs the inserting of the given data object into a specific data source.
     * @param data object to insert into the data source.
     */
    public abstract void unsafeInsert(Data data);

    /**
     * Performs the deleting of the given data object in a specific data source.
     * @param data object to delete from the data source.
     */
    public abstract void unsafeDelete(Data data);

    /**
     * Gets a data object from the data source by the given unique identifier.
     * @param dataID The unique identifier of the data object.
     * @return Data object that got loaded from the data source.
     */
    public abstract Data getDataByID(long dataID);

    /**
     * Gets a list of data objects from a data source by given DataState,Type and contact.
     * @param type Type of the data state.
     * @param contact Contact that is used as the key for the DataState-HashMap in a data object.
     * @return list of data objects.
     */
    public abstract List<Data> getDataObjectsByDataStateAndContact(DataState.Type type, Contact contact);

    /**
     * Gets a list of all data objects that are stored in the data source.
     * @return list of all data objects that are stored in the data source.
     */
    public abstract List<Data> getDataObjects();

    /**
     * Gets a list data objects with a given limit and offset.
     * Method is useful for pagination.
     * @param limit Amount of data objects to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @return list of data objects.
     */
    public abstract List<Data> getDataObjects(int limit, int offset);

    /**
     * Gets a list data objects with a given data state type.
     * @param stateType Type of the data state.
     * @return list of data objects.
     */
    public abstract List<Data> getDataObjectsByDataState(DataState.Type stateType);

    /**
     * Gets a list data objects with a given data state type, limit and offset.
     * @param limit Amount of data objects to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @param stateType Type of the data state.
     * @return list of data objects.
     */
    public abstract List<Data> getDataObjectsByDataState(DataState.Type stateType, int limit, int offset);

    /**
     * Gets a list data objects with a a given unique identifier for a chat.
     * @param networkChatID Unique identifier of the chat.
     * @return list of data objects.
     */
    public abstract List<Data> getDataObjectsByNetworkChatID(String networkChatID);

    /**
     * Gets a list data objects with a a given unique identifier for a chat, limit and offset.
     *
     * @param limit Amount of data objects to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @param networkChatID Unique identifier of the chat.
     * @return list of data objects.
     */
    public abstract List<Data> getDataObjectsByNetworkChatID(String networkChatID, int limit, int offset);

    /**
     * Deletes all data objects that are contained in a chat by the given unique chat identifier.
     *
     * @param networkChatID Unique identifier of the chat.
     */
    public abstract void deleteByNetworkChatID(String networkChatID);
}