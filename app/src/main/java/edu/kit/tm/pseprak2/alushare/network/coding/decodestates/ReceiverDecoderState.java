package edu.kit.tm.pseprak2.alushare.network.coding.decodestates;

/**
 * This state decodes a receiver field.
 *
 * @author Albrecht Weiche
 */
public class ReceiverDecoderState extends StringDecodeState {

    /**
     * Creates a new receiver state
     * @param decodeStateMachine the statemachine which gets the receiver string
     * @param fieldSize the size of the receiver field
     */
    public ReceiverDecoderState(DecodeStateMachine decodeStateMachine, int fieldSize) {
        super(decodeStateMachine, fieldSize);
    }

    @Override
    public void decodingDone(String decodedString) {
        decodeStateMachine.addReceiver(decodedString);
    }
}
