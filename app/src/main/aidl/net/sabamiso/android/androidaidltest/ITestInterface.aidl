package net.sabamiso.android.androidaidltest;

import net.sabamiso.android.androidaidltest.ITestCallbackInterface;

interface ITestInterface {
    void test1();
    int test2(in int val);
    String test3(in String test);

    oneway void setOnTestCallback(ITestCallbackInterface callback);
}
