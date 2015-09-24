package edu.kit.tm.pseprak2.alushare.network.coding.decodestates;

/**
 * This state decodes text.
 *
 * @author Albrecht Weiche
 */
public class TextDecoderState extends StringDecodeState {

    /**
     * Creates a new text state object.
     *
     * @param decodeStateMachine the decode state machine
     * @param fieldSize the size of the text field
     */
    public TextDecoderState(DecodeStateMachine decodeStateMachine, int fieldSize) {
        super(decodeStateMachine, fieldSize);
    }

    @Override
    public void decodingDone(String decodedString) {
        decodeStateMachine.setText(decodedString);
    }
}
