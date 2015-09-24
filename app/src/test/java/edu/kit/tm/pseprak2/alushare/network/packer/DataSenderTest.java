package edu.kit.tm.pseprak2.alushare.network.packer;

import android.content.Context;
import android.os.Build;

import org.apache.tools.ant.util.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.DummyDataSet;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.MockMessagingProtocol;
import edu.kit.tm.pseprak2.alushare.network.protocol.MockNetProtocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author Albrecht Weiche
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class DataSenderTest {
    Context context;
    DataHelper dataHelper;
    ChatHelper chatHelper;
    ContactHelper contactHelper;

    MockMessagingProtocol mockMessagingProtocol;
    MockNetProtocol mockNetProtocol;
    Contact self;

    DataSender dataSender;

    @Before
    public void setup() {
        TestHelper.resetHelperFactory();
        context = RuntimeEnvironment.application;
        contactHelper = HelperFactory.getContacHelper(context);
        chatHelper = HelperFactory.getChatHelper(context);
        dataHelper = HelperFactory.getDataHelper(context);

        DummyDataSet.createDummyDataSet(context);

        self = contactHelper.getSelf();

        mockMessagingProtocol = new MockMessagingProtocol(context) {
            @Override
            public void packetReceived(Packet packet) {
                if (packet.sequenceNumber == 0) {
                    DataReceiver old = dataReceiverMap.get(keyForPacket(packet));
                    assertNull("packet with sequence number 0 should be the first packet", old);
                    dataReceiverMap.put(keyForPacket(packet), new DataReceiver(packet, this));
                } else {
                    DataReceiver receiver = dataReceiverMap.get(keyForPacket(packet));
                    receiver.packetReceived(packet);
                }
            }
        };
        mockNetProtocol = new MockNetProtocol(context);
        mockNetProtocol.setPacketListener(mockMessagingProtocol);
        mockNetProtocol.connect();

        assertNotNull("The self contact should be initialized.", self);
    }

    @Test
    public void testDataPacket() {
        List<Data> datas = dataHelper.getDataObjects();
        for (Data data : datas) {
            if (data.received() || (data.getFile() != null && !data.getFile().exists())) {
                continue;
            }
            data.getReceivers().clear();
            data.getReceivers().add(self);

            HashMap<Long, DataState> stateMap = DataState.createStates(data.getReceivers(), DataState.Type.NOT_SENT);
            data.setReceiverStateMap(stateMap);

            dataSender = new DataSender(data, mockMessagingProtocol, mockNetProtocol, context);
            dataSender.startSending(true);
            Data received = mockMessagingProtocol.lastReceivedData;

            assertEquals("Data object should be finished sending!", mockMessagingProtocol.lastFinishedDataID, data.getId());
            assertEquals("Data object should have state SENDING_SUCCESS!", DataState.Type.SENDING_SUCCESS, data.getState(self).getDataStateType());
            assertNotNull("A data object should be received!", received);
            assertEquals("Send and received data should have the same text!", data.getText(), received.getText());
            assertEquals("Send and received data should have the same sender!", data.getSender(), received.getSender());
            assertEquals("Send and received data should have the same cnid!", data.getNetworkChatID(), received.getNetworkChatID());
            if (data.getFile() != null && data.getFile().exists()) {
                FileUtils fileUtils = FileUtils.getFileUtils();
                try {
                    assertTrue("Send and received data should have the same file!", fileUtils.contentEquals(data.getFile(), received.getFile()));
                } catch (IOException e) {
                    assertTrue(e.getMessage(), false);
                }
            }
        }
    }

    @Test
    public void testDataSendProgress() {
        List<Data> datas = dataHelper.getDataObjects();
        for (Data data : datas) {
            if (data.received()) {
                continue;
            }
            data.getReceivers().clear();
            data.getReceivers().add(self);

            HashMap<Long, DataState> stateMap = DataState.createStates(data.getReceivers(), DataState.Type.NOT_SENT);
            data.setReceiverStateMap(stateMap);

            dataSender = new DataSender(data, mockMessagingProtocol, mockNetProtocol, context);
            dataSender.startSending(true);

            assertEquals("Data object should be finished sending!", mockMessagingProtocol.lastFinishedDataID, data.getId());
            assertEquals("Data object should have state SENDING_SUCCESS!", DataState.Type.SENDING_SUCCESS, data.getState(self).getDataStateType());
            assertEquals("The progress should reached 100%", 100, dataSender.getProgress());
        }
    }

    @Test
    public void testData() {
        List<Data> datas = dataHelper.getDataObjects();
        for (Data data : datas) {
            if (data.received()) {
                continue;
            }
            data.getReceivers().clear();
            data.getReceivers().add(self);

            HashMap<Long, DataState> stateMap = DataState.createStates(data.getReceivers(), DataState.Type.NOT_SENT);
            data.setReceiverStateMap(stateMap);

            dataSender = new DataSender(data, mockMessagingProtocol, mockNetProtocol, context);
            assertEquals("The data sender should hold the given data object", data, dataSender.getData());
        }
    }
}
