package edu.kit.tm.pseprak2.alushare.model.helper;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Contact;

/**
 * Defines more method signatures that are needed for inserting/updating/deleting contacts
 * to a data source.
 */
public abstract class ContactHelper extends Helper<Contact> {

    /**
     * Gets a contact from the data source by given unique networking identifier.
     * @param networkingID unique networking identifier.
     * @return contact that contains all information stored in the data source.
     */
    public abstract Contact getContactByNetworkingID(String networkingID);

    /**
     * Gets a contact from the data source by given unique identifier.
     * @param contactID unique identifier.
     * @return contact that contains all information stored in the data source.
     */
    public abstract Contact getContactByID(long contactID);

    /**
     * Gets a list of all contacts that are stored in the data source.
     * @return list of all contacts.
     */
    public abstract List<Contact> getContacts();

    /**
     * Gets a list of all contacts that are stored in the data source with given limit and offset.
     * @param limit Amount of contact objects to fetch from the data source.
     * @param offset At which entry to begin in the data source.
     * @return list of all contacts.
     */
    public abstract List<Contact> getContacts(int limit, int offset);

    /**
     * Gets a list of all contacts that are in the specified chat identifier.
     * @param networkChatID unique chat identifier
     * @return list of all contacts that are in a specified chat.
     */
    public abstract List<Contact> getContactsByNetworkChatID(String networkChatID);

    /**
     * Gets a list contacts that sent the specified data with given unique identifier.
     * @param dataID unique data identifier
     * @return list of contacts.
     */
    public abstract List<Contact> getContactByData(long dataID);

    /**
     * Gets a contact that contains the own networking identifier and information.
     * @return own contact.
     */
    public abstract Contact getSelf();

    /**
     * Sets the own networking identifier and writes it to a file if not already exists.
     * @param newOwnNID the unique identifier to set.
     */
    public abstract void setOwnNID(String newOwnNID);

    /**
     * Checks if contact is member of any chat or not.
     * @param contactID unique identifier.
     * @return true if contact is member of any chat, otherwise false.
     */
    public abstract boolean isContactInAnyChat(long contactID);
}
