package edu.kit.tm.pseprak2.alushare.presenter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;

import edu.kit.tm.pseprak2.alushare.model.ASFile;
import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.ASFileHelper;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.view.adapter.ImageManager;

/**
 * A class to generate Intents to open files and contacts.
 * Created by Niklas Sänger on 19.08.15.
 */
public class IntentGenerator {

    /**
     * Return the appropriate intent to open the file with the given id.
     * Uses the mimeType of the filePath to determine the right intent.
     * @param id    the file id
     * @param context   the app context
     * @return  the intent to open the file
     */
    public static Intent getIntentByFileId(long id, Context context) {

        ASFileHelper filehelper = HelperFactory.getFileHelper(context);
        File file = getFilePath(id, filehelper);
        if(file != null) {
            String type = ImageManager.getMimeType(file.getPath());
            if (type == null)
                type = "*/*";
            Intent i = new Intent();
            i.setAction(Intent.ACTION_VIEW);
            i.setType(type);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                Uri u = FileProvider.getUriForFile(context, "edu.kit.tm.pseprak2.alushare.fileprovider", file);
                i.setData(u);
                return i;
            } catch (IllegalArgumentException e) {
                Log.e("File Selector", "The selected file can't be shared: " + file);
            }
            return null;
        }
        else {
            return null;
        }
    }

    /**
     * Returns the intent to open the contact with the given id in the system contacts.
     * @param id the contact id
     * @param context   the app context
     * @return  the intent to open the contact
     */
    public static Intent getIntentByContactId(long id, Context context) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String key = getContactID(id, context);
        Uri uri = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI, key);
        intent.setData(uri);
        return intent;
    }

    /**
     * Retrieves the contact system contact id of the contact with given id.
     * @param id    the contact id
     * @param context   the app context
     * @return  the system contact id
     */
    private static String getContactID(long id, Context context) {
        Contact contact = HelperFactory.getContacHelper(context).getContactByID(id);
        long cID = -1;
        if (contact != null) {
            try {
                cID = contact.getSystemContactId(context);
            } catch (Exception e) {
                Log.d("ContactTabPresenter", e.toString());
            }
        }
        return String.valueOf(cID);
    }

    /**
     * Retrieves the filepath of the file with the given id.
     * @param id    the file id
     * @param fileHelper    the fileHelper to access the database
     * @return  the filePath of the file
     */
    private static File getFilePath(long id,ASFileHelper fileHelper) {
        ASFile file = fileHelper.getFileByID(id);
        if (file == null) {
            Log.d("IntentGenerator", "IST NULL WTF" + id);
        }
        return file;
    }
}
