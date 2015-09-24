package edu.kit.tm.pseprak2.alushare.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.ASFile;
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

/**
 * Represents the presenter of the chat. Receives new messages from the ChatActivity,
 * creates new data objects and adds them to the database. Uses AluObserver<Data> and AluObserver<Chat>
 * to receive Notifications of inserted, updated and removed data objects or chat object and updates
 * the view if relevant changes to the local chat have been made.
 *
 * @author Arthur Anselm
 */
public class ChatPresenter implements AluObserver<Data> {
    private static final int BUFFER_SIZE = 10 * 1024;
    private String TAG = "ChatPresenter";
    private ChatActivity view;
    private Context context;
    private Chat chat;
    private Contact sender;
    private DataHelper dataHelper;
    private ContactHelper contactHelper;
    //Field cant be local because the observable holds just weak references.
    //If the field was local, the object would have been destroyed. The class won't receive any updates
    @SuppressWarnings("FieldCanBeLocal")
    private AluObserver<Chat> chatObserver;
    private int iUpdated;
    private int iRemoved;

    /**
     * The constructor of this class. Initializes all attributes and fetches the chat with the delivered
     * chatID. Invokes showDataList in the chatActivity to display old messages.
     *
     * @param view   the view of this chatPresenter
     * @param chatId the chatId of the chat
     */
    public ChatPresenter(ChatActivity view, String chatId) {
        if (view == null || chatId == null) {
            throw new NullPointerException("Parameters aren't allowed to be null.");
        }
        this.view = view;
        this.context = view.getApplicationContext();
        this.dataHelper = HelperFactory.getDataHelper(context);
        this.contactHelper = HelperFactory.getContacHelper(context);
        ChatHelper chatHelper = HelperFactory.getChatHelper(context);
        this.sender = contactHelper.getSelf();
        this.chat = chatHelper.getChat(chatId);
        if (chat != null && chat.getDataObjects() != null) {
            Collections.reverse(chat.getDataObjects());
            view.showDataList(chat.getDataObjects());
        } else {
            startMainActivity();
        }
        dataHelper.addObserver(this);
        chatObserver = getChatObserver();
        chatHelper.addObserver(chatObserver);
    }

    /**
     * Invokes the MainActivity. Mostly done after invalid states in the chatPresenter/chatActivity.
     */
    public void startMainActivity() {
        Intent mainActivity = new Intent(view, MainActivity.class);
        mainActivity.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        view.startActivity(mainActivity);
    }

    /**
     * Invokes deleteSender on the receiver list of the local chat and returns the result.
     *
     * @return the new list of receivers
     */
    public List<Contact> getReceivers() {
        return deleteSender(chat.getReceivers());
    }

    /**
     * Deletes the contact lookupkey and updates the database.
     *
     * @param contactId the contactId of the contact
     */
    public void updateContactNotFound(long contactId) {
        Contact contact = contactHelper.getContactByID(contactId);
        contact.setLookUpKey("");
        contactHelper.update(contact);
    }

    /**
     * Resends data if the data object with the given id needs a resend.
     *
     * @param id the id of the data object that need potentially a resend.
     */
    public void resendData(long id) {
        Data data = dataHelper.getDataByID(id);
        if (data.needsResend()) {
            data.resend();
            dataHelper.update(data);
        }
    }

    /**
     * Creates a new Data object with the given String and inserts it into the database.
     *
     * @param newMessage the string that contains the new message
     */
    public void addData(final String newMessage) {
        new AddTextToDatabase().execute(newMessage);
    }

    /**
     * Creates a new Data object with the given File and inserts it into the database.
     *
     * @param file the new file to send
     */
    public void addData(final File file) {
        new AddFileToDatabase().execute(file);
    }

    /**
     * Creates a new Data object with the given Uri and inserts it into the database.
     *
     * @param uri the new uri to send
     */
    public void addData(final Uri uri) {
        new AddUriToDatabase().execute(uri);
    }

