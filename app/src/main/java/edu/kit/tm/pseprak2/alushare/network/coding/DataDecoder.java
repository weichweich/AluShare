package edu.kit.tm.pseprak2.alushare.network.coding;

import android.content.Context;

import edu.kit.tm.pseprak2.alushare.model.Data;

/**
 * This Class defines an interface for decoding bytes to an Data object.
 *
 * @author Albrecht Weiche
 */
public abstract class DataDecoder {

    /**
     * the current application context.
     */
    protected Context context;

    /**
     * the network identifier of the sender of the byte stream
     */
    protected final String senderNID;

    /**
     * Initialises a DataDecoder object.
     *
     * @param context the application context.
     */
    public DataDecoder(Context context, String senderNID) {
        this.context = context;
        this.senderNID = senderNID;
    }

    /**
     * Returns the decoded data which was computed from the data which was written to the decoder.
     *
     * @return a <code>Data</code> object containing the decoded data, null if the written data was
     * invalid.
     */
    public abstract Data getDecodedData();

    /**
     * Writes the bytes to the <code>DataDecoder</code>.
     *
     * @param buffer the data which should be written to the <code>DataDecoder</code>.
     */
    public void write(byte[] buffer) {
        this.write(buffer, 0, buffer.length);
    }

    /**
     * Writes the bytes from index to index + length to the <code>DataDecoder</code>.
     *
     * @param buffer the data which should be written to the <code>DataDecoder</code>.
     * @param offset the index where the <code>DataDecoder</code> starts reading the buffer
     * @param length the number of bytes the <code>DataDecoder</code> reads from the buffer.
     */
    public abstract void write(byte[] buffer, int offset, int length);

    /**
     * Returns the decoded chat networking identifier.
     *
     * @return a string representing the chat networking identifier.
     */
    public abstract String getNetworkChatID();
}
