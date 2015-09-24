package edu.kit.tm.pseprak2.alushare.network;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.Chat;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.Data;
import edu.kit.tm.pseprak2.alushare.model.DataState;
import edu.kit.tm.pseprak2.alushare.model.helper.AluObserver;
import edu.kit.tm.pseprak2.alushare.model.helper.ChatHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.ContactHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.DataHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.ChatActivity;
import edu.kit.tm.pseprak2.alushare.view.MainActivity;
import edu.kit.tm.pseprak2.alushare.view.SplashScreen;

/**
 * This class notifies the user about the current networking state and about received or failed
 * data.
 *
 * @author Albrecht Weiche
 */
public class NetworkingNotifier implements AluObserver<Data> {
    static final private String TAG = "NetworkingNotifier";
    static final private int SERVICE_STATE_NOTIFY_ID = 42;
    static final private long NOTIFICATION_DELAY = 1000l;

    /**
     * A boolean which indicates that new data was received and the user was not notified yet.
     */
    static private long lastDataReceiveTime = 0l;
    static private AtomicBoolean unhandledReceive = new AtomicBoolean(false);
    static private AtomicBoolean delayedNotification = new AtomicBoolean(false);

    private final NetworkingService networkingService;
    private final SharedPreferences preferences;


    /**
     * Initiates a new NetworkingNotifier. There should be always only one NetworkingNotifier object.
     *
     * @param aNetworkingService the networking service
     */
    public NetworkingNotifier(NetworkingService aNetworkingService) {
        this.networkingService = aNetworkingService;
        HelperFactory.getDataHelper(networkingService).addObserver(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(networkingService);
    }

    private void createNotification(boolean notify) {

        DataHelper dHelper = HelperFactory.getDataHelper(networkingService);
        ChatHelper cHelper = HelperFactory.getChatHelper(networkingService);
        ContactHelper coHelper = HelperFactory.getContacHelper(networkingService);

        List<Data> unreadData;
        List<Chat> unreadChat;
        Contact self = coHelper.getSelf();
        if (self == null) {
            unreadData = new ArrayList<>();
            unreadChat = new ArrayList<>();
        } else {
            unreadData = dHelper.getDataObjectsByDataStateAndContact(DataState.Type.RECEIVED_UNREAD, self);
            HashMap<String, Boolean> chatMap = new HashMap<>();
            unreadChat = new ArrayList<>();
            for (Data data :unreadData) {
                if (!chatMap.containsKey(data.getNetworkChatID())) {
                    Chat nextChat = cHelper.getChat(data.getNetworkChatID());
                    if (nextChat != null && !nextChat.isDeleted()) {
                        unreadChat.add(nextChat);
                    }
                    chatMap.put(data.getNetworkChatID(), true);
                }
            }
        }
        List<Data> failedData = dHelper.getDataObjectsByDataState(DataState.Type.SENDING_FAILED);
        List<Data> sendingData = dHelper.getDataObjectsByDataState(DataState.Type.SENDING);

        HashMap<String, Boolean> failedChatMap = new HashMap<>();
        List<Chat> failedChat = new ArrayList<>();
        for (Data data :failedData) {
            if (!failedChatMap.containsKey(data.getNetworkChatID())) {
                Chat nextChat = cHelper.getChat(data.getNetworkChatID());
                if (nextChat != null) {
                    failedChat.add(nextChat);
                }
                failedChatMap.put(data.getNetworkChatID(), true);
            }
        }

        boolean connected = networkingService.isProtocolConnected();

        int unreadDataCount = unreadData.size();
        int failedDataCount = failedData.size();

        int icon;
        String text;
        String title;
        Intent notificationIntent = null;

        // find right icon, text and title for notification
        if (failedDataCount > 0 && unreadDataCount == 0) {

            // icon:
            if (connected && sendingData.isEmpty()) {
                icon = R.drawable.ic_con_message_failed;
            } else if (connected) {
                icon = R.drawable.ic_con_message_failed_sending;
            } else {
                icon = R.drawable.ic_discon_message_failed;
            }

            // title:
            title = networkingService.getString(R.string.message_send_failed);
            if (failedChat.size() == 1) {
                text = failedChat.get(0).getTitle(networkingService);
                notificationIntent = intentToChatActivity(failedChat.get(0).getNetworkChatID());
            } else {
                text = failedDataCount + " " + networkingService.getString(R.string.multi_message_send_failed);
            }


        } else if (unreadDataCount > 0 && failedDataCount == 0) { // no failed messages

            // icon:
            if (connected && sendingData.isEmpty()) {

                icon = R.drawable.ic_con_message_received;
            } else if (connected) {
                icon = R.drawable.ic_con_message_received_sending;
            } else {
                icon = R.drawable.ic_discon_message_received;
            }

            // Text, Title intent:
            title = networkingService.getString(R.string.message_received);
            if (unreadDataCount == 1) {
                text = unreadMessageText(unreadData.get(0));
            } else {
                text = unreadDataCount + " " + networkingService.getString(R.string.multi_message_received);
            }
            if (unreadChat.size() == 1) {
                title = unreadMessageTitle(unreadChat.get(0));
                notificationIntent = intentToChatActivity(unreadChat.get(0).getNetworkChatID());
            }
        } else if (unreadDataCount > 0 && failedDataCount > 0) {
            if (connected) {
                icon = R.drawable.ic_con_message_failed;
            } else {
                icon = R.drawable.ic_discon_message_failed;
            }
            title = networkingService.getString(R.string.multi_message_received)
                    + " " + networkingService.getString(R.string.message_send_failed);

            text = unreadDataCount + " " + networkingService.getString(R.string.multi_received_failed_I)
                    + " " + failedDataCount + " " + networkingService.getString(R.string.multi_received_failed_II);


        } else {
            if (connected && !sendingData.isEmpty()) {
                icon = R.drawable.ic_con_sending;
                text = networkingService.getResources().getString(R.string.tor_running);
            } else if (connected) {
                icon = R.drawable.ic_con;
                text = networkingService.getString(R.string.tor_running);
            } else {
                icon = R.drawable.ic_discon;
                text = networkingService.getString(R.string.tor_not_running);
            }
            title = networkingService.getString(R.string.app_service_name);
        }
        if (notificationIntent == null) {
            if (coHelper.getSelf() == null) {
                notificationIntent = new Intent(networkingService, SplashScreen.class);
            } else {
                notificationIntent = new Intent(networkingService, MainActivity.class);
            }
            notificationIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }

        final PendingIntent pendingIntent = PendingIntent.getActivity(networkingService, 0, notificationIntent, 0);
        NotificationCompat.Builder notificationB = new NotificationCompat.Builder(networkingService)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(icon)
                .setContentIntent(pendingIntent);

        if (notify && unhandledReceive.getAndSet(false)) { // if new message was received, add sound, light and vibration!
            int defaultNot = 0;

            if (preferences.getBoolean("vibrate_notification", true))
                defaultNot |= Notification.DEFAULT_VIBRATE;

            if (preferences.getBoolean("sound_notification", true))
                defaultNot |= Notification.DEFAULT_SOUND;

            if (preferences.getBoolean("led_notification", true))
                defaultNot |= Notification.DEFAULT_LIGHTS;

            notificationB.setDefaults(defaultNot);
        }
        networkingService.startForeground(SERVICE_STATE_NOTIFY_ID, notificationB.build());
    }

    /**
     * Notifies the notifier that a new message was received. The notifier will play, vibrate and
     * flash the LED if the user wishes
     */
    public static void receivedNewMessage() {
        lastDataReceiveTime = System.currentTimeMillis();
        unhandledReceive.set(true);
    }

    /**
     * Updates the icon in the statusbar and plays a sound, vibrates and makes the LED flash if new
     * data was received and the fields are set to true.
     */
    void updateNotification() {
        if (!delayedNotification.get() && unhandledReceive.get()) {
            updateLater();
        } else {
            createNotification(false);
        }
    }

    private void updateLater() {
        if (!delayedNotification.getAndSet(true)) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "Diff: " + (System.currentTimeMillis() - lastDataReceiveTime));
                    while (System.currentTimeMillis() - lastDataReceiveTime < NOTIFICATION_DELAY) {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException ignored) {
                        }
                    }
                    Log.d(TAG, "Notify");
                    createNotification(true);
                    delayedNotification.getAndSet(false);
                }
            }).start();
        }
    }

    private String unreadMessageTitle(Chat chat) {
        return chat.getTitle(networkingService);
    }

    private String unreadMessageText(Data data) {
        String prevText = data.getText();
        if (prevText == null) {
            if (data.getFile() != null) {
                prevText = data.getFile().getASName();
            } else {
                prevText = "";
            }
        }
        return prevText;
    }

    private Intent intentToChatActivity(String ncid) {
        Intent notificationIntent = new Intent(networkingService, ChatActivity.class);
        String chatIDKey = networkingService.getResources().getString(R.string.CHAT_ID);
        notificationIntent.putExtra(chatIDKey, ncid);
        return notificationIntent;
    }

    @Override
    public void updated(Data data) {
        this.updateNotification();
    }

    @Override
    public void inserted(Data data) {
        this.updateNotification();
    }

    @Override
    public void removed(Data data) {
        this.updateNotification();
    }

}
