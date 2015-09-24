package edu.kit.tm.pseprak2.alushare.network.coding.decodestates;

/**
 * This state decodes the name of the received file.
 *
 * @author Albrecht Weiche
 */
public class FileNameDecoderState extends StringDecodeState{
    private static final String TAG = "FileNameDecoderState";

    /**
     * creates a new file name state
     *
     * @param decodeStateMachine the statemachine which will get the file name
     * @param fieldSize the size of the field
     */
    public FileNameDecoderState(DecodeStateMachine decodeStateMachine, int fieldSize) {
        super(decodeStateMachine, fieldSize);
    }

    @Override
    public void decodingDone(String decodedString) {
        decodeStateMachine.setFileName(decodedString);
    }
}
