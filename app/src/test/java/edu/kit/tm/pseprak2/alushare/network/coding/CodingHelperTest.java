package edu.kit.tm.pseprak2.alushare.network.coding;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.Random;

import edu.kit.tm.pseprak2.alushare.BuildConfig;

import static junit.framework.TestCase.assertEquals;

/**
 * @author Albrecht Weiche
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CodingHelperTest {

    @Test
    public void testRandInt() {
        Random rn = new Random();
        byte[] codedByte = new byte[4];

        for (int n = 0; n < 100000; n++) {
            clearBuffer(codedByte);

            int startInt = rn.nextInt();
            CodingHelper.intTo4Byte(startInt, codedByte);

            int resultInt = CodingHelper.intFromBuffer(codedByte);

            assertEquals("Coding changed integer! in pass: " + n, startInt, resultInt);
        }
    }

    @Test
    public void testRandLong() {
        Random rn = new Random();
        byte[] codedByte = new byte[8];

        for (int n = 0; n < 1000000; n++) {
            clearBuffer(codedByte);

            long startLong = rn.nextLong();
            CodingHelper.longTo8Byte(startLong, codedByte);

            long resultLong = CodingHelper.longFromBuffer(codedByte);

            assertEquals("Coding changed long! in pass: " + n, startLong, resultLong);
        }
    }

    private void clearBuffer(byte[] buffer) {
        for (int index = 0; index < buffer.length; index++) {
            buffer[index] = 0;
        }
    }
}
