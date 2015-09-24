package edu.kit.tm.pseprak2.alushare.network.coding.decodestates;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolConstants;

/**
 * This abstract class provides the functionality to decode a string.
 * The subclass should determine what to do with the decoded string.
 *
 * @author albrecht weiche
 */
public abstract class StringDecodeState extends DecoderState {
    private static final String TAG = "StringDecodeState";

    private ByteArrayOutputStream stringBaos;

    /**
     * Creates a new string decode state.
     * @param decodeStateMachine the decode statemachine
     * @param fieldSize the size of the string field.
     */
    public StringDecodeState(DecodeStateMachine decodeStateMachine, int fieldSize) {
        super(decodeStateMachine, fieldSize);
    }

    @Override
    public void decode(byte[] buffer, int offset, int length) {

        if (stringBaos == null) {
            stringBaos = new ByteArrayOutputStream(fieldSize);
        }

        //byte[] decodedData = this.decodeBase64(buffer, offset, length);
        //stringBaos.write(decodedData, 0, decodedData.length);
        stringBaos.write(buffer, offset, length);
    }

    /**
     * this method is called when ever the state is left.
     */
    @Override
    public void onLeave() {
        // byte[] decodedData = this.flush();
        // stringBaos.write(decodedData, 0, decodedData.length);

        try {
            String decodedString = stringBaos.toString(ProtocolConstants.CHARSET);
            decodingDone(decodedString);
            stringBaos.close();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage() + "\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * This method is called from the string decoder state when the field was decoded.
     * @param decodedString the decoded string
     */
    protected abstract void decodingDone(String decodedString);
}