    /**
     * Creates a Data object with the given asFile object and inserts it into the database.
     *
     * @param asFile the asFile object
     */
    private void addData(ASFile asFile) {
        if (asFile != null) {
            List<Contact> receiverList = deleteSender(chat.getReceivers());
            final HashMap<Long, DataState> receiverStateMap = DataState.createStates(receiverList, DataState.Type.NOT_SENT);

            Data data = new Data(sender, receiverList, receiverStateMap, asFile);
            data.setNetworkChatID(chat.getNetworkChatID());

            dataHelper.insert(data);
        } else {
            view.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,
                            context.getString(R.string.message_data_not_sent), Toast.LENGTH_LONG).show();
                }
            });

        }
    }

    /**
     * Creates a new ASFile object with the given file and returns it.
     *
     * @param file the file that will be load into a new asFile
     */
    private ASFile getASFile(File file) {
        ASFile asFile;
        if (file == null) {
            asFile = null;
        } else {
            //String mimeType = ImageManager.getMimeType(file.getPath());
            //asFile = new ASFile(context, newFileName(mimeType));
            asFile = new ASFile(context, file.getName());
            FileInputStream fileInputStream;
            try {
                fileInputStream = getSourceStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "File not found.");
                return null;
            }
            this.saveToInternalStorage(fileInputStream, asFile);
        }
        return asFile;
    }

    /**
     * Creates a new ASFile object with the given uri and returns it.
     *
     * @param contentUri the uri that will be used to load data into a new asFile
     */
    public ASFile getASFile(Uri contentUri) {
        ASFile asFile;
        if (contentUri == null) {
            asFile = null;
        } else {
            String mimeType = context.getContentResolver().getType(contentUri);
            String fileName = newFileName(mimeType);
            asFile = new ASFile(context, fileName);
            FileInputStream fileInputStream;
            try {
                fileInputStream = getSourceStream(contentUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Log.d(TAG, "File not found.");
                return null;
            }
            this.saveToInternalStorage(fileInputStream, asFile);
        }
        return asFile;
    }

    /**
     * Deletes the one contact object that belongs to the sender in the given contact list.
     *
     * @param receivers the receiver list
     * @return the new receiver list
     */
    private List<Contact> deleteSender(List<Contact> receivers) {
        List<Contact> newReceivers = new ArrayList<>(receivers);
        for (Contact c : newReceivers) {
            if (c.getNetworkingId().equals(sender.getNetworkingId())) {
                newReceivers.remove(c);
                return newReceivers;
            }
        }
        return newReceivers;
    }

    /**
     * Uses the given fileInputStream to load the file into the given asFile
     *
     * @param fileInputStream the fileInputStream that contains the source data
     * @param asfile          the destination of the file
     */
    private void saveToInternalStorage(FileInputStream fileInputStream, ASFile asfile) {
        try {
            OutputStream out = new FileOutputStream(asfile);
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            while ((len = fileInputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            fileInputStream.close();

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns a FileInputStream from the given uri
     *
     * @param contentUri the uri with a content scheme
     * @return the new FileInputStream
     * @throws FileNotFoundException
     */
    private FileInputStream getSourceStream(Uri contentUri) throws FileNotFoundException {
        FileInputStream out;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            ParcelFileDescriptor parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(contentUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            out = new FileInputStream(fileDescriptor);
        } else {
            out = (FileInputStream) context.getContentResolver().openInputStream(contentUri);
        }
        return out;
    }

    /**
     * Returns a FileInputStream from the given uri
     *
     * @param file the file for the fileInputStream
     * @return the new FileInputStream
     * @throws FileNotFoundException
     */
    private FileInputStream getSourceStream(File file) throws FileNotFoundException {
        return new FileInputStream(file);
    }

    /**
     * Return a new filename depending one the given mimetype and the current time.
     *
     * @param mimeType the mimeType of a file
     * @return the new filename of the file with the given mimetype
     */
    private String newFileName(String mimeType) {
        String fileName;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        if (mimeType == null) {
            fileName = "_" + timeStamp;
        } else {
            String[] mType = mimeType.split("/");
            fileName = "_" + mType[0] + "_" + timeStamp + "." + mType[1];
        }
        return fileName;
    }

    /**
     * Return the title of the local chat
     *
     * @return the chat title
     */
    public String getTitle() {
        if (chat != null) {
            return chat.getTitle();
        } else {
            startMainActivity();
            return null;
        }
    }

    /**
     * Invokes the method updateItemInserted(i) of ChatActivity if the data object belongs the local
     * chat.
     *
     * @param data the data object that got inserted into the database
     */
    @Override
    public void inserted(Data data) {
        if (chat != null) {
            if (view != null && chat.getDataObjects() != null
                    && chat.getNetworkChatID().equals(data.getNetworkChatID())) {
                chat.addData(0, data);
                view.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (chat.getDataObjects() != null) {
                            view.updateItemInserted(0);
                            //view.updateDataSet();
                            //view.scrollDown();
                        }
                    }
                });
            }
        }
    }

    /**
     * Checks if the given data object belongs to the local chat. If that's true and the update is relevant
     * to the view the data object in the local chat with the same data id will be replaced.
     *
     * @param data the data object that was updated
     */
    @Override
    public void updated(Data data) {
        if (chat != null) {
            if (view != null && chat.getDataObjects() != null && chat.getDataObjects().size() > 0
                    && chat.getNetworkChatID().equals(data.getNetworkChatID())) {
                for (int i = 0; i < chat.getDataObjects().size(); i++) {
                    if (chat.getDataObjects().get(i).getId() == data.getId()) {
                        Data d = chat.getDataObjects().get(i);
                        chat.getDataObjects().set(i, data);
                        if (d.needsResend() != data.needsResend()) {
                            iUpdated = i;
                            view.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    view.updateItemChanged(iUpdated);
                                    //view.updateDataSet();
                                }
                            });
                        }
                        return;
                    }
                }
            }
        }
    }

    /**
     * Checks if the given data object belongs to the local. If that's true and the data object in
     * the local chat with the same data id will be removed.
     *
     * @param data the data object that was removed
     */
    @Override
    public void removed(Data data) {
        if (chat != null) {
            if (view != null && chat.getDataObjects() != null && chat.getDataObjects().size() > 0
                    && chat.getNetworkChatID().equals(data.getNetworkChatID())) {
                for (int i = 0; i < chat.getDataObjects().size(); i++) {
                    if (chat.getDataObjects().get(i).getId() == data.getId()) {
                        chat.getDataObjects().remove(i);
                        iRemoved = i;
                        view.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                view.updateItemRemoved(iRemoved);
                                //view.updateDataSet();
                            }
                        });
                        return;
                    }
                }
            }
        }

    }

    /**
     * Updates the chat title of the local chat if the chat id's of the given chat and the local
     * chat are equal.
     *
     * @param chat the updated chat
     */
    public void updated(final Chat chat) {
        if (chat != null && this.chat != null) {
            if (chat.getNetworkChatID().equals(this.chat.getNetworkChatID())
                    && !chat.getTitle().equals(this.chat.getTitle())) {
                this.chat.setTitle(chat.getTitle());
                view.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        view.setTitle(chat.getTitle());
                    }
                });
            }
        }
    }

    /**
     * If the given chat and the local chat have the same id the mainActivity will be invoked.
     *
     * @param chat the removed chat
     */
    public void removed(Chat chat) {
        if (chat != null && this.chat != null) {
            if (chat.getNetworkChatID().equals(this.chat.getNetworkChatID())) {
                startMainActivity();
            }
        }
    }

    /**
     * Returns an AluObserver<Chat>
     *
     * @return the AlueObserver<Chat>
     */
    private AluObserver<Chat> getChatObserver() {
        return new AluObserver<Chat>() {
            @Override
            public void updated(Chat chat) {
                ChatPresenter.this.updated(chat);
            }

            @Override
            public void inserted(Chat chat) {
            }

            @Override
            public void removed(Chat chat) {
                ChatPresenter.this.removed(chat);
            }
        };
    }

    private class AddTextToDatabase extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            if (chat != null) {
                List<Contact> receiverList = deleteSender(chat.getReceivers());
                final HashMap<Long, DataState> receiverStateMap = DataState.createStates(receiverList, DataState.Type.NOT_SENT);

                Data data = new Data(sender, receiverList, receiverStateMap, strings[0]);
                data.setNetworkChatID(chat.getNetworkChatID());

                dataHelper.insert(data);
            }
            return null;
        }
    }

    private class AddFileToDatabase extends AsyncTask<File, Void, Void> {
        @Override
        protected Void doInBackground(File... files) {
            ASFile asFile = getASFile(files[0]);
            addData(asFile);
            return null;
        }
    }

    private class AddUriToDatabase extends AsyncTask<Uri, Void, Void> {
        @Override
        protected Void doInBackground(Uri... uris) {
            Uri uri = uris[0];
            ASFile asFile = null;
            if (uri.getScheme().equals("file")) {
                asFile = getASFile(new File(uri.getPath()));
            } else if (uri.getScheme().equals("content")) {
                asFile = getASFile(uri);
            }
            addData(asFile);
            return null;
        }
    }
}
