package edu.kit.tm.pseprak2.alushare.network.coding.decodestates;

/**
 * This class represents the state of the <code>StreamDataDecoder</code>. The subclasses of
 * <code>DecoderState</code> must implement the <code>decode()</code> method, which specifies the
 * way in which the written data is decoded and where it is saved.
 *
 * @author Albrecht Weiche
 */
public abstract class DecoderState {

    /**
     * The size of the current field.
     */
    protected int fieldSize;
    /**
     * the number of bytes read from the current field
     */
    protected int decodedBytesCount = 0;

    /**
     * the decodeStateMachine which will get the decoded data.
     */
    protected DecodeStateMachine decodeStateMachine;

    /**
     * Creates a new <code>DecoderState</code> object.
     */
    public DecoderState(DecodeStateMachine decodeStateMachine, int fieldSize) {
        this.fieldSize = fieldSize;
        this.decodeStateMachine = decodeStateMachine;
    }

    /**
     * Decodes the buffer beginning at offset and reading length bytes.
     *
     * @param buffer the byte buffer
     * @param offset the offset
     * @param length the length
     */
    protected abstract void decode(byte[] buffer, int offset, int length);

    /**
     * this method is called whene ever the state is left.
     */
    public abstract void onLeave();

    /**
     * Calls the abstract decode method and updates the decoded bytes count. Changes to field header
     * state if the end of the field is reached.
     *
     * @param buffer the buffer with the data
     * @param offset the offset where to start decoding
     * @param length the length of the field which should be decoded
     * @return the number of bytes actually decoded
     */
    public int write(byte[] buffer, int offset, int length) {
        int readCount = length;
        if (readCount > expectedByteCount()) {
            readCount = expectedByteCount();
        }
        this.decode(buffer, offset, readCount);
        countBytes(readCount);
        if (expectedByteCount() == 0) {
            decodeStateMachine.fieldHeaderState();
        }
        return readCount;
    }

    /**
     * Counts the bytes which are decoded and returns the number of bytes which are out of field bounds.
     *
     * @param bytesCount the number of bytes which are decoded
     */
    protected void countBytes(int bytesCount) {
        decodedBytesCount += bytesCount;
    }

    /**
     * the number of bytes which are needed to complete decoding the current field
     *
     * @return the number of bytes left
     */
    public int expectedByteCount() {
        return fieldSize - decodedBytesCount;
    }
}
