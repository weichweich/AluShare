package edu.kit.tm.pseprak2.alushare.network.coding.decoderstates;

import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.network.coding.CodingHelper;
import edu.kit.tm.pseprak2.alushare.network.coding.StreamDataDecoder;
import edu.kit.tm.pseprak2.alushare.network.coding.decodestates.FieldHeaderState;
import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolConstants;

import static junit.framework.Assert.assertEquals;

/**
 * @author Albrecht Weiche
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk= Build.VERSION_CODES.LOLLIPOP)
public class FieldHeaderStateTest {
    enum DecoderStates {
        FILENAME, FILE, TEXT, RECEIVER, FIELDHEADER, ERROR, CHATID
    }


    private FieldHeaderState state;
    private StreamDataDecoder decoder;

    private DecoderStates currentState;
    private int fieldSize;

    @Before
    public void setup() {
        decoder = new StreamDataDecoder(RuntimeEnvironment.application, "123456send.onion") {
            @Override
            public void fileNameState(int size) {
                currentState = DecoderStates.FILENAME;
                fieldSize = size;
            }

            @Override
            public void fileState(int size) {
                currentState = DecoderStates.FILE;
                fieldSize = size;
            }

            @Override
            public void errorState() {
                currentState = DecoderStates.ERROR;
            }

            @Override
            public void fieldHeaderState() {
                currentState = DecoderStates.FIELDHEADER;
            }

            @Override
            public void textState(int size) {
                currentState = DecoderStates.TEXT;
                fieldSize = size;
            }

            @Override
            public void receiverState(int size) {
                currentState = DecoderStates.RECEIVER;
                fieldSize = size;
            }

            @Override
            public void chatIDState(int size) {
                currentState = DecoderStates.CHATID;
                fieldSize = size;
            }
        };

        state = new FieldHeaderState(decoder);

        currentState = DecoderStates.FIELDHEADER;
        fieldSize = -1;
    }

    @Test
    public void testBitByBitTextHeader() {

        final int length = 14512;
        final byte[] testHeader = new byte[5];

        testHeader[0] = ProtocolConstants.TEXT;
        CodingHelper.intTo4Byte(length, testHeader, 1);

        for (int i = 0; i < testHeader.length; i++) {
            state.decode(testHeader, i, 1);
        }

        assertEquals("length should be ", length, fieldSize);
        assertEquals("Should switched to text state", DecoderStates.TEXT, currentState);
    }

    @Test
    public void testAllOnceTextHeader() {

        final int length = 2345;
        final byte[] testHeader = new byte[5];

        testHeader[0] = ProtocolConstants.TEXT;
        CodingHelper.intTo4Byte(length, testHeader, 1);

        state.decode(testHeader, 0, testHeader.length);

        assertEquals("length should be ", length, fieldSize);
        assertEquals("Should switched to text state", DecoderStates.TEXT, currentState);
    }

    @Test
    public void testBitByBitFileHeader() {

        final int length = 2;
        final byte[] testHeader = new byte[5];

        testHeader[0] = ProtocolConstants.FILE;
        CodingHelper.intTo4Byte(length, testHeader, 1);

        for (int i = 0; i < testHeader.length; i++) {
            state.decode(testHeader, i, 1);
        }

        assertEquals("length should be ", length, fieldSize);
        assertEquals("Should switched to text state", DecoderStates.FILE, currentState);
    }

    @Test
    public void testAllOnceFileHeader() {

        final int length = 67;
        final byte[] testHeader = new byte[5];

        testHeader[0] = ProtocolConstants.FILE;
        CodingHelper.intTo4Byte(length, testHeader, 1);

        state.decode(testHeader, 0, testHeader.length);

        assertEquals("length should be ", length, fieldSize);
        assertEquals("Should switched to text state", DecoderStates.FILE, currentState);
    }

    @Test
    public void testBitByBitFileNameHeader() {

        final int length = 98;
        final byte[] testHeader = new byte[5];

        testHeader[0] = ProtocolConstants.FILENAME;
        CodingHelper.intTo4Byte(length, testHeader, 1);

        for (int i = 0; i < testHeader.length; i++) {
            state.decode(testHeader, i, 1);
        }

        assertEquals("length should be ", length, fieldSize);
        assertEquals("Should switched to text state", DecoderStates.FILENAME, currentState);
    }

    @Test
    public void testAllOnceFileNameHeader() {

        final int length = 2345;
        final byte[] testHeader = new byte[5];

        testHeader[0] = ProtocolConstants.FILENAME;
        CodingHelper.intTo4Byte(length, testHeader, 1);

        state.decode(testHeader, 0, testHeader.length);

        assertEquals("length should be ", length, fieldSize);
        assertEquals("Should switched to text state", DecoderStates.FILENAME, currentState);
    }

    @Test
    public void testBitByBitChatIDHeader() {

        final int length = 33;
        final byte[] testHeader = new byte[5];

        testHeader[0] = ProtocolConstants.CHAT_ID;
        CodingHelper.intTo4Byte(length, testHeader, 1);

        for (int i = 0; i < testHeader.length; i++) {
            state.decode(testHeader, i, 1);
        }

        assertEquals("length should be ", length, fieldSize);
        assertEquals("Should switched to text state", DecoderStates.CHATID, currentState);
    }

    @Test
    public void testAllOnceChatIDHeader() {

        final int length = 666;
        final byte[] testHeader = new byte[5];

        testHeader[0] = ProtocolConstants.CHAT_ID;
        CodingHelper.intTo4Byte(length, testHeader, 1);

        state.decode(testHeader, 0, testHeader.length);

        assertEquals("length should be ", length, fieldSize);
        assertEquals("Should switched to text state", DecoderStates.CHATID, currentState);
    }

    @Test
    public void testBitByBitReceiverHeader() {

        final int length = 1;
        final byte[] testHeader = new byte[5];

        testHeader[0] = ProtocolConstants.RECEIVER;
        CodingHelper.intTo4Byte(length, testHeader, 1);

        for (int i = 0; i < testHeader.length; i++) {
            state.decode(testHeader, i, 1);
        }

        assertEquals("length should be ", length, fieldSize);
        assertEquals("Should switched to text state", DecoderStates.RECEIVER, currentState);
    }

    @Test
    public void testAllOnceReceiverHeader() {

        final int length = 2;
        final byte[] testHeader = new byte[5];

        testHeader[0] = ProtocolConstants.RECEIVER;
        CodingHelper.intTo4Byte(length, testHeader, 1);

        state.decode(testHeader, 0, testHeader.length);

        assertEquals("length should be ", length, fieldSize);
        assertEquals("Should switched to text state", DecoderStates.RECEIVER, currentState);
    }

    @Test
    public void testErrorFieldHeader() {
        final int length = 2;
        final byte[] testHeader = new byte[5];

        testHeader[0] = '1';
        CodingHelper.intTo4Byte(length, testHeader, 1);

        state.decode(testHeader, 0, testHeader.length);

        assertEquals("Should switched to text state", DecoderStates.ERROR, currentState);
    }

    @After
    public void teardown() {
        TestHelper.resetHelperFactory();
    }
}
