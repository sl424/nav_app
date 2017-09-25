package com.android.nav;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This is a simple framework for a test of an Application.  See
 * {@link android.test.ApplicationTestCase ApplicationTestCase} for more information on
 * how to write and extend Application tests.
 * <p/>
 * To run this test, you can type:
 * adb shell am instrument -w \
 * -e class com.android.nav.mainActivityTest \
 * com.android.nav.tests/android.test.InstrumentationTestRunner
 */
public class mainActivityTest extends ActivityInstrumentationTestCase2<mainActivity> {

    public mainActivityTest() {
        super("com.android.nav", mainActivity.class);
    }

}
