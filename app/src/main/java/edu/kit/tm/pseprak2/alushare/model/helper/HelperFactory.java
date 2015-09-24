package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.Context;

import edu.kit.tm.pseprak2.alushare.model.DataState;

/**
 * Created by dominik on 26.06.15.
 */
public class HelperFactory {
    private static ChatHelper chatHelper;
    private static DataHelper dataHelper;
    private static ASFileHelper asFileHelper;
    private static ContactHelper contactHelper;
    private static DataStateHelper dataStateHelper;

    public static ChatHelper getChatHelper(Context context){
        if (chatHelper == null) {
            chatHelper = new SQLChatHelper(context);
        }
        return chatHelper;
    }

    public static ContactHelper getContacHelper(Context context){
        if (contactHelper == null) {
            contactHelper = new SQLContactHelper(context);
        }
        return contactHelper;
    }

    public static DataHelper getDataHelper(Context context){
        if (dataHelper == null) {
            dataHelper = new SQLDataHelper(context);
        }
        return dataHelper;
    }

    public static ASFileHelper getFileHelper(Context context){
        if (asFileHelper == null) {
            asFileHelper = new SQLFileHelper(context);
        }
        return asFileHelper;    }

    public static DataStateHelper getDataStateHelper(Context context) {
        if (dataStateHelper == null) {
            dataStateHelper = new SQLDataStateHelper(context);
        }
        return dataStateHelper;
    }
}
