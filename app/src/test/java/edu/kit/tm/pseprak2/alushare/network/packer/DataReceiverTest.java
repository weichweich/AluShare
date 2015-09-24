package edu.kit.tm.pseprak2.alushare.network.packer;

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
import java.util.HashMap;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.TestHelper;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.coding.DataEncoder;
import edu.kit.tm.pseprak2.alushare.network.protocol.MockMessagingProtocol;
import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolConstants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 *
 * @author Albrecht Weiche
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP)
public class DataReceiverTest {
    private ChatHelper chatHelper;
    private ContactHelper contactHelper;
    private MockMessagingProtocol mockMessagingProtocol;
    private Data sendData;
    private final String testString = "Test Test Test Test Test Test Test 7xTest";

    private List<Contact> receivers;
    private Contact self;

    @Before
    public void setup() {
        Context context = RuntimeEnvironment.application;
        contactHelper = HelperFactory.getContacHelper(context);
        chatHelper = HelperFactory.getChatHelper(context);

        contactHelper.setOwnNID(MockMessagingProtocol.ownTestNID);
        self = contactHelper.getSelf();
        mockMessagingProtocol = new MockMessagingProtocol(context);
        receivers = new ArrayList<Contact>();

        for (int i = 0; i < 25; i++) {
            Contact c = new Contact("er" + i + ".onion");
            HelperFactory.getContacHelper(RuntimeEnvironment.application).insert(c);
            receivers.add(c);
        }

        HashMap<Long, DataState> stateMap = DataState.createStates(receivers, DataState.Type.NOT_SENT);
        sendData = new Data(self, receivers, stateMap, testString);
        sendData.setNetworkChatID(self.getNetworkingId() + ":qiweurioqweu");
        sendData.setId(12);

        List<Contact> chatReceivers = new ArrayList<>(receivers);
        chatReceivers.add(self);
        Chat chat = new Chat(sendData.getNetworkChatID(), "TITEL!", chatReceivers);
        chatHelper.insert(chat);
    }

    @Test
    public void testReceived() {
        DataEncoder encoder = mockMessagingProtocol.getEncoder(sendData);

        assertTrue("One Packet...", encoder.available() < ProtocolConstants.PACKET_MAX_DATA_SIZE);
        byte[] byteData = new byte[encoder.available()];
        encoder.read(byteData, 0, byteData.length);

        String nid = self.getNetworkingId();
        Packet packet = new Packet(0, 1, sendData.getId(), byteData, nid, nid);

        new DataReceiver(packet, mockMessagingProtocol);

        Data received = mockMessagingProtocol.lastReceivedData;

        assertNotNull("Received data must not be null!", received);
        assertEquals("Send and received text should be equal!", testString, received.getText());
        assertEquals("Sender should be equal!", received.getSender().getNetworkingId(), sendData.getSender().getNetworkingId());
    }

    @Test
    public void testReceivedWrongID() {
        DataEncoder encoder = mockMessagingProtocol.getEncoder(sendData);

        assertTrue("One Packet...", encoder.available() < ProtocolConstants.PACKET_MAX_DATA_SIZE);
        byte[] byteData = new byte[encoder.available() / 2];
        encoder.read(byteData, 0, byteData.length);

        String nid = self.getNetworkingId();
        Packet packet = new Packet(0, 2, sendData.getId(), byteData, nid, nid);

        DataReceiver dataReceiver = new DataReceiver(packet, mockMessagingProtocol);


        encoder.read(byteData, 0, encoder.available());
        packet = new Packet(1, 2, sendData.getId() + 1, byteData, nid, nid);
        dataReceiver.packetReceived(packet);

        assertNull("Nothing should be received", mockMessagingProtocol.lastReceivedData);
    }

    @Test
    public void testReceivedWrongSequence() {
        DataEncoder encoder = mockMessagingProtocol.getEncoder(sendData);

        assertTrue("One Packet...", encoder.available() < ProtocolConstants.PACKET_MAX_DATA_SIZE);
        byte[] byteData = new byte[encoder.available() / 2];
        encoder.read(byteData, 0, byteData.length);

        String nid = self.getNetworkingId();
        Packet packet = new Packet(0, 2, sendData.getId(), byteData, nid, nid);

        DataReceiver dataReceiver = new DataReceiver(packet, mockMessagingProtocol);


        encoder.read(byteData, 0, encoder.available());
        packet = new Packet(0, 2, sendData.getId(), byteData, nid, nid);
        dataReceiver.packetReceived(packet);

        assertNull("Nothing should be received", mockMessagingProtocol.lastReceivedData);
    }

    @After
    public void teardown() {
        TestHelper.resetHelperFactory();
    }
}
