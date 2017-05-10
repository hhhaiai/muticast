package com.example.multicase;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class SS extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        new TUdpRecevier().DoStart();
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        flags = START_STICKY;
//        new Thread(new TUdpRecevier()).start();
        new TUdpRecevier().DoStart();
        return super.onStartCommand(intent, flags, startId);
    }

}
