package edu.kit.tm.pseprak2.alushare.model.helper;

import android.content.Context;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import edu.kit.tm.pseprak2.alushare.BuildConfig;

import static org.junit.Assert.assertEquals;

/**
 * Created by dominik on 08.09.15.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk=21)
public class AluObservableTest {
    private Context mContext = RuntimeEnvironment.application;
    private AluObservable<String> observable;
    private SomeObserver observerA;
    private SomeObserver observerB;

    private class SomeObserver implements AluObserver<String> {
        public String test;

        @Override
        public void updated(String data) {
            test = data;
        }

        @Override
        public void inserted(String data) {
            test = data;
        }

        @Override
        public void removed(String data) {
            test = data;
        }
    }

    @Before
    public void setUp() throws Exception {
        observable = new AluObservable();
        observerA = new SomeObserver();
        observerB = new SomeObserver();

        observable.addObserver(observerA);
        observable.addObserver(observerB);
        observable.addObserver(null);
    }

    @Test
    public void testNotifyUpdated() {
        observable.notifyUpdated("up");
        assertEquals("up", observerA.test);
        assertEquals("up", observerB.test);
    }

    @Test
    public void testNotifyRemoved() {
        observable.notifyRemoved("del");
        assertEquals("del", observerA.test);
        assertEquals("del", observerB.test);
    }

    @Test
    public void testNotifyInserted() {
        observable.notifyInserted("in");
        assertEquals("in", observerA.test);
        assertEquals("in", observerB.test);
    }
}
