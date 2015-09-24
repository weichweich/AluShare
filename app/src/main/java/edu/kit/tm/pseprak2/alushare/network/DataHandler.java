package edu.kit.tm.pseprak2.alushare.network;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.AluObserver;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.MessagingListener;
import edu.kit.tm.pseprak2.alushare.network.protocol.MessagingProtocol;

/**
 * The DataHandler handles data objects which where adds into the database or received with the set
 * messaging protocol.
 *
 * @author Albrecht Weiche
 */
public class DataHandler implements AluObserver<Data>, MessagingListener {
    private static final String TAG = "DataHandler";

    private final Context context;
    private final MessagingProtocol messagingProtocol;

    private final DataHelper dataHelper;
    private final ChatHelper chatHelper;

    /**
     * Initiates a new DataHandler object. After the constructor is called, the object will start
     * observing the data model immediately.
     *
     * @param context the current networking service
     * @param messagingProtocol the current messaging protocol
     */
    public DataHandler(Context context, MessagingProtocol messagingProtocol) {
        this.context = context;
        this.messagingProtocol = messagingProtocol;

        dataHelper = HelperFactory.getDataHelper(context);
        dataHelper.addObserver(this);
        chatHelper = HelperFactory.getChatHelper(context);

        List<Data> notSendData = dataHelper.getDataObjectsByDataState(DataState.Type.NOT_SENT);
        for (Data data:notSendData) {
            messagingProtocol.sendMessage(data);
        }
    }

    @Override
    public void messageSendSuccess(Data data) {
        dataHelper.update(data);
    }

    @Override
    public void messageSendFailed(Data data) {
        dataHelper.update(data);
    }

    @Override
    public void messageReceived(Data data) {


        if (data.getNetworkChatID() == null) { // if data is invalid
            Log.e(TAG, "data received without a chatNetworkID");
            return;
        }
        Chat chat = chatHelper.getChatWithoutData(data.getNetworkChatID());

        if (chat == null && data.getReceivers() != null && !chatHelper.isDeleted(data.getNetworkChatID())) {
        // if chat does not exist -> create a new one!
            List<Contact> receivers = new ArrayList<>(data.getReceivers());
            String chatTitle = data.getSender().getName(context);

            receivers.add(data.getSender());

            chat = new Chat(data.getNetworkChatID(), chatTitle, receivers);
            chat.addData(data);

            NetworkingNotifier.receivedNewMessage();
            chatHelper.insert(chat);
            Log.i(TAG, "Created new chat after data receive. Requesting chat info.");

            String chatNID = chat.getNetworkChatID();
            String senderNID =  data.getSender().getNetworkingId();

            messagingProtocol.sendRequestChatInformation(chatNID, senderNID);
        } else if (chat != null && chatHelper.isContactInChat(data.getSender(), chat) && !chat.isDeleted()) {
            NetworkingNotifier.receivedNewMessage();
            dataHelper.insert(data); // update chat in database, GUI will be notified over observers if necessary

            // Message for debugging.
            Log.i(TAG, "message received and inserted into chat! title: " + chat.getTitle()
                    + " CNID: " + chat.getNetworkChatID());
        } else {
            Log.i(TAG, "Received message from unknown or deleted chat. Sending delete chat packet.");
            messagingProtocol.sendDeleteChat(chat, data.getSender());
        }
    }

    @Override
    public void updated(Data data) {
        inserted(data);
    }

    @Override
    public void removed(Data data) {
        inserted(data);
    }

    @Override
    public void inserted(Data data) {
        if (data != null && data.wasNotSend()  && messagingProtocol != null) {
            messagingProtocol.sendMessage(data);
        }
    }
}