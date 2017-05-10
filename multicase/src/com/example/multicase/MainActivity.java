package com.example.multicase;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent service = new Intent(this, SS.class);
        startService(service);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String AGroupIP = "239.192.152.163";
    // private String AGroupIP = "255.255.255.255";
    private int ARemotePort = 48809;

    @Override
    protected void onResume() {
        super.onResume();

        new Thread(new Runnable() {

            @Override
            public void run() {
                TUdpSender.DoSendUdpGroupMsg(MainActivity.this, AGroupIP, ARemotePort, "group msg");
            }
        }).start();
        // try {
        // MulticastSocket multicastSocket = new MulticastSocket(ARemotePort);
        // InetAddress address = InetAddress.getByName(AGroupIP); // 必须使用D类地址
        // multicastSocket.joinGroup(address); // 以D类地址为标识，加入同一个组才能实现广播
        //
        // byte[] buf = AMsg.getBytes("utf-8");
        // DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length,
        // address, ARemotePort);
        // if (datagramPacket == null) {
        // Log.d("100", "data is null");
        // return;
        // }
        // if (multicastSocket == null) {
        // Log.d("100", "multicastSocket is null");
        // return;
        // }
        // multicastSocket.send(datagramPacket);
        // multicastSocket.close();
        //
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }
}
