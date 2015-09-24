package edu.kit.tm.pseprak2.alushare.network.coding;

import android.content.Context;
import android.util.Log;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.coding.decodestates.ChatIDState;
import edu.kit.tm.pseprak2.alushare.network.coding.decodestates.DecodeStateMachine;
import edu.kit.tm.pseprak2.alushare.network.coding.decodestates.DecoderState;
import edu.kit.tm.pseprak2.alushare.network.coding.decodestates.FieldHeaderState;
import edu.kit.tm.pseprak2.alushare.network.coding.decodestates.FileDecoderState;
import edu.kit.tm.pseprak2.alushare.network.coding.decodestates.FileNameDecoderState;
import edu.kit.tm.pseprak2.alushare.network.coding.decodestates.ReceiverDecoderState;
import edu.kit.tm.pseprak2.alushare.network.coding.decodestates.TextDecoderState;

/**
 * The <code>StreamDataDecoder</code> class provides the ability to decode a stream of data.
 *
 * @author Albrecht Weiche
 */
public class StreamDataDecoder extends DataDecoder implements DecodeStateMachine {
    private static final String TAG = "StreamDataDecoder";

    // fields for Data:
    private final ArrayList<String> receivers;
    private ASFile decodedFile;
    private String messageText;

    /**
     * the flag of the data stream
     */
    protected byte flag;
    private String chatID;

    // fields for state machine
    private DecoderState curState;
    private int writtenBytesCount;

    /**
     * Creates a new StreamDataDecoder object.
     */
    public StreamDataDecoder(Context context, String senderNID) {
        super(context, senderNID);
        this.receivers = new ArrayList<>();
    }


    @Override
    public void write(byte[] buffer, int offset, int length) {
        if ((buffer.length - offset) == 0 || length == 0) {
            return;
        }
        if (writtenBytesCount == 0) {
            // get flags:
            this.flag = buffer[offset];
            offset += 1;
            length -= 1;
            fieldHeaderState();
        }

        while (length > 0 && curState != null) {
            int usedBytes = curState.write(buffer, offset, length);
            length -= usedBytes;
            offset += usedBytes;
            writtenBytesCount += usedBytes;
        }
    }

    private void changeState(DecoderState state) {
        if (curState != null) {
            curState.onLeave();
        }
        curState = state;
    }

    @Override
    public void errorState() {
        if (decodedFile != null) {
            final boolean delete = decodedFile.delete();
            if (!delete) {
                Log.e(TAG, "File could not be deleted.");
            }
        }
        curState = null;
    }

    @Override
    public void textState(int size) {
        changeState(new TextDecoderState(this, size));
    }

    @Override
    public void chatIDState(int size) {
        changeState(new ChatIDState(this, size));
    }

    @Override
    public void receiverState(int size) {
        changeState(new ReceiverDecoderState(this, size));
    }

    @Override
    public void fileNameState(int size) {
        changeState(new FileNameDecoderState(this, size));
    }

    @Override
    public void fileState(int size) {
        changeState(new FileDecoderState(this, size));
    }

    @Override
    public void fieldHeaderState() {
        changeState(new FieldHeaderState(this));
    }

    @Override
    public void setText(String text) {
        this.messageText = text;
    }

    @Override
    public void setFileName(String fileName) {
        if (decodedFile == null) {
            decodedFile = new ASFile(context, fileName);
        } else {
            decodedFile.setASName(fileName);
        }
    }

    @Override
    public OutputStream getFileStream() {
        if (decodedFile == null) {
            decodedFile = new ASFile(context, "");
        }
        try {
            return new FileOutputStream(decodedFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getNetworkChatID() {
        return chatID;
    }

    @Override
    public void setChatID(String cid) {
        this.chatID = cid;
    }

    @Override
    public Data getDecodedData() {
        Data data;
        Contact cSender = CodingHelper.nidToContact(senderNID, this.context);

        ChatHelper caHelper = HelperFactory.getChatHelper(context);
        Chat chat = caHelper.getChat(chatID);

        List<Contact> receiverList;

        if (chat == null) {
            receiverList = new ArrayList<>();
            receiverList.add(HelperFactory.getContacHelper(context).getSelf());
        } else {
            receiverList = chat.getReceivers();
        }

        final HashMap<Long, DataState> receiverStateMap = DataState.createStates(receiverList, DataState.Type.RECEIVED_UNREAD);

        if (this.messageText == null && this.decodedFile == null) {
            Log.e(TAG, "Empty message received!");
            return null;
        } else if (this.messageText == null || "".equals(messageText)) {
            data = new Data(cSender, receiverList, receiverStateMap, decodedFile);
        } else if (this.decodedFile == null) {
            data = new Data(cSender, receiverList, receiverStateMap, messageText);
        } else {
            data = new Data(cSender, receiverList, receiverStateMap, messageText, decodedFile);
        }
        data.setNetworkChatID(chatID);
        return data;
    }

    @Override
    public void addReceiver(String nid) {
        receivers.add(nid);
    }
}
