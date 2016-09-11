package net.sabamiso.android.androidaidltest;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class TestService extends Service {
    final String TAG = getClass().getSimpleName();

    ITestCallbackInterface mTestCallbackInterface;

    ITestInterface.Stub mStub =
            new ITestInterface.Stub() {
                @Override
                public void test1() throws RemoteException {
                    Log.i(TAG, "test1()");
                }

                @Override
                public int test2(int val) throws RemoteException {
                    Log.i(TAG, "test2() : val=" + val);
                    return val * 2;
                }

                @Override
                public String test3(String val) throws RemoteException {
                    Log.i(TAG, "test3() : val=" + val);
                    if (val == null) return "null";
                    return val + val;
                }

                @Override
                public void setOnTestCallback(ITestCallbackInterface callback) throws RemoteException {
                    mTestCallbackInterface = callback;
                }
            };

    public IBinder onBind(Intent intent) {
        return mStub;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        startTimer();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        stopTimer();
    }

    /////////////////////////////////////////////////////////////////////////////////////

    Observable<Long> observable = Observable.interval(1000, TimeUnit.MILLISECONDS);;
    Subscription subscription;

    public void startTimer() {
        stopTimer();

        subscription =  observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long val) {
                        String msg = "message from TestService : val=" + val;
                        Log.d(TAG, msg);
                        if (mTestCallbackInterface != null) {
                            try {
                                mTestCallbackInterface.onTest(msg);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
    }

    public void stopTimer() {
        if (subscription != null) {
            subscription.unsubscribe();
            subscription = null;
        }
    }
}
