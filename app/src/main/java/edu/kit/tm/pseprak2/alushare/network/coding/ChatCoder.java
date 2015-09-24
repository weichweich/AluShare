package edu.kit.tm.pseprak2.alushare.network.coding;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolConstants;

/**
 * The ChatCoder encodes and decodes important information about a chat (Title, receiver, NChatID)
 *
 * @author Albrecht Weiche
 */
public class ChatCoder {
    private static final String TAG = "ChatCoder";

    /**
     * Encodes the given chat to an byte array.
     * @param chat the chat which should be encoded
     * @return the encoded chat as an byte array
     */
    public static byte[] chatToByte(Chat chat) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        for (Contact c : chat.getReceivers()) { // CONTACTS
            CodingHelper.encodedString(c.getNetworkingId(), baos, ProtocolConstants.RECEIVER);
        }

        CodingHelper.encodedString(chat.getNetworkChatID(), baos, ProtocolConstants.CHAT_ID);
        CodingHelper.encodedString(chat.getTitle(), baos, ProtocolConstants.CHAT_TITLE);
        byte[] bytes = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    /**
     * Decodes a chat from a byte array.
     * @param buffer the bytebuffer holding the information about the chat.
     * @param context the current application context
     * @return the decoded chat
     */
    public static Chat byteToChat(byte[] buffer, Context context) {
        String chatTitle = null;
        String cnid = null;
        List<String> receiverNIDs = new ArrayList<>();

        int index = 0;
        while (index < buffer.length) {
            byte identifier = buffer[index++];
            int size = CodingHelper.intFromBuffer(buffer, index);
            index += 4;
            switch (identifier) {
                case ProtocolConstants.CHAT_ID:
                    try {
                        cnid = new String(buffer, index, size, ProtocolConstants.CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Invalid cnid! " + e.getMessage());
                    }
                    break;
                case ProtocolConstants.RECEIVER:
                    try {
                        String receiver = new String(buffer, index, size, ProtocolConstants.CHARSET);
                        receiverNIDs.add(receiver);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Invalid receiver! " + e.getMessage());
                    }
                    break;
                case ProtocolConstants.CHAT_TITLE:
                    try {
                        chatTitle = new String(buffer, index, size, ProtocolConstants.CHARSET);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Invalid chat title! " + e.getMessage());
                    }
                    break;
                default:
            }
            index += size;
        }
        if (cnid == null || chatTitle == null || receiverNIDs.isEmpty()) {
            Log.e(TAG, "Invalid chat packet!!");
        }
        return new Chat(cnid, chatTitle, CodingHelper.nidsToContacts(receiverNIDs, context));
    }
}
