package edu.kit.tm.pseprak2.alushare;

import org.robolectric.RuntimeEnvironment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import edu.kit.tm.pseprak2.alushare.model.Contact;
import edu.kit.tm.pseprak2.alushare.model.helper.HelperFactory;
import edu.kit.tm.pseprak2.alushare.network.protocol.MessagingProtocol;

/**
 * @author Albrecht Weiche
 */
public class TestHelper {
    public static void resetHelperFactory() {
        resetSingleton(HelperFactory.class, "chatHelper");
        resetSingleton(HelperFactory.class, "dataHelper");
        resetSingleton(HelperFactory.class, "asFileHelper");
        resetSingleton(HelperFactory.class, "contactHelper");
        resetSingleton(HelperFactory.class, "dataStateHelper");
    }

    private static void resetSingleton(Class clazz, String fieldName) {
        Field instance;
        try {
            instance = clazz.getDeclaredField(fieldName);
            instance.setAccessible(true);
            instance.set(null, null);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static void blockingSend(boolean blocking) {
        Field instance;
        try {
            instance = MessagingProtocol.class.getDeclaredField("sendBlocking");
            instance.setAccessible(true);
            instance.setBoolean(null, blocking);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    public static List<Contact> diffReceiver(List<Contact> receiverA, List<Contact> receiverB) {
        List<Contact> newReceiverB = new ArrayList<>(receiverB);
        List<Contact> diffList = new ArrayList<>();

        for (Contact aContact:receiverA) {
            boolean found = false;
            int index = 0;
            while (!found && index < newReceiverB.size()) {
                Contact contact = newReceiverB.get(index);
                found = contact.getNetworkingId().equals(aContact.getNetworkingId());
                if (found) {
                    newReceiverB.remove(index);
                }
                index++;
            }
            if (!found) {
                diffList.add(aContact);
            }
        }
        diffList.addAll(newReceiverB);
        return diffList;
    }


    public static void copyFileUsingStream(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;

        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    public static String getTestResourcePath() { // /test/resources/
        String str = RuntimeEnvironment.application.getApplicationContext().getPackageResourcePath();

        if (str.length() > 0 && str.charAt(str.length()-1)=='.') {
            str = str.substring(0, str.length()-1);
        }
        str = str.concat("src/test/resources/");
        return str;
    }

    public static String getDatabasePath() {//  /../databases/
        return RuntimeEnvironment.application.getDatabasePath("").getPath().concat("/");
    }

    public static String getFilesPath() {
        return RuntimeEnvironment.application.getFilesDir().getPath().concat("/");
    }

}
