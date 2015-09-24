package edu.kit.tm.pseprak2.alushare.model;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

import edu.kit.tm.pseprak2.alushare.R;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;

/**
 * Class that represents the users contact
 */
public class Profile {

    /**
     * Method to get the network address from the network
     *
     * @param context context of the application
     * @return returns the own network address
     */
    public static String getNetworkadress(Context context) {

        return HelperFactory.getContacHelper(context).getSelf().getNetworkingId();
    }

    /**
     * Method to get the users names
     *
     * @param context context of the application
     * @return returns the name of the user
     */
    public static String getOwnName(Context context) {
        String name = context.getString(R.string.profile_not_found);
        try {
            Cursor c = context.getContentResolver().query(
                    ContactsContract.Profile.CONTENT_URI, null, null, null, null);

            if (c.moveToFirst()) {
                name = c.getString(c.getColumnIndex(ContactsContract.Profile.DISPLAY_NAME));
            }
        }catch(Exception e){

        }
        return name;
    }
}
