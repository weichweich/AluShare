package edu.kit.tm.pseprak2.alushare.network.protocol;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.net.util.Base64;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.ptp.Identifier;
import edu.kit.tm.ptp.Message;
import edu.kit.tm.ptp.PTP;
import edu.kit.tm.ptp.ReceiveListenerAdapter;
import edu.kit.tm.ptp.SendListener;
import edu.kit.tm.ptp.examples.android.receive.TorManager;
import edu.kit.tm.ptp.utility.Constants;

/**
 * An implementation of the NetworkProtocol which sends and receives data through the tor network.
 *
 * @author Albrecht Weiche
 */
public class PTPProtocol extends NetworkProtocol {
    private static final String TAG = "PTPProtocol";

    private AtomicBoolean connecting = new AtomicBoolean(false);

    // Where hidden services data will be stored
    public static final String serviceHSDirectory = "TheHiddenService";

    private PTP ptp;

    /**
     * Initiates a new PTPProtocol. This protocol dispatches bytes over the tor network.
     *
     * @param appContext the current application context
     * @param protocolListener the protocol listener
     */
    public PTPProtocol(Context appContext, ProtocolListener protocolListener) {
        super(appContext, protocolListener);
    }

    @Override
    public boolean isConnected() {
        return ptp != null && ptp.getIdentifier() != null;
    }

    @Override
    public void connect() {
        if (ptp == null && !connecting.getAndSet(true)) {
            startTor();
        }
    }

    @Override
    public void disconnect() {
    }

    @Override
    public void stop() {
        this.disconnect();
        stopTor();
    }

    @Override
    public String getNetworkID() {
        if (ptp != null && ptp.getIdentifier() != null) {
            return ptp.getIdentifier().getTorAddress();
        } else {
            return null;
        }
    }

    @Override
    public void dispatch(byte[] buffer, String receiversNID, final SendCallback callback) {

        if (ptp == null) {
            callback.sendFailed();
        }
        Message msg = new Message(new String(Base64.encodeBase64(buffer)), new Identifier(receiversNID));

        ptp.sendMessage(msg, ProtocolConstants.PACKET_SEND_TIMEOUT, new SendListener() {
            @Override
            public void sendSuccess(Message message) {
                callback.sendSuccess();
            }

            @Override
            public void sendFail(Message message, FailState failState) {
                callback.sendFailed();
            }
        });
    }

    /**
     * Starts PTP thread. Called from {@link #startTor()}.
     *
     * @param workingDirectory
     * @param controlPort
     * @param socksPort
     * @param localPort
     */
    private void startPTP(String workingDirectory, int controlPort, int socksPort, int localPort) {
        Log.i(TAG, "Start PTP");
        final NetworkProtocol protocol = this;
        try {
            ptp = new PTP(workingDirectory, controlPort, socksPort, localPort, serviceHSDirectory);
            ptp.setListener(new ReceiveListenerAdapter() {

                @Override
                public void receivedMessage(Message message) {
                    byte[] messageBytes = Base64.decodeBase64(message.content);
                    protocol.received(messageBytes, message.identifier.getTorAddress());
                }
            });

            new Thread(new Runnable() {

                @Override
                public void run() {
                    try {
                        // Set up Hidden Service
                        ptp.reuseHiddenService();

                        connecting.set(false);
                        notifyConnectionProgress(100);
                        checkConnected();
                    } catch (IOException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }).start();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * Starts Tor using {@link TorManager}. Runs {@link #startPTP(String, int, int, int)} on success.
     * <p/>
     * In case a different Tor manager should be used (e.g., Orbot) - rewrite this.
     */
    private void startTor() {
        Log.i(TAG, "Start TOR");
        TorManager.start(context, new TorManager.Listener() {

            @Override
            public void success(final String message, int progress) {

                // Feedback for user
                Toast.makeText(context, context.getText(R.string.tor_running), Toast.LENGTH_SHORT).show();

                // Get Tor config options
                final String directory = TorManager.getWorkingDirectory(context.getFilesDir().getPath());
                final int start = message.indexOf(TorManager.delimiter) + 1;
                final int middle = message.indexOf(TorManager.delimiter, start) + 1;
                final int controlPort = Integer.valueOf(message.substring(start, middle - 1));
                final int socksPort = Integer.valueOf(message.substring(middle));

                // start PeerTorPeer
                startPTP(directory, controlPort, socksPort, Constants.anyport);
            }

            @Override
            public void update(String message, int progress) {
                // Feedback for user
                //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                notifyConnectionProgress(progress);
            }

            @Override
            public void failure(String message, int progress) {
                // Feedback for user
                //Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                connecting.set(false);
                notifyConnectionFailed();
            }
        });
    }

    /**
     * Stop Tor using {@link TorManager}.
     * <p/>
     * In case a different Tor manager should be used (e.g., Orbot) - rewrite this.
     */
    private synchronized void stopTor() {
        Log.i(TAG, "Stop TOR");
        TorManager.stop(context, new TorManager.Listener() {

            @Override
            public void success(String message, int progress) {
                // Feedback for user
                Toast.makeText(context, context.getText(R.string.tor_not_running), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void update(String message, int progress) {
                // Feedback for user
                // Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(String message, int progress) {
                // Feedback for user
                // Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
