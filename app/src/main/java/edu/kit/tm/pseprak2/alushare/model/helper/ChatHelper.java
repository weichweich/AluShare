package edu.kit.tm.pseprak2.alushare.model.helper;

import android.util.Log;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;

/**
 * Defines more method signatures that are needed for inserting/updating/deleting a {@link Chat}
 * into a data source.
 */
public abstract class ChatHelper extends Helper<Chat> {
    private static final String TAG = "ChatHelper";
    private static ConcurrentHashMap<String, Semaphore> semaphoreMap = new ConcurrentHashMap<>();

    /**
     * Locks a semaphore for handling multi threading with insert/delete/update methods.
     *
     * @param key key for inserting the locked semaphore in a hashmap.
     * @return the locked semaphore.
     */
    public static boolean lockChat(String key) {
        Semaphore semaphore = new Semaphore(1);
        Semaphore oldSemaphore = semaphoreMap.putIfAbsent(key, semaphore);

        if (oldSemaphore != null) {
            semaphore = oldSemaphore;
        }
        //semaphore.tryAcquire(30, TimeUnit.SECONDS);
        try {
            semaphore.acquire();
            return true;
        } catch (InterruptedException ignored) {
            Log.w(TAG, "Could not acquire chat: " + key);
            return false;
        }
    }

    /**
     * Releases the locked semaphore for the given key.
     *
     * @param key key for getting the locked semaphore out of the hashmap.
     */
    public static void releaseChat(String key) {
        Semaphore semaphore = semaphoreMap.get(key);
        if (semaphore != null) {
            semaphore.release();
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void update(Chat chat) {
        for (int i = 0; !lockChat(chat.getNetworkChatID()) && i < 3; i++) {
            Log.w(TAG, "Try " + i);
        }
        unsafeUpdate(chat);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void insert(Chat chat) {
        for (int i = 0; !lockChat(chat.getNetworkChatID()) && i < 3; i++) {
            Log.w(TAG, "Try " + i);
        }
        unsafeInsert(chat);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void delete(Chat chat) {
        for (int i = 0; !lockChat(chat.getNetworkChatID()) && i < 3; i++) {
            Log.w(TAG, "Try " + i);
        }
        unsafeDelete(chat);

    }

    /**
     * Notifies all observers, that the given chat got updated in the data source and releases
     * the semaphore for the related chat.
     *
     * @param data object that got updated.
     */
    public void finishedUpdatingChat(Chat data) {
        releaseChat(data.getNetworkChatID());
        super.notifyUpdated(data);
    }

    /**
     * Notifies all observers, that the given chat got inserted into the data source and releases
     * the semaphore for the related chat.
     *
     * @param data object that got updated.
     */
    public void finishedInsertingChat(Chat data) {
        releaseChat(data.getNetworkChatID());
        super.notifyInserted(data);
    }

    /**
     * Notifies all observers, that the given chat got removed from the data source and releases
     * the semaphore for the related chat.
     *
     * @param data object that got updated.
     */
    public void finishedDeletingChat(Chat data) {
        releaseChat(data.getNetworkChatID());
        super.notifyRemoved(data);
    }

    /**
     * Performs the updating of the given chat in the data source.
     *
     * @param chat object to update in the data source.
     */
    public abstract void unsafeUpdate(Chat chat);

    /**
     * Performs the inserting of the given chat into a data source.
     *
     * @param chat object to insert into the data source.
     */
    public abstract void unsafeInsert(Chat chat);

    /**
     * Performs the deleting of the given chat from a data source.
     *
     * @param chat object to delete from the data source.
     */
    public abstract void unsafeDelete(Chat chat);

    /**
     * Gets a chat from the data source by the given unique identifier.
     *
     * @param NetworkID The unique identifier of the chat.
     * @return chat that got loaded from the data source.
     */
    public abstract Chat getChat(String NetworkID);

    /**
     * Gets a chat from the data source by the given unique identifier
     * but contains no data in data list.
     *
     * @param NetworkID The unique identifier of the chat.
     * @return chat that got loaded from the data source.
     */
    public abstract Chat getChatWithoutData(String NetworkID);

    /**
     * Gets a list of all chats that are stored in the data source.
     *
     * @return list of all chats that are stored in the data source.
     */
    public abstract List<Chat> getChats();

    /**
     * Gets a list of all chats that are stored in the data source.
     *
     * @param limit  Amount of chats to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @return list of all chats that are stored in the data source.
     */
    public abstract List<Chat> getChats(int limit, int offset);

    /**
     * Gets a list of chats that contain the given string in there title.
     *
     * @param title  the title to search for.
     * @param limit  Amount of chats to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @return
     */
    public abstract List<Chat> getChatsByTitle(String title, int limit, int offset);

    /**
     * Gets a list of chats where the given contact is a receiver.
     *
     * @param contactID unique identifier of a contact that is a receiver.
     * @return list of chats.
     */
    public abstract List<Chat> getChatsByContactID(long contactID);

    /**
     * Gets a list of chats where the given contact is a receiver.
     *
     * @param limit     Amount of chats to fetch from the data source.
     * @param offset    At which entry to begin in the data source.
     * @param contactID unique identifier of a contact that is a receiver.
     * @return list of chats.
     */
    public abstract List<Chat> getChatsByContactID(long contactID, int limit, int offset);

    /**
     * Checks on the data source side if the given contact is a receiver of the given chat.
     *
     * @param contact contact that should be a receiver.
     * @param chat    chat in which the contact should be a receiver.
     * @return true if the contact is a receiver of the chat, otherwise false.
     */
    public abstract boolean isContactInChat(Contact contact, Chat chat);

    /**
     * Checks on the data source side if the given chat got deleted before. Therefore it checks the
     * deleted flag.
     *
     * @param chat chat to check for if deleted flag is true.
     * @return true if the deleted flag is true, otherwise false.
     */
    public abstract boolean isDeleted(Chat chat);

    /**
     * Checks on the data source side if the given chat got deleted before. Therefore it checks the
     * deleted flag.
     *
     * @param networkId chat to check for if deleted flag is true.
     * @return true if the deleted flag is true, otherwise false.
     */
    public abstract boolean isDeleted(String networkId);

    /**
     * Removes the m-to-n relation between the given contact and the given chat.
     *
     * @param chat    chat that has a relation with the contact.
     * @param contact contact that has a relation with the chat.
     */
    public abstract void removeReceiver(Chat chat, Contact contact);
}
