package edu.kit.tm.pseprak2.alushare.network.coding;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

/**
 * @author Albrecht Weiche
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class StreamDataEncoderTest {
    private static final String TEXT_MESSAGE_1 = "Test test Test test dies ist ein TEST\n \n \nnach drei absetzen immer noch ein test!";
    private static final String ENCODED_TEXT_1 = "VGVzdCB0ZXN0IFRlc3QgdGVzdCBkaWVzIGlzdCBlaW4gVEVTVA0KIA0KIA0KbmFjaCBkcmVpIGFic2V0emVuIGltbWVyIG5vY2ggZWluIHRlc3Qh";
    private static final String FILE_PATH_1 = "src/test/resources/Datei-1";
    private List<Contact> receivers;
    private Contact self;
    private String cnid;

    @Before
    public void setup() {
        receivers = new ArrayList<Contact>();

        for (int i = 0; i < 25; i++) {
            Contact c = new Contact("er" + i + ".onion");
            HelperFactory.getContacHelper(RuntimeEnvironment.application).insert(c);
            receivers.add(c);
        }
        HelperFactory.getContacHelper(RuntimeEnvironment.application).setOwnNID("ichichich.onion");
        self = HelperFactory.getContacHelper(RuntimeEnvironment.application).getSelf();
        cnid = self.getNetworkingId() + ":djafjkdakadf";
    }

    @After
    public void teardown() {
        TestHelper.resetHelperFactory();
    }

    @Test
    public void encodeText() {
        HashMap<Long, DataState> receiverStateMap = DataState.createStates(receivers, DataState.Type.NOT_SENT);
        Data data = new Data(self, receivers, receiverStateMap, TEXT_MESSAGE_1);
        data.setNetworkChatID(cnid);

        assertNotNull("Data must not be null!", data);
        assertEquals("Data text must be equal to original text", data.getText(), TEXT_MESSAGE_1);

        DataEncoder encoder = new StreamDataEncoder(data);
        StreamDataDecoder decoder = new StreamDataDecoder(RuntimeEnvironment.application, "123456send.onion");
        byte[] encoded = new byte[encoder.available()];
        encoder.read(encoded, 0, encoder.available());

        decoder.write(encoded, 0, encoded.length);
        Data decodedData = decoder.getDecodedData();
        assertEquals("Text musst be euqal!", TEXT_MESSAGE_1, decodedData.getText());
        assertNull("File was expected to be null!", decodedData.getFile());
    }

    @Test
    public void encodeFile() {
        ASFile file = new ASFile(123, -1, FILE_PATH_1, "Bildbildö.jpg", false);
        HashMap<Long, DataState> receiverStateMap = DataState.createStates(receivers, DataState.Type.NOT_SENT);
        Data data = new Data(self, receivers, receiverStateMap, file);
        data.setNetworkChatID(cnid);

        if (file.exists()) {
            assertNotNull("Data must not be null!", data);
            assertTrue("File should exist!", file.exists());

            DataEncoder encoder = new StreamDataEncoder(data);

            final long aproxAvailable = data.getFile().length();
            String msg = "More than " + aproxAvailable + " bytes should be available (actually:" +
                    encoder.available() + ")";

            assertTrue(msg, encoder.available() >= aproxAvailable);
            assertEquals("Data ID must be equal to original ID", data.getId(), encoder.getID());

            byte[] readByte = new byte[encoder.available()];
            encoder.read(readByte, 0, readByte.length);

            assertEquals("After complete read stream should be closed", -1, encoder.available());
        }
    }

    @Test
    public void encodeTextFile() {
        ASFile file = new ASFile(123, -1, FILE_PATH_1, "Bildbildö.jpg", true);
        HashMap<Long, DataState> receiverStateMap = DataState.createStates(receivers, DataState.Type.NOT_SENT);
        Data data = new Data(self, receivers, receiverStateMap, TEXT_MESSAGE_1, file);
        data.setNetworkChatID(cnid);

        if (file.exists()) {
            assertNotNull("Data must not be null!", data);
            assertTrue("File should exist!", file.exists());
            assertEquals("Data text must be equal to original text", data.getText(), TEXT_MESSAGE_1);

            DataEncoder encoder = new StreamDataEncoder(data);

            byte[] readByte = new byte[encoder.available()];
            encoder.read(readByte, 0, readByte.length);
        }
    }

    @Test
    public void getDataTest() {
        ASFile file = new ASFile(123, -1, FILE_PATH_1, "Bildbildö.jpg", false);
        HashMap<Long, DataState> receiverStateMap = DataState.createStates(receivers, DataState.Type.NOT_SENT);
        Data data = new Data(self, receivers, receiverStateMap, TEXT_MESSAGE_1, file);
        data.setNetworkChatID(cnid);

        if (file.exists()) {
            assertNotNull("Data must not be null!", data);
            assertTrue("File should exist!", file.exists());
            assertEquals("Data text must be equal to original text", data.getText(), TEXT_MESSAGE_1);

            DataEncoder encoder = new StreamDataEncoder(data);

            byte[] readByte = new byte[encoder.available()];
            encoder.read(readByte, 0, readByte.length);

            assertEquals("The data should be equal!", data, encoder.getData());
        }
    }

    @Test
    public void readToMuch() {
        ASFile file = new ASFile(123, -1, FILE_PATH_1, "Bildbildö.jpg", false);
        HashMap<Long, DataState> receiverStateMap = DataState.createStates(receivers, DataState.Type.NOT_SENT);
        Data data = new Data(self, receivers, receiverStateMap, TEXT_MESSAGE_1, file);
        data.setNetworkChatID(cnid);

        if (file.exists()) {
            assertNotNull("Data must not be null!", data);
            assertTrue("File should exist!", file.exists());
            assertEquals("Data text must be equal to original text", data.getText(), TEXT_MESSAGE_1);

            DataEncoder encoder = new StreamDataEncoder(data);

            int available = encoder.available();
            byte[] encodedData = new byte[available];
            int read = encoder.read(encodedData, 0, available + 500);

            assertEquals("All available bytes should be wasRead and no more!", available, read);

            byte[] nullData = new byte[available];
            int secondRead = encoder.read(encodedData, 0, available + 500);

            assertEquals("if no data is left the encoder should return -1!", -1, secondRead);
        }
    }
}
