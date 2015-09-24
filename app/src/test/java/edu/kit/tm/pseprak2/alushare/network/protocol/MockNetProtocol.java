package edu.kit.tm.pseprak2.alushare.network.protocol;

import android.content.Context;

import java.util.ArrayList;

import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.packer.Packet;

/**
 * @author Alrecht Weiche
 */
public class MockNetProtocol extends NetworkProtocol {
    private boolean connected;
    private ContactHelper contactHelper;

    public ArrayList<Packet> lastDispatchedPackets = new ArrayList<>();
    private boolean safePacketHistory = false;

    public MockNetProtocol(Context context) {
        super(context, null);
        contactHelper = HelperFactory.getContacHelper(context);
    }

    public void setSafePacketHistory(boolean safePacketHistory) {
        this.safePacketHistory = safePacketHistory;
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void connect() {
        connected = true;
    }

    @Override
    public void disconnect() {
        connected = false;
    }

    @Override
    public void stop() {
        connected = false;
    }

    @Override
    public String getNetworkID() {
        return MockMessagingProtocol.ownTestNID;
    }

    @Override
    protected void dispatch(byte[] buffer, String receiversNID, SendCallback callback) {
        if (safePacketHistory) {
            lastDispatchedPackets.add(Packet.byteToPacket(buffer, receiversNID, contactHelper.getSelf().getNetworkingId()));
        }
        if (receiversNID.equals(MockMessagingProtocol.ownTestNID)) {
            received(buffer, receiversNID);
            callback.sendSuccess();
        } else {
            callback.sendFailed();
        }
    }
}
