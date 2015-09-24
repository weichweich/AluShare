package edu.kit.tm.pseprak2.alushare.network;

import android.content.Context;

import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.AluObserver;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.ChatChangeListener;
import edu.kit.tm.pseprak2.alushare.network.protocol.MessagingProtocol;

/**
 * This class handles changes to chats.
 * Whenever a chat in the model changes, this class starts the task of notifying all receivers in the chat.
 * This class also notifies the model if it receives a change notification from an other device.
 *
 * @author Albrecht Weiche
 */
public class ChatHandler implements AluObserver<Chat>, ChatChangeListener {
    private final MessagingProtocol messagingProtocol;
    private final ChatHelper chatHelper;
    private final ContactHelper contactHelper;
    private final Context context;
    private final Contact self;

    /**
     * Initialises a new ChatHanlder with the given context and messaging protocol. Also starts
     * observing the chat model.
     * <p/>
     * This class needs the network identifier of the current device. If the own network identifier
     * is not jet set in the model, this method will throw a null pointer exception.
     *
     * @param context           the current application context
     * @param messagingProtocol the current messaging protocol
     */
    public ChatHandler(Context context, MessagingProtocol messagingProtocol) {
        this.context = context;
        chatHelper = HelperFactory.getChatHelper(context);
        contactHelper = HelperFactory.getContacHelper(context);
        chatHelper.addObserver(this);

        this.messagingProtocol = messagingProtocol;
        self = HelperFactory.getContacHelper(context).getSelf();

    }

    @Override
    public void updated(Chat chat) {
        if (chat.isAdmin(self)) {
            messagingProtocol.sendUpdateChat(chat);
        }
    }

    @Override
    public void inserted(Chat chat) {
        if (chat.isAdmin(self) && chat.isGroupChat()) {
            messagingProtocol.sendCreateChat(chat);
        }
    }

    @Override
    public void removed(Chat chat) {
        messagingProtocol.sendDeleteChat(chat);
    }

    @Override
    public void receivedInsert(Chat chat) {
        chatHelper.insert(chat);
    }

    @Override
    public void receivedUpdate(Chat chat) {
        if (!containsSelf(chat)) {
            return;
        }
        if (!chat.isGroupChat()) {
            Chat oldChat = chatHelper.getChat(chat.getNetworkChatID());
            if (oldChat == null) {
                Contact self = contactHelper.getSelf();
                for (Contact c : chat.getReceivers()) {
                    if (!c.getNetworkingId().equals(self.getNetworkingId())) {
                        chat.setTitle(c.getName(context));
                    }
                }
            } else {
                chat.setTitle(oldChat.getTitle());
            }
        }
        chatHelper.update(chat);
    }

    @Override
    public void receivedDelete(Chat chat, String nid) {
        Chat localChat = chatHelper.getChat(chat.getNetworkChatID());
        if (chat.isAdmin(nid)) {
            if (localChat != null && localChat.isGroupChat()) {
                chatHelper.delete(chat);
            }
        } else {
            if (localChat != null && localChat.isGroupChat()) {
                chatHelper.removeReceiver(chat, contactHelper.getContactByNetworkingID(nid));
            }
        }

    }

    private boolean containsSelf(Chat chat) {
        for (Contact c: chat.getReceivers()) {
            if (c.getNetworkingId().equals(self.getNetworkingId())) {
                return true;
            }
        }
        return false;
    }
}