package edu.kit.tm.pseprak2.alushare.network.coding.decodestates;

import android.util.Log;

import edu.kit.tm.pseprak2.alushare.network.coding.StreamDataDecoder;

/**
 * The state which decodes a chat ID.
 *
 * @author Albrecht Weiche
 */
public class ChatIDState extends StringDecodeState {
    private static final String TAG = "ChatIDState";

    /**
     * Creates a new chat id state
     * @param streamDecoder the StreamDataDecoder which will get the decoded chat id
     * @param fieldSize the size if the chat id field
     */
    public ChatIDState(StreamDataDecoder streamDecoder, int fieldSize) {
        super(streamDecoder, fieldSize);
    }

    @Override
    public void decodingDone(String decodedString) {
        decodeStateMachine.setChatID(decodedString);
    }
}
