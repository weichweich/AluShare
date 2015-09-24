package edu.kit.tm.pseprak2.alushare.network.coding.decodestates;

import java.io.IOException;
import java.io.OutputStream;

/**
 * this state decodes the file. It takes the OutputStream from the statemachine and writs all data
 * to it.
 *
 * @author Albrecht Weiche
 */
public class FileDecoderState extends DecoderState {
    private OutputStream fileOutputStream;

    /**
     * Creates a new <code>DecoderState</code> object.
     *
     * @param fieldSize the size of the field
     */
    public FileDecoderState(DecodeStateMachine decodeStateMachine, int fieldSize) {
        super(decodeStateMachine, fieldSize);
        this.fileOutputStream = decodeStateMachine.getFileStream();
    }

    @Override
    public void decode(byte[] buffer, int offset, int length) {
        try {
            fileOutputStream.write(buffer, offset, length);
        } catch (IOException e) {
            e.printStackTrace();
            decodeStateMachine.errorState();
        }
    }

    /**
     * this method is called when ever the state is left.
     */
    @Override
    public void onLeave() {
        try {
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            decodeStateMachine.errorState();
        }
    }
}
