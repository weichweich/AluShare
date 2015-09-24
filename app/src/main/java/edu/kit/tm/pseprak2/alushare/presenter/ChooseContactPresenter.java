package edu.kit.tm.pseprak2.alushare.presenter;


import java.util.LinkedList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.NetworkingService;
import edu.kit.tm.pseprak2.alushare.view.ChooseContactActivity;
import edu.kit.tm.pseprak2.alushare.view.adapter.ChooseContactAdapter;

public class ChooseContactPresenter {
    private List<Contact> contactList;
    private ContactHelper contactHelper;
    private ChooseContactActivity view;
    private ChooseContactAdapter adapter;
    private ChatHelper chatHelper;

    public ChooseContactPresenter(ChooseContactActivity view, ChooseContactAdapter adapter) {
        if((view == null) || (adapter == null)){
            throw new IllegalArgumentException();
        }
        this.view = view;
        this.adapter = adapter;
        contactHelper = HelperFactory.getContacHelper(view.getApplication());
        contactList = contactHelper.getContacts();
        chatHelper = HelperFactory.getChatHelper(view.getApplicationContext());
    }

    public List<Contact> getContactList() {
        return this.contactList;
    }

    public List<Contact> getChoosenContacts() {
        LinkedList<Contact> choosenContacts = new LinkedList<>();
        for (Contact contact : adapter.getList()) {
            if (contact.isSelected()) {
                choosenContacts.add(contact);
            }
        }

        return choosenContacts;
    }

    public String startChat(String name) {
        String chatID = NetworkingService.getNewNetworkChatID(view.getApplicationContext());
        List<Contact> receivers = this.getChoosenContacts();
        receivers.add((contactHelper.getSelf()));
        Chat chat = new Chat(chatID, name, receivers);
        chatHelper.insert(chat);
        return chat.getNetworkChatID();
    }
}
