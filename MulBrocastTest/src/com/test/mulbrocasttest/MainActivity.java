package com.test.mulbrocasttest;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity
        implements OnClickListener, MulSocketMng.RecDataCallback, OnCheckedChangeListener {

    private static final CommonLog log = LogFactory.createLog();
    private static final String GROUP_IP = "239.255.255.250";

    private TextView mTextView;
    private Button mButtonSend;
    private Button mButtonOpen;
    private Button mButtonClose;
    private Button mButtonClear;
    private TextView mTVContent;
    private ScrollView mScrollView;
    private RadioGroup mRadioGroup;

    private MulSocketMng mSocketMng;
    private MulticastLock mMulticastLock;
    private StringBuffer mDataBuffer = new StringBuffer();
    private UnickSocketMng mUnickSocketMng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupViews();
        initData();
    }

    @Override
    protected void onDestroy() {
        closeWifiBrocast();
        super.onDestroy();
    }

    private void setupViews() {
        mTextView = (TextView) findViewById(R.id.textview);
        mButtonSend = (Button) findViewById(R.id.btnSend);
        mButtonOpen = (Button) findViewById(R.id.btnOpen);
        mButtonClose = (Button) findViewById(R.id.btnClose);
        mButtonClear = (Button) findViewById(R.id.btnClear);
        mButtonClose.setOnClickListener(this);
        mButtonOpen.setOnClickListener(this);
        mButtonSend.setOnClickListener(this);
        mButtonClear.setOnClickListener(this);
        mTVContent = (TextView) findViewById(R.id.tv_content);
        mScrollView = (ScrollView) findViewById(R.id.sv_view);
        mRadioGroup = (RadioGroup) findViewById(R.id.rg_group);
        mRadioGroup.setOnCheckedChangeListener(this);
    }

    private void initData() {
        mSocketMng = new MulSocketMng();
        mUnickSocketMng = new UnickSocketMng();
    }

    private void updateText(String groupIP, int port) {
        StringBuffer sBuffer = new StringBuffer();
        sBuffer.append("Socket Bind --> groupIP = " + groupIP + ", port = " + port);
        mTextView.setText(sBuffer.toString());
    }

    private void clearText() {
        mTextView.setText("socket close...");
        clearContent();
    }

    private void updateContent(String hostIP, int port, final String data) {
        mDataBuffer.append("rec from ip:" + hostIP + ", port = " + port);
        mDataBuffer.append("\n" + data + "\n-----------------\n");
        mTVContent.setText(mDataBuffer.toString());

        mScrollView.scrollTo(0, 1024 * 1024);

    }

    private void clearContent() {
        mDataBuffer = new StringBuffer();
        mTVContent.setText("");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.btnClose:
            close();
            break;
        case R.id.btnOpen:
            open();
            break;
        case R.id.btnSend:
            send();
            break;
        case R.id.btnClear:
            clearContent();
            break;
        }
    }

    private static final int TARGET_PORT = 1900;

    private void send() {
        String value = "test mulbrocast!!!";
        boolean ret = mSocketMng.syncSendData(value, GROUP_IP, TARGET_PORT);

        mUnickSocketMng.syncSendData("udpdata..", "192.168.11.3", 12345);
    }

    private static final int LOCAL_PORT = 1900;

    private void open() {
        boolean ret = mSocketMng.openSokcet(GROUP_IP, LOCAL_PORT);
        log.e("openSokcet GROUP_IP = " + GROUP_IP + ", LOCAL_PORT = " + LOCAL_PORT + ", ret = " + ret);

        mSocketMng.startListenThread(this);
        updateText(GROUP_IP, LOCAL_PORT);

        mUnickSocketMng.openSokcet(12345);
        mUnickSocketMng.startListenThread();
    }

    private void close() {
        mSocketMng.closeSocket();
        log.e("closeSocket");
        clearText();

        mUnickSocketMng.closeSocket();
    }

    @Override
    public void onDataReceive(final String hostIP, final int port, final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateContent(hostIP, port, data);
            }
        });
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
        case R.id.rb_open:
            openWifiBrocast();
            break;
        case R.id.rb_close:
            closeWifiBrocast();
            break;
        }
    }

    private void openWifiBrocast() {
        if (mMulticastLock == null) {
            WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            mMulticastLock = wifiManager.createMulticastLock("multicast.test");
            if (mMulticastLock != null) {
                mMulticastLock.acquire();
                log.e("openWifiBrocast");
            }
        }
    }

    private void closeWifiBrocast() {
        if (mMulticastLock != null) {
            mMulticastLock.release();
            mMulticastLock = null;
            log.e("closeWifiBrocast");
        }
    }
}
