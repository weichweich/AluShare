package edu.kit.tm.pseprak2.alushare.network.coding;


import android.content.Context;

import org.apache.tools.ant.util.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolConstants;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * @author Albrecht Weiche
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class StreamDataDecoderTest {
    private static final Context context = RuntimeEnvironment.application;
    private static final String FILE_PATH_1 = "src/test/resources/Datei-0";
    private static final String CNID = "chatididididi.onion:sdkldjf";
    private static final String TEXT = "ajksdfhlkacnrloaiwecurkljsdchfjlkhsdcklfjhsdcjfkhs vhcfnsadhcfjkhsdcjkhf ttest test est dsaklfasdfh";
    private static final String SENDER_NID = "adfasdfadsfasdfasdfk.onion";

    private static byte[] intTo4Byte(int integer) {

        byte[] bytes = new byte[4];
        bytes[0] = (byte) (integer >> 24);

        integer = integer - (bytes[0] << 24);
        bytes[1] = (byte) (integer >> 16);

        integer = integer - (bytes[1] << 16);
        bytes[2] = (byte) (integer >> 8);

        integer = integer - (bytes[2] << 8);
        bytes[3] = (byte) (integer);

        return bytes;
    }

    private static byte[] message1() {
        byte[] message = TEXT.getBytes();
        byte[] cnidByte = StreamDataDecoderTest.CNID.getBytes();
        // 1 (flag) + 1 (Deliminator) + 4 (size) + ... + 1 (Deliminator) + 4 (size) + ...
        int messageLength = 6 + message.length + 5 + cnidByte.length;
        byte[] byteMessage = new byte[messageLength];

        int copyIndex = 0;

        byteMessage[copyIndex++] = 0; // flag

        byteMessage[copyIndex++] = ProtocolConstants.CHAT_ID; // chat id flag

        System.arraycopy(intTo4Byte(cnidByte.length), 0, byteMessage, copyIndex, 4); // chat id size
        copyIndex += 4;

        System.arraycopy(cnidByte, 0, byteMessage, copyIndex, cnidByte.length); // chat id
        copyIndex += cnidByte.length;

        byteMessage[copyIndex++] = ProtocolConstants.TEXT; // text flag

        System.arraycopy(intTo4Byte(message.length), 0, byteMessage, copyIndex, 4); // text size
        copyIndex += 4;

        System.arraycopy(message, 0, byteMessage, copyIndex, message.length); // text
        copyIndex += message.length;

        return byteMessage;
    }

    private DataDecoder decoder;

    @Before
    public void setup() {
        TestHelper.resetHelperFactory();
        DummyDataSet.initSelf(context);

        decoder = new StreamDataDecoder(context, SENDER_NID);
        assertNotNull("Self should be initialized", HelperFactory.getContacHelper(context).getSelf());
    }

    @Test
    public void testDecodeNothing() {
        assertNull("No Data written but data is not null", decoder.getDecodedData());
    }

    @Test
    public void encodeDecode() {
        Random rand = new Random();
        DummyDataSet.createDummyDataSet(context);

        List<Data> datas = HelperFactory.getDataHelper(context).getDataObjects(0, 4);
        for (Data data:datas) {
            if (data.getFile() != null && !data.getFile().exists()) {
                continue;
            }

            DataEncoder encoder = new StreamDataEncoder(data);
            StreamDataDecoder decoder = new StreamDataDecoder(context, data.getSender().getNetworkingId());

            while (encoder.available() > 0) {
                byte[] bytes = new byte[rand.nextInt(encoder.available() + 1)];
                int read = encoder.read(bytes, 0, bytes.length);
                decoder.write(bytes, 0, read);
            }
            Data decodedData = decoder.getDecodedData();

            assertNotNull("DecodedData should not be null", decodedData);
            assertEquals("Text should be equal!", data.getText(), decodedData.getText());
            assertEquals("sender should be equal!", data.getSender().getNetworkingId(), decodedData.getSender().getNetworkingId());
            assertEquals("Chat network identifier should be equal (Data)!", data.getNetworkChatID(), decodedData.getNetworkChatID());
            assertEquals("Chat network identifier should be equal (Decoder)!", data.getNetworkChatID(), decoder.getNetworkChatID());

            if (data.getFile() != null && data.getFile().exists()) {
                FileUtils fileUtils = FileUtils.getFileUtils();
                try {
                    Assert.assertTrue("Send and received data should have the same file!", fileUtils.contentEquals(data.getFile(), decodedData.getFile()));
                } catch (IOException e) {
                    Assert.assertTrue(e.getMessage(), false);
                }
            }
        }
    }

    @Test
    public void testDecodeRandomData() {
        byte[] randomBytes = new byte[1024 * 1024];
        new Random().nextBytes(randomBytes);

        decoder.write(randomBytes, 0, randomBytes.length);
        assertNull("Random data should cause return of null.", decoder.getDecodedData());
    }

    @After
    public void teardown() {
        TestHelper.resetHelperFactory();
    }
}