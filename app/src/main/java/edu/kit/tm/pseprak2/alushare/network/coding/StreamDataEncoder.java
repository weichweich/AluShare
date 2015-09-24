package edu.kit.tm.pseprak2.alushare.network.coding;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolConstants;

/**
 * This class implements the DataEncoder. It encodes the text, chatID and filename and stores them
 * in to a ByteArrayInputStream. The file is loaded on demand from the disk.
 *
 * @author Albrecht Weiche
 */
public class StreamDataEncoder extends DataEncoder {
    private String TAG = "StreamDataEncoder";
    private int readIndex = 0;
    private FileInputStream fis;
    private ByteArrayInputStream dataStream;

    /**
     * Creates a StreamDataEncoder for the given data object.
     * @param data the data object which should be encoded.
     */
    public StreamDataEncoder(Data data) {
        super(data);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        baos.write(0); // FLAG

        /*for (Contact c : data.getReceiver()) { // CONTACTS receivers not needed. receiver list is send through PACKT_NEW_CHAT
            CodingHelper.encodedString(c.getNetworkingId(), baos, ProtocolConstants.RECEIVER);
        }*/
        CodingHelper.encodedString(data.getSender().getNetworkingId(), baos, ProtocolConstants.RECEIVER);

        CodingHelper.encodedString(data.getNetworkChatID(), baos, ProtocolConstants.CHAT_ID); // CHAT_ID

        CodingHelper.encodedString(data.getText(), baos, ProtocolConstants.TEXT); // TEXT

        if (data.getFile() != null && data.getFile().exists()) { // FILE HEADER/ NAME
            CodingHelper.encodedString(data.getFile().getASName(), baos, ProtocolConstants.FILENAME);

            encodeFileHeader(baos);

            try {
                fis = new FileInputStream(data.getFile());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        dataStream = new ByteArrayInputStream(baos.toByteArray());
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean encodeFileHeader(ByteArrayOutputStream baos) {

        byte[] encodedFileHeader = new byte[1 + Integer.SIZE / Byte.SIZE];
        encodedFileHeader[0] = ProtocolConstants.FILE;

        int available = (int) data.getFile().length();
        CodingHelper.intTo4Byte(available, encodedFileHeader, 1);

        baos.write(encodedFileHeader, 0, encodedFileHeader.length);

        return true;
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        int oldReadIndex = readIndex;
        int availableData = dataStream.available();

        if (availableData > 0) {
            int readSize = (length > availableData ? availableData : length);

            readIndex += dataStream.read(buffer, offset, readSize);
            offset += readSize;
            length -= readSize;
        }

        if (length != 0 && fis != null) {
            try {
                int availableFile = fis.available();
                int readSize = (length > availableFile ? availableFile : length);

                readIndex += fis.read(buffer, offset, readSize);
                if (fis.available() == 0) {
                    fis.close();
                }
            } catch (IOException e) {
                Log.d(TAG, e.getMessage());
            }
        }
        if (readIndex == oldReadIndex) {
            return -1;
        }
        return readIndex - oldReadIndex;
    }

    @Override
    public long getID() {
        return data.getId();
    }

    @Override
    public int available() {
        if (fis == null) {
            return dataStream.available();
        }
        try {
            return fis.available() + dataStream.available();
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        return -1;
    }
}
