package edu.kit.tm.pseprak2.alushare.network.protocol;

import android.content.Context;
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
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.packer.Packet;

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
public class NetworkProtocolTest {
    DataHelper dataHelper;
    ChatHelper chatHelper;
    ContactHelper contactHelper;
    NetworkProtocol networkProtocol;
    Packet received = null;

    boolean sendSuccess = false;
    boolean sendfailed = false;

    NetworkProtocol.SendCallback callback = new NetworkProtocol.SendCallback() {
        @Override
        public void sendSuccess() {
            sendSuccess = true;
        }

        @Override
        public void sendFailed() {
            sendfailed = true;
        }
    };

    @Before
    public void setup() {
        TestHelper.resetHelperFactory();

        Context context = RuntimeEnvironment.application;
        contactHelper = HelperFactory.getContacHelper(context);
        chatHelper = HelperFactory.getChatHelper(context);
        dataHelper = HelperFactory.getDataHelper(context);

        sendSuccess = false;
        sendfailed = false;

        networkProtocol = new MockNetProtocol(context);
        contactHelper.setOwnNID(MockMessagingProtocol.ownTestNID);
    }

    @Test
    public void testConnect() {
        networkProtocol.connect();

        assertNotNull("A network identifier should be created!", networkProtocol.getNetworkID());
        assertTrue("NetworkProtocol should be connected", networkProtocol.isConnected());
    }

    @Test
    public void testDisconnect() {
        networkProtocol.connect();

        assertNotNull("A network identifier should be created!", networkProtocol.getNetworkID());
        assertTrue("NetworkProtocol should be connected", networkProtocol.isConnected());

        networkProtocol.disconnect();
        assertTrue("NetworkProtocol should not be connected", !networkProtocol.isConnected());
    }

    @Test
    public void testConnectionLost() {
        networkProtocol.connect();

        assertNotNull("A network identifier should be created!", networkProtocol.getNetworkID());
        assertTrue("NetworkProtocol should be connected", networkProtocol.isConnected());

        networkProtocol.networkConnectionLost();
        assertTrue("NetworkProtocol should not be connected", !networkProtocol.isConnected());
    }

    @Test
    public void testConnectionRestored() {
        networkProtocol.connect();

        assertNotNull("A network identifier should be created!", networkProtocol.getNetworkID());
        assertTrue("NetworkProtocol should be connected", networkProtocol.isConnected());

        networkProtocol.networkConnectionLost();
        assertTrue("NetworkProtocol should not be connected", !networkProtocol.isConnected());

        networkProtocol.connect();
        assertTrue("NetworkProtocol should be reconnected", networkProtocol.isConnected());
    }

    @Test
    public void testChatID() {
        networkProtocol.connect();

        assertNotNull("A network identifier should be created!", networkProtocol.getNetworkID());
        assertTrue("NetworkProtocol should be connected", networkProtocol.isConnected());

        String cnid = networkProtocol.createNewNetworkChatID();
        String nid = networkProtocol.getNetworkID();

        assertNotNull("The chat network identifier must not be null!", cnid);
        String[] splitedCNID = cnid.split(":");

        assertTrue("First part if chat network identifier " +
                "should equal the network identifier", nid.equals(splitedCNID[0]));
    }

    @Test
    public void testSendPacket() {
        networkProtocol.connect();
        String nid = networkProtocol.getNetworkID();

        assertNotNull("A network identifier should be created!", nid);
        assertTrue("NetworkProtocol should be connected", networkProtocol.isConnected());
        networkProtocol.setPacketListener(new NetworkProtocol.PacketListener() {
            @Override
            public void packetReceived(Packet packet) {
                received = packet;
            }
        });
        Packet sendPacket = new Packet(0, 1, 1, "test".getBytes(), nid, nid);
        networkProtocol.dispatchPacket(sendPacket, callback);

        assertTrue("Send should not fail!", !sendfailed);
        assertTrue("Send should be successful!", sendSuccess);
        assertNotNull("Received packet should not be null", received);
        assertEquals("Sequence number should be equal!", sendPacket.sequenceNumber, received.sequenceNumber);
        assertEquals("Sequence count should be equal!", sendPacket.packetCount, received.packetCount);
        assertEquals("Data should be equal!", new String(sendPacket.data), new String(received.data));
        assertEquals("Sender should be equal!", sendPacket.senderNID, received.senderNID);
        assertEquals("Receiver should be equal!", sendPacket.receiverNID, received.receiverNID);

    }

    @Test
    public void testSendWhileOffline() {
        networkProtocol.connect();
        String nid = networkProtocol.getNetworkID();

        assertNotNull("A network identifier should be created!", nid);
        assertTrue("NetworkProtocol should be connected", networkProtocol.isConnected());
        networkProtocol.setPacketListener(new NetworkProtocol.PacketListener() {
            @Override
            public void packetReceived(Packet packet) {
                received = packet;
            }
        });

        networkProtocol.networkConnectionLost();

        Packet sendPacket = new Packet(0, 1, 1, "test".getBytes(), nid, nid);
        networkProtocol.dispatchPacket(sendPacket, callback);

        networkProtocol.connect();

        assertTrue("Send should fail!", sendfailed);
        assertTrue("Send should not be successful!", !sendSuccess);
    }

    @Test
    public void testSendFailed() {
        networkProtocol.connect();
        String nid = networkProtocol.getNetworkID();

        assertNotNull("A network identifier should be created!", nid);
        assertTrue("NetworkProtocol should be connected", networkProtocol.isConnected());

        networkProtocol.setPacketListener(new NetworkProtocol.PacketListener() {
            @Override
            public void packetReceived(Packet packet) {
                received = packet;
            }
        });


        Packet sendPacket = new Packet(0, 1, 1, "test".getBytes(), nid, "wrongnid.onion");
        networkProtocol.dispatchPacket(sendPacket, callback);
        assertTrue("Send should fail!", sendfailed);
        assertTrue("Send should not be successful!", !sendSuccess);
        assertNull("Received packet should be null", received);

        for (int i = 1; i < ProtocolConstants.PACKET_MAX_SEND_TRIES; i++) {
            networkProtocol.dispatchPacket(sendPacket, callback);
            assertTrue("Send should fail!", sendfailed);
            assertTrue("Send should not be successful!", !sendSuccess);
            assertNull("Received packet should be null", received);
        }
    }

    @After
    public void teardown() {
        TestHelper.resetHelperFactory();
    }
}
