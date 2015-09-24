package edu.kit.tm.pseprak2.alushare.view;

import android.content.Context;
import android.media.Image;

import com.squareup.picasso.Picasso;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;
import java.lang.reflect.Field;

import edu.kit.tm.pseprak2.alushare.BuildConfig;
import edu.kit.tm.pseprak2.alushare.view.adapter.ImageManager;
import edu.kit.tm.pseprak2.alushare.view.adapter.viewholder.ChatRecyclerItemViewHolder;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;

/**
 * Created by arthuranselm on 09.09.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ImageManagerTest {

    private static final String FILES_DIR = "src/test/resources/";
    private static final int TEST_FILE_COUNT = 4;
    private ImageManager imageManager;
    private Context context = RuntimeEnvironment.application;


    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        imageManager = new ImageManager(context);
        Field field = ImageManager.class.getDeclaredField("picasso");
        field.setAccessible(true);
        Picasso picasso = (Picasso) field.get(imageManager);
        assertFalse(picasso != null);
    }

    @Test
    public void testGetMimeType(){
        String path = FILES_DIR + "Datei-" + ((1 % TEST_FILE_COUNT) + 1);
        String type = ImageManager.getMimeType(path);
        assertEquals(type, null);
    }

    @After
    public void tearDown(){
        imageManager = null;
    }
}
