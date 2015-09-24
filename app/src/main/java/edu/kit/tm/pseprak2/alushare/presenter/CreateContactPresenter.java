package edu.kit.tm.pseprak2.alushare.presenter;


import android.content.Context;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.model.Contact;

public class CreateContactPresenter {
    private Context context;
    private ContactHelper contactHelper;
    private ChatHelper chatHelper;
    public CreateContactPresenter() {

    }


    public void onTakeView(Context context) {
        if (context == null){
            throw new IllegalArgumentException();
        }
        this.context = context;
        contactHelper = HelperFactory.getContacHelper(context);
        chatHelper = HelperFactory.getChatHelper(context);
    }

    public void createContact(String networkAddress, String lookUpKey) {
        Contact contact = new Contact(lookUpKey, networkAddress);
        contactHelper.insert(contact);

    }

    public Contact getContactByNetworkadress(String networkAddress) {
        return contactHelper.getContactByNetworkingID(networkAddress);
    }

    public Contact getContactById(long id) {
        return contactHelper.getContactByID(id);
    }

    public void updateContact(Contact contact, String networkAdress, String lookUpKey) {
        contact.setLookUpKey(lookUpKey);
        contact.setNetworkingId(networkAdress);
        contactHelper.update(contact);
    }

    public void linkContact(Contact contact, String lookUpKey){
        contact.setLookUpKey(lookUpKey);
        List<Chat> chats = chatHelper.getChatsByContactID(contact.getId());
        for(Chat chat : chats) {
            if(!chat.isGroupChat()) {
                chat.setTitle(contact.getName(context));
                chatHelper.update(chat);
            }
        }
        contactHelper.update(contact);
    }

    public boolean contactInDatabase(long systemContactId){
        List<Contact> contactList = contactHelper.getContacts();
        for (Contact contact : contactList){
            if (contact.getSystemContactId(context) == systemContactId){
                return true;
            }
        }
        return false;
    }


}
