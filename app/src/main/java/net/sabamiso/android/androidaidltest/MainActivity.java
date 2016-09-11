package net.sabamiso.android.androidaidltest;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.jakewharton.rxbinding.view.RxView;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.subjects.PublishSubject;

public class MainActivity extends AppCompatActivity {
    final String TAG = getClass().getSimpleName();

    ITestInterface mTestInterface;
    TextView mTextViewMessage;

    PublishSubject<String> mMessageBus = PublishSubject.create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, TestService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);

        RxView.clicks(findViewById(R.id.buttonTest1))
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (mTestInterface != null) {
                            try {
                                mTestInterface.test1();
                                Log.i(TAG, "call : mTestInterface.test1()");
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        RxView.clicks(findViewById(R.id.buttonTest3))
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (mTestInterface != null) {
                            try {
                                int result = mTestInterface.test2(123);
                                Log.i(TAG, "call : mTestInterface.test2(123) : result=" + result);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        RxView.clicks(findViewById(R.id.buttonTest3))
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (mTestInterface != null) {
                            try {
                                String result = mTestInterface.test3("/String Message/");
                                Log.i(TAG, "call : mTestInterface.test3(\"/String Message/\") : result=" + result);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

        mTextViewMessage = (TextView)findViewById(R.id.textViewMessage);
        mMessageBus.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                     @Override
                     public void call(String val) {
                        Log.i(TAG, "call() : val = " + val);
                         mTextViewMessage.setText(val);
                     }
                 }
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mTestInterface = ITestInterface.Stub.asInterface(iBinder);
            try {
                mTestInterface.setOnTestCallback(callback);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mTestInterface = null;
        }
    };

    private ITestCallbackInterface callback = new ITestCallbackInterface.Stub(){
        @Override
        public void onTest(String msg) throws RemoteException {
            mMessageBus.onNext(msg);
        }
    };
}
