package edu.kit.tm.pseprak2.alushare.network.coding;

import edu.kit.tm.pseprak2.alushare.model.Data;

/**
 * This abstract class specifies an interface for encoder <code>Data</code> objects to raw byte.
 *
 * @author Albrecht Weiche
 */
public abstract class DataEncoder {

    /**
     * the data object which should be encoded
     */
	protected Data data;

    /**
     * Creates a new data encoder
     *
     * @param data the data which will be encoded
     */
	public DataEncoder(Data data) {
		this.data = data;
	}

    /**
     * Writes <code>length</code> bytes into <code>buffer</code> starting at index <code>offset</code>
     *
     * @param buffer the buffer where the encoded data should be written
     * @param offset the position where the data should start in the buffer.
     * @param length the number of bytes which should be written.
     *
     * @return the number of bytes which are actually written.
     */
	public abstract int read(byte[] buffer, int offset, int length);

    /**
     * Returns the id of the data object which will be encoded.
     *
     * @return the id of the data object.
     */
    public abstract long getID();

    /**
     * Returns the number of bytes which are available and not yet wasRead.
     *
     * @return the number of remaining bytes.
     */
    public abstract int available();

    /**
     * Returns the data object to encode.
     *
     * @return a data object
     */
    public Data getData() {
        return data;
    }
}
