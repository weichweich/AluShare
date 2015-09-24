package edu.kit.tm.pseprak2.alushare.network.coding.decodestates;

import java.io.OutputStream;

/**
 * The interface for the state machine used to decode a data object
 *
 * @author Albrecht Weiche
 */
public interface DecodeStateMachine {

    /**
     * Changes to the field header which is always 5 bytes big.
     */
    void fieldHeaderState();

    /**
     * changes to the file sate.
     * @param size the number of bytes which belong to the file field.
     */
    void fileState(int size);

    /**
     * Changes to the chat network identifier state
     * @param size the number of bytes which belong to the chat id field.
     */
    void chatIDState(int size);

    /**
     * Changes to the file name state.
     * @param size the number of bytes which belong to the file name field.
     */
    void fileNameState(int size);

    /**
     * Changes to the receiver state.
     * @param size the number of bytes which belong to the receiver field.
     */
    void receiverState(int size);

    /**
     * Changes to the text state.
     * @param size the number of bytes which belong to the text field.
     */
    void textState(int size);

    /**
     * changes to the error state. After switching to the error state all written bytes are ignored.
     */
    void errorState();

    /**
     * This method creates and returns a OutputStream where the received file can be stored.
     *
     * @return an outputstream where the file data can be written to.
     */
    OutputStream getFileStream();

    /**
     *
     * @param fileName the filename of the transmitted file
     */
    void setFileName(String fileName);

    /**
     * Adds a network identifier to the list of receivers.
     */
    void addReceiver(String decodedString);

    /**
     * Sets the text of the data object
     *
     * @param text the text of the data object
     */
    void setText(String text);

    /**
     * sets the network chat identifier of the received data object
     * @param cid the network chat identifier
     */
    void setChatID(String cid);
}