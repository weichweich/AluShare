package edu.kit.tm.pseprak2.alushare.network.protocol;

import android.content.Context;

import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Only a pseudo protocol. This class is not able to send or receive data. It should be used while
 * testing with the android emulator.
 *
 * @author Albrecht Weiche
 */
public class PseudoProtocol extends NetworkProtocol {
    private static final String TAG = "PseudoProtocol";

    private AtomicBoolean connecting = new AtomicBoolean();
    private boolean connected = false;
    private String testNID = null;

    /**
     * Initiats a MessagingProtocol
     *
     * @param appContext the current application context
     */
    public PseudoProtocol(Context appContext, ProtocolListener protocolListener) {
        super(appContext, protocolListener);
        connecting.set(false);
    }

    @Override
    public boolean isConnected() {
        return connected;
    }

    @Override
    public void connect() {

        if (!connecting.getAndSet(true)) {
            testNID = "ownTest.onion";

            notifyConnectionProgress(100);
            connected = true;
            notifyConnected();
            connecting.set(false);
        }
    }

    @Override
    public void disconnect() {
        if (connected) {
            notifyDisconnected();
        }
        connected = false;
    }

    @Override
    public void stop() {
        this.disconnect();
    }

    @Override
    public String getNetworkID() {
        return testNID;
    }

    @Override
    protected void dispatch(byte[] buffer, String receiversNID, SendCallback callback) {
        if (receiversNID.equals(getNetworkID()) && isConnected()) {
            callback.sendSuccess();
            this.received(buffer, getNetworkID());
        } else {
            callback.sendFailed();
        }
    }
}
