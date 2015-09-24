package edu.kit.tm.pseprak2.alushare.network.coding;

import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolConstants;

/**
 * This class contains methods for encoding and decoding ints, longs and strings to byte arrays.
 * It also contains methods do get contacts from networking identifiers.
 *
 * @author Albrecht Weiche
 */
public final class CodingHelper {

    /**
     * Takes the 4 bytes and converts them to an integer.
     *
     * @param buffer 4 bytes
     * @return the integer from the buffer.
     */
    public static int intFromBuffer(byte[] buffer) {
        return intFromBuffer(buffer, 0);
    }

    /**
     * Calculates the integer at the position <code>offset</code> in the buffer
     * @param buffer the offset of the integer
     * @param offset the buffer holding the integer.
     *
     * @return the calculated integer
     */
    public static int intFromBuffer(byte[] buffer, int offset) {
        int result = 0;
        for (int i = 0; i < Integer.SIZE/Byte.SIZE; i++) {
            result <<= Byte.SIZE;
            result |= (buffer[offset + i] & 0xFF);
        }
        return result;
    }

    /**
     * Writes the given integer in to the byte buffer.
     * @param integer the integer
     * @param buffer the buffer where the int should be written in. the length of the buffer must
     *               have at least 4 bytes.
     */
    public static void intTo4Byte(int integer, byte[] buffer) {
        CodingHelper.intTo4Byte(integer, buffer, 0);
    }

    /**
     * Writes the given integer in to the byte buffer.
     * @param integer the integer
     * @param buffer the buffer where the integer should be written in. This buffer must have at least
     *               offset + 4 entries.
     * @param offset the offset where the int should start.
     */
    public static void intTo4Byte(int integer, byte[] buffer, int offset) {
        for (int i = Integer.SIZE/Byte.SIZE - 1; i >= 0; i--) {
            buffer[offset + i] = (byte) (0xFF & integer);
            integer >>= Byte.SIZE;
        }
    }

    /**
     * Takes the 8 bytes and converts them to an integer.
     *
     * @param buffer 8 bytes containing the long
     * @return the long from the buffer.
     */
    public static long longFromBuffer(byte[] buffer) {
        return longFromBuffer(buffer, 0);
    }

    /**
     * Takes the 8 bytes and converts them to an integer.
     *
     * @param buffer offset + 8 bytes containing the long
     * @param offset the index where the long start in the buffer
     * @return the long from the buffer.
     */
    public static long longFromBuffer(byte[] buffer, int offset) {
        long result = 0;
        for (int i = 0; i < Long.SIZE/Byte.SIZE; i++) {
            result <<= Byte.SIZE;
            result |= (buffer[offset + i] & 0xFF);
        }
        return result;
    }

    /**
     * Writes the given long into the buffer
     *
     * @param integer the long to write into the buffer
     * @param buffer the buffer into which the long should be write. Must have at least 8 bytes
     */
    public static void longTo8Byte(long integer, byte[] buffer) {
        CodingHelper.longTo8Byte(integer, buffer, 0);
    }

    /**
     * Writes the given long into the buffer
     *
     * @param integer the long to write into the buffer
     * @param buffer the buffer into which the long should be write. Must have at least 8 bytes
     * @param offset
     */
    public static void longTo8Byte(long integer, byte[] buffer, int offset) {
        for (int i = Long.SIZE/Byte.SIZE - 1; i >= 0; i--) {
            buffer[offset + i] = (byte) (0xFF & integer);
            integer >>= Byte.SIZE;
        }
    }

    /**
     * This method encodes the given string with the charset specified in ProtocolConstants.
     * the first byte written is the deliminator which identifies the string. The next four bytes
     * are the length of the decoded string.
     *
     * @param string the string which should be encoded.
     * @param baos the outputstream in which the string should be written
     * @param deliminator the deliminator which identifies the string.
     * @return true on success otherwise false.
     */
    public static boolean encodedString(String string, ByteArrayOutputStream baos, byte deliminator) {
        byte[] encodedString;
        try {
            encodedString = string.getBytes(ProtocolConstants.CHARSET);
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        byte[] header = new byte[1 + Integer.SIZE / Byte.SIZE];
        header[0] = deliminator;
        CodingHelper.intTo4Byte(encodedString.length, header, 1);

        baos.write(header, 0, header.length);
        baos.write(encodedString, 0, encodedString.length);
        return true;
    }

    /**
     * Loads the corresponding contact to the networking id or adds a new contact to the database.
     *
     * @param nid the networking identifier of the requested contact
     * @return the contact with the networking identifier
     */
    public static Contact nidToContact(String nid, Context context) {
        ContactHelper contactHelper = HelperFactory.getContacHelper(context.getApplicationContext());
        Contact contact = contactHelper.getContactByNetworkingID(nid);
        if (contact == null) {
            contact = new Contact(nid);
            contactHelper.insert(contact);
        }
        return contact;
    }

    /**
     * Creates a list of contacts which correspond to the given networking id.
     *
     * @param receiverNIDs a list of networking ids.
     * @return a list of contacts matching the networking identifiers.
     */
    public static List<Contact> nidsToContacts(List<String> receiverNIDs, Context context) {
        List<Contact> contactList = new ArrayList<>(receiverNIDs.size());

        for (String nid: receiverNIDs) {
            contactList.add(nidToContact(nid, context));
        }
        return contactList;
    }
}