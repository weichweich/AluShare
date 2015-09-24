package edu.kit.tm.pseprak2.alushare.presenter;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import edu.kit.tm.pseprak2.alushare.BuildConfig;

import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)

public class QRCodeGeneratorTest {
    String networkAddress = "TorID";
    QRCodeGenerator generator;

    @Before
    public void setUp(){
        generator = new QRCodeGenerator(networkAddress);
    }

    @Test
    public void generateQRCodeTest(){
        assertNotNull(generator.generateQRCode());
    }

}
