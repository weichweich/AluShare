package edu.kit.tm.pseprak2.alushare.network;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.MessagingProtocol;
import edu.kit.tm.pseprak2.alushare.network.protocol.NetworkProtocol;
import edu.kit.tm.pseprak2.alushare.network.protocol.PTPProtocol;
import edu.kit.tm.pseprak2.alushare.network.protocol.ProtocolListener;
import edu.kit.tm.pseprak2.alushare.network.protocol.PseudoProtocol;

/**
 * This class starts and stops the networking module. It provides information about the current
 * network state.
 * This class also ensures that all data objects are in a proper state when the module is setup.
 *
 * @author Albrecht Weiche
 */
public class NetworkingService extends Service implements ProtocolListener {
    private static final String TAG = "NetworkingService";

    /**
     * The time the service waits until it retries to connect.
     */
    private static final int TRY_CONNECT_INTERVAL = 30 * 1000;

    // Binder given to clients
    private final IBinder mBinder = new NetworkBinder();

    private static boolean running = false;

    private MessagingProtocol messagingProtocol;
    private static NetworkProtocol networkProtocol;
    private NetworkingNotifier networkingNotifier;

    private NetworkingServiceConnectionListener nscListener;

    private boolean notifiedCreatedNID = false;

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class NetworkBinder extends Binder {
        public NetworkingService getService() {
            // Return this instance of NetworkingService so clients can call public methods
            return NetworkingService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    /**
     * Restarts the
     */
    public void restartNetwork() {
        networkProtocol.connect();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        this.networkingNotifier = new NetworkingNotifier(this);
        this.networkingNotifier.updateNotification();

        networkProtocol = new PTPProtocol(this, this);

        initMessagingProtocol();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;

        // Connect to network if not already connected and online
        if (!networkProtocol.isConnected()) {
            networkProtocol.connect();
        }

        // start the messaging protocol and check if there are old data objects which hab an invalid
        // state
        initMessagingProtocol();

        return START_STICKY; // tell Android to restart this services should it be killed
    }

    @Override
    public void onDestroy() {
        running = false;

        // stop the protocol
        networkProtocol.stop();
        networkProtocol.setPacketListener(null);

        // set all data objects which are in sending state to send failed.
        DataHelper dataHelper = HelperFactory.getDataHelper(this);
        List<Data> unsendData = dataHelper.getDataObjectsByDataState(DataState.Type.SENDING);

        for (Data data : unsendData) {
            data.sendingStopped();
            dataHelper.update(data);
        }
    }

    /**
     * Returns true if the protocol is connected, otherwise false.
     *
     * @return the connection state of the network protocol.
     */
    public boolean isProtocolConnected() {
        return messagingProtocol != null && networkProtocol.isConnected();
    }

    @Override
    public void protocolConnected() {
        String netID = networkProtocol.getNetworkID();

        // check if wie have an network identifier. If we don't have one, we are not connected.
        if (netID == null) {
            Log.e(TAG, "Connected but NID is null!!");
            return;
        }

        // create own contact if not existing yet.
        ContactHelper cHelper = HelperFactory.getContacHelper(this.getApplicationContext());
        cHelper.setOwnNID(netID);

        // start the messaging protocol and check data states
        initMessagingProtocol();

        // notify the listener if necessary. Update the notification.
        networkingNotifier.updateNotification();
        if (nscListener != null) {
            nscListener.connected();
        }
    }

    @Override
    public void protocolConnectionFailed() {
        // notify about the failed connection:
        networkingNotifier.updateNotification();
        if (nscListener != null) {
            nscListener.connectionFailed();
        }
        if (networkProtocol == null) {
            return;
        }
        // reset state:
        notifiedCreatedNID = false;

        // try to connect later:
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(TRY_CONNECT_INTERVAL);
                } catch (InterruptedException ignored) {
                }
                if (networkProtocol != null) {
                    networkProtocol.connect();
                }
            }
        }).start();
    }

    @Override
    public void protocolConnectionProgress(int progress) {
        // notify about the progress only if there is a listener
        if (nscListener != null) {
            nscListener.connectionProgress(progress);

            // if we have got an network identifier, notify the listener:
            String netID = networkProtocol.getNetworkID();
            if (netID != null && !notifiedCreatedNID) {

                // create own contact if not existing yet.
                ContactHelper cHelper = HelperFactory.getContacHelper(this.getApplicationContext());
                cHelper.setOwnNID(netID);

                initMessagingProtocol();

                nscListener.networkingIDCreated(netID);
                notifiedCreatedNID = true;
            }
        }
    }

    @Override
    public void protocolDisconnected() {
        networkingNotifier.updateNotification();
    }

    /**
     * sets the current NetworkingServiceConnectionListener. There can only be one listener at a time.
     * @param nscListener the new listener.
     */
    public void setConnectionListener(NetworkingServiceConnectionListener nscListener) {
        this.nscListener = nscListener;
    }

    /**
     * Returns true if the service is running.
     * @return true if the service is running, otherwise false.
     */
    static public boolean isRunning() {
        return running;
    }

    /**
     * Returns a new network chat identifier. The identifier consist of the current netwokadress
     * and an unique identifier.
     *
     * @param context the current application context
     * @return a string which can be used to identify a chat in the alushare/tor network.
     */
    public static String getNewNetworkChatID(Context context) {
        if (!NetworkingService.isRunning()) {
            context.startService(new Intent(context, NetworkingService.class));
        }
        return networkProtocol.createNewNetworkChatID();
    }

    private void initMessagingProtocol() {
        Contact self = HelperFactory.getContacHelper(this).getSelf();
        if (messagingProtocol == null && self != null) {
            checkData();
            messagingProtocol = new MessagingProtocol(this, networkProtocol);
            networkProtocol.setPacketListener(messagingProtocol);
        }
    }

    /**
     * Changes all data objects from sending state to sending failed.
     */
    private void checkData() {
        DataHelper dataHelper = HelperFactory.getDataHelper(this);
        List<Data> unsendData = dataHelper.getDataObjectsByDataState(DataState.Type.SENDING);

        for (Data data : unsendData) {
            if (!data.sendingCompleted()) {
                data.stopSending();
                dataHelper.update(data);
            }
        }
    }
}
