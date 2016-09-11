AndroidAIDLTest
====

AndroidでActivity-Service間の通信を行う際のメモ。
AIDLを使った通信を行うことで、プロセス間通信を行うことができる。

Activity→Serviceの呼び出し
----

- インタフェースはITestInterface.aidlで定義
  - ビルド時にITestInterface, ITestInterface.Stubクラスが生成される
- Service側にITestInterface.Stubのインスタンスを用意し、onBind()の戻り値でITestInterface.Stubのインスタンスを返す
- Activity側はbindService()でITestInterfaceのインスタンスを得る
- Activity側からITestInterfaceのメソッドを呼び出すと、Service側にあるITestInterface.Stubのメソッドが呼び出される

Service→Activityの呼び出し
----

- Activity側にITestInterface.Stubのインスタンスを用意しておく
- ITestInterface.setOnTestCallback()を使ってService側にstubを渡す
- Service側からITestCallbackInterface.onTest()を呼び出すと、Activity側のITestCallbackInterface.StubインスタンスのonTest()が呼び出される。
- ITestCallbackInterface.Stub.onTest()の呼び出しは、別スレッドから呼び出されるので、GUI要素を操作する場合はHandlerクラスなどを使ってメインスレッドから行うこと。

プロセスモデル
----
AndroidManifest.xml内の、<service android:process="..." />の指定方法によってプロセスモデルが変わる。

何も指定しない通常の記述をすると、ServiceはActivityと同じプロセス内に別スレッドとして生成される。

    <application ...>
        ...
        <service android:name=".TestService"/>
    </application>

android:processに次のように指定すると、Activityとは別のプライベートプロセスが生成される。

    <application ...>
        ...
        <service android:name=".TestService" android:process=":serviceprocess"/>
    </application>

この場合は、"Activityのプロセス名"+"android:nameで指定した名前"という名前のプロセスが生成される

    $ ps
    u0_a420   26811 687   1573632 96820 SyS_epoll_ 0000000000 S net.sabamiso.android.androidaidltest
    u0_a420   26952 687   1515608 68928 SyS_epoll_ 0000000000 S net.sabamiso.android.androidaidltest:serviceprocess

android:processに次のように指定すると、Activityとは別のグローバルプロセスが生成される。

    <application ...>
        ...
        <service android:name=".TestService" android:process="serviceprocess"/>
    </application>

この場合は、"android:nameで指定した名前"のプロセスが生成される

    $ ps
    u0_a422   4204  687   1574424 95284 SyS_epoll_ 0000000000 S net.sabamiso.android.androidaidltest
    u0_a422   4419  687   1515608 69124 SyS_epoll_ 0000000000 S .serviceprocess


参考
  - [https://developer.android.com/guide/topics/manifest/service-element.html](https://developer.android.com/guide/topics/manifest/service-element.html)

> If the name assigned to this attribute begins with a colon (':'), a new process, private to the application, is created when it's needed and the service runs in that process. If the process name begins with a lowercase character, the service will run in a global process of that name, provided that it has permission to do so. This allows components in different applications to share a process, reducing resource usage.

Copyright and license
----
Copyright (c) 2016 yoggy

Released under the [MIT license](LICENSE.txt)
