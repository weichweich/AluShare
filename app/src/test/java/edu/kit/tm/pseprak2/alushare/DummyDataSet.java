package edu.kit.tm.pseprak2.alushare;

import android.content.Context;

import org.robolectric.shadows.ShadowLog;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.MockMessagingProtocol;

/**
 * Created by dominik on 09.08.15.
 */
public class DummyDataSet {

    private static final int TEST_FILE_COUNT = 4;
    private static final String FILES_DIR = "src/test/resources/";

    private static int AMOUNT_CHATS = 3;
    private static int AMOUNT_MESSAGES_IN_CHAT = 10;
    private static int MAX_CONTACTS_IN_CHAT = 5;
    private static final int AMOUNT_CONTACTS = AMOUNT_CHATS * MAX_CONTACTS_IN_CHAT;

    private static final Random RANDOM = new Random(3284789234798l);

    public static List<Chat> createDummyDataSet(Context context, int amountChats, int amountMessagesInChat, int maxContactsInChat) {
        AMOUNT_CHATS = amountChats;
        AMOUNT_MESSAGES_IN_CHAT = amountMessagesInChat;
        MAX_CONTACTS_IN_CHAT = maxContactsInChat;
        return createDummyDataSet(context);
    }

    public static void copyDataSet(String filename) throws IllegalArgumentException{ // /test/resources/databses/<filename>
        String targetPath = TestHelper.getDatabasePath();
        ShadowLog.stream = System.out;
        File dbFile = new File(TestHelper.getTestResourcePath().concat("databases/" + filename));
        if (!dbFile.exists()) {
            System.out.print("File does not exist");
        }

        File targetDir = new File(targetPath);
        if(!targetDir.exists()) {
            targetDir.mkdirs();
        }


        File targetFile = new File(targetPath + "AluShareDataBase.db");
        if (targetFile.exists()) {
            targetFile.delete();
        }

        try {
            TestHelper.copyFileUsingStream(dbFile,targetFile);
        } catch (IOException e) {
            System.out.println("File copy failed");
        }
    }
    public static List<Chat> createDummyDataSet(Context context) {
        initSelf(context);

        ChatHelper chatHelper = HelperFactory.getChatHelper(context);

        List<Chat> chatList = generateChatList(context, AMOUNT_CHATS);
        for (Chat chat : chatList) {
            chat.addData(generateDataList(context, AMOUNT_MESSAGES_IN_CHAT, chat.getReceivers()));
        }

        for (Chat chat : chatList) {
            chatHelper.insert(chat);
        }
        return chatList;
    }

    public static List<Chat> generateChatList(Context context, int amountChats) {
        Contact selfContact = initSelf(context);

        List<Chat> chats = new ArrayList<>();

        for (int i = 0; i < amountChats; i++) {
            List<Contact> chatReceiver = randomContacts(context, RANDOM.nextInt(MAX_CONTACTS_IN_CHAT - 2) + 2);

            chatReceiver.add(selfContact);
            Contact admin;
            if (i % 3 == 0) {
                admin = selfContact;
            } else {
                admin = pickRandomContact(chatReceiver);
            }
            Chat chat = new Chat(admin.getNetworkingId() + ":" + randomString(), "Chat-Title-" + i, chatReceiver);

            chats.add(chat);
        }
        return chats;
    }

    public static List<Contact> generateContactList(Context context, int amountContacts) {
        List<Contact> contacts = new ArrayList<>();
        for (int i = 0; i < amountContacts; i++) {
            Contact contact = new Contact(randomString() + ".onion");
            contacts.add(contact);
            HelperFactory.getContacHelper(context).insert(contact);
        }
        return contacts;
    }

    public static List<Data> generateDataList(Context context, int amountMessagesInChat, List<Contact> receiver) {
        Contact selfContact = initSelf(context);

        List<Data> dataList = new ArrayList<>();

        for (int i = 0; i < amountMessagesInChat; i++) {
            Contact sender = pickRandomContact(receiver);
            if (i % 6 == 0) {
                sender = selfContact;
            }
            List<Contact> dataReceiver = new ArrayList<>(receiver);
            dataReceiver.remove(sender);

            if (i < 4) {
                ASFile asFile = new ASFile(-1, -1, FILES_DIR + "Datei-" + ((i % TEST_FILE_COUNT) + 1), "Datei-" + ((i % TEST_FILE_COUNT) +1), RANDOM.nextBoolean());
                dataList.add(new Data(sender, dataReceiver, randomDataState(context, dataReceiver, sender), asFile));
            } else {
                String test = randomString(RANDOM.nextInt(21329));
                dataList.add(new Data(sender, dataReceiver, randomDataState(context, dataReceiver, sender), test));
            }
        }
        return dataList;
    }

    public static List<Contact> randomContacts(Context context, int size) {
        Contact selfContact = initSelf(context);
        int index = 0;
        boolean found = false;
        List<Contact> contacts = HelperFactory.getContacHelper(context).getContacts();

        // add more contacts if necessary
        if (contacts.size() <= size) {
            generateContactList(context, (size - contacts.size()) * 2 + 10);
            contacts = HelperFactory.getContacHelper(context).getContacts();
        }

        // remove self
        while (!found && index < contacts.size()) {
            if (contacts.get(index).getNetworkingId().equals(selfContact.getNetworkingId())) {
                contacts.remove(index);
                found = true;
            }
            index++;
        }

        // remove contacts to match size
        while (contacts.size() > size) {
            contacts.remove(RANDOM.nextInt(contacts.size()));
        }

        return contacts;
    }

    public static Contact pickRandomContact(List<Contact> receivers) {
        return receivers.get(RANDOM.nextInt(receivers.size()));
    }

    public static HashMap<Long, DataState> randomDataState(Context context, List<Contact> receiver, Contact sender) {
        Contact selfContact = initSelf(context);


        if (sender.equals(selfContact)) {
            return DataState.createStates(receiver, DataState.Type.SENDING_SUCCESS);
        }
        return DataState.createStates(receiver, DataState.Type.RECEIVED_READ);
    }

    public static String randomString(int length) {
        String text = "";
        while (text.length() < length) {
            text += randomString();
        }
        return text;
    }

    public static String randomString() {
        SecureRandom random = new SecureRandom();
        return new BigInteger(130, random).toString(32);
    }

    public static Contact initSelf(Context context) {
        ContactHelper contactHelper = HelperFactory.getContacHelper(context);
        if (contactHelper.getSelf() == null) {
            contactHelper.setOwnNID(MockMessagingProtocol.ownTestNID);
        }
        return contactHelper.getSelf();
    }

    public static void generateChat(String chatId, Context context){
        Contact sender = initSelf(context);

        Contact contact1 = new Contact("Receiver1");
        Contact contact2 = new Contact("Receiver2");
        List<Contact> cList = new ArrayList<>();
        cList.add(sender);
        cList.add(contact1);
        cList.add(contact2);
        Chat chat = new Chat(chatId, "TestChat", cList);
        HelperFactory.getChatHelper(context).insert(chat);
        List<Contact> receiverList = chat.getReceivers();
        final HashMap<Long, DataState> receiverStateMap = DataState.createStates(receiverList, DataState.Type.SENDING_FAILED);
        Data data = new Data(sender, receiverList, receiverStateMap, "newMessageSendingFailed");
        data.setNetworkChatID(chat.getNetworkChatID());
        chat.addData(data);
        HelperFactory.getDataHelper(context).insert(data);
    }
}
