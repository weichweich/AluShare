package edu.kit.tm.pseprak2.alushare.network;

import android.content.Context;
import android.os.Build;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.MockMessagingProtocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Albrecht Weiche
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class DataHandlerTest {
    MockMessagingProtocol mockMessProto;
    DataHandler dataHandler;

    DataHelper dataHelper;
    ContactHelper contactHelper;

    Context context = RuntimeEnvironment.application;

    private final List<Contact> receivers = new ArrayList<>();
    private final String cnid = "jadkfasdkfjö.onion:asidoiasd";
    private Contact self;

    @Before
    public void setup() {
        TestHelper.resetHelperFactory();
        contactHelper = HelperFactory.getContacHelper(context);
        contactHelper.setOwnNID(MockMessagingProtocol.ownTestNID);
        dataHelper = HelperFactory.getDataHelper(context);

        for (int i = 0; i < 10; i++) {
            Contact c = new Contact("er" + i + ".onion");
            contactHelper.insert(c);
            receivers.add(c);
        }

        mockMessProto = new MockMessagingProtocol(RuntimeEnvironment.application);
        dataHandler = (DataHandler) mockMessProto.getMessageListener();

        assertNotNull("Mock protocol not initialized", mockMessProto);
        assertNotNull("DataHandler not initialized", dataHandler);


        self = contactHelper.getSelf();
        assertNotNull("Self not initialized", self);
    }

    @Test
    public void testSendData() {

        String text = "test teste tdfakco,ekäovawgocrjkvmöklamf,vmc,bmäklmgskmgklfdmsgkm,cxväkgpkgq+rewpüo";
        Data insertedData = new Data(self, receivers, DataState.createStates(receivers, DataState.Type.NOT_SENT), text);
        insertedData.setNetworkChatID(cnid);
        dataHelper.insert(insertedData);

        assertEquals("Inserted and send data object should be equal!", insertedData, mockMessProto.lastSendingData);
    }

    @Test
    public void testDoNotSendData() {

        String cnid = "jafjkasdfhdsjfh.onion:232hjhadslj";

        String text = "test teste tdfakco,ekäovawgocrjkvmöklamf,vmc,bmäklmgskmgklfdmsgkm,cxväkgpkgq+rewpüo";
        Data receivedData = new Data(self, receivers, DataState.createStates(receivers, DataState.Type.SENDING_SUCCESS), text);
        receivedData.setNetworkChatID(cnid);

        dataHandler.messageReceived(receivedData);

        List<Data> dataList = dataHelper.getDataObjectsByNetworkChatID(cnid);
        assertTrue("The chat must have exactly one data object!", dataList.size() == 1);
        Data dbData = dataList.get(0);
        assertEquals("Inserted and received data should have the same id!", receivedData.getId(), dbData.getId());
        assertEquals("Inserted and received data should have the same text!", receivedData.getText(), dbData.getText());
        assertEquals("Inserted and received data should have the same sender!", receivedData.getSender(), dbData.getSender());
        assertEquals("Inserted and received data should have the same cnid!", receivedData.getNetworkChatID(), dbData.getNetworkChatID());
        assertEquals("Inserted and received data should have the same file!", receivedData.getFile(), dbData.getFile());
        assertEquals("Inserted and received data should have the same receivers!", receivedData.getReceivers(), dbData.getReceivers());
    }

    @Test
    public void testReceiveData() {

        // first data
        String text = "test teste tdfakco,ekäovawgocrjkvmöklamf,vmc,bmäklmgskmgklfdmsgkm,cxväkgpkgq+rewpüo";
        Data insertedData = new Data(self, receivers, DataState.createStates(receivers, DataState.Type.SENDING_SUCCESS), text);
        insertedData.setNetworkChatID(cnid);
        dataHelper.insert(insertedData);
        // second data
        text = "test teste tdfakco,ekäovawgocrjkvmöklamf,vmc,bmäklmgskmgklfdmsgkm,cxväkgpkgq+rewpüo";
        Data insertedData2 = new Data(self, receivers, DataState.createStates(receivers, DataState.Type.SENDING_SUCCESS), text);
        insertedData2.setNetworkChatID(cnid);
        dataHelper.insert(insertedData2);

        assertNull("No data should be send!", mockMessProto.lastSendingData);
    }

    @Test
    public void testUpdateDataToSending() {

        String text = "test teste tdfakco,ekäovawgocrjkvmöklamf,vmc,bmäklmgskmgklfdmsgkm,cxväkgpkgq+rewpüo";
        Data insertedData = new Data(self, receivers, DataState.createStates(receivers, DataState.Type.NOT_SENT), text);
        insertedData.setNetworkChatID(cnid);
        dataHelper.insert(insertedData);

        assertEquals("Inserted and send data object should be equal!", insertedData, mockMessProto.lastSendingData);

        for (Contact c : insertedData.getReceivers()) {
            DataState state = insertedData.getState(c);
            assertNotNull("Every receiver must have a state.", state);
            state.sendingStarted();
        }

        dataHelper.update(insertedData);
        assertEquals("After update, only one data should be send!", 1, mockMessProto.sendingDataCount());

        dataHelper.delete(insertedData);
        assertEquals("After remove, only one data should be send!", 1, mockMessProto.sendingDataCount());
    }

    @Test
    public void testFailedSending() {

        String text = "test teste tdfakco,ekäovawgocrjkvmöklamf,vmc,bmäklmgskmgklfdmsgkm,cxväkgpkgq+rewpüo";
        Data insertedData = new Data(self, receivers, DataState.createStates(receivers, DataState.Type.NOT_SENT), text);
        insertedData.setNetworkChatID(cnid);
        dataHelper.insert(insertedData);

        assertEquals("Inserted and send data object should be equal!", insertedData, mockMessProto.lastSendingData);

        for (Contact c : insertedData.getReceivers()) {
            DataState state = insertedData.getState(c);
            assertNotNull("Every receiver must have a state.", state);
            state.sendingStarted();
            state.sendingFailed();
        }

        dataHandler.messageSendFailed(insertedData);
        assertEquals("After sending failed, one data should be send!", 1, mockMessProto.sendingDataCount());
    }

    @Test
    public void testSendingSuccess() {

        String text = "test teste tdfakco,ekäovawgocrjkvmöklamf,vmc,bmäklmgskmgklfdmsgkm,cxväkgpkgq+rewpüo";
        Data insertedData = new Data(self, receivers, DataState.createStates(receivers, DataState.Type.NOT_SENT), text);
        insertedData.setNetworkChatID(cnid);
        dataHelper.insert(insertedData);

        assertEquals("Inserted and send data object should be equal!", insertedData, mockMessProto.lastSendingData);

        for (Contact c : insertedData.getReceivers()) {
            DataState state = insertedData.getState(c);
            assertNotNull("Every receiver must have a state.", state);
            state.sendingStarted();
            state.sendingFinished();
        }

        dataHandler.messageSendSuccess(insertedData);
        assertEquals("After sending was successful, one data should be send!", 1, mockMessProto.sendingDataCount());
    }

    @After
    public void teardown() {
        TestHelper.resetHelperFactory();
        contactHelper = null;
        dataHelper = null;

        dataHandler = null;
    }
}
