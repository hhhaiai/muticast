package ncepu.wsy.multicastclient;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.sql.Date;
import java.text.SimpleDateFormat;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MulticastClient extends Activity {
	
	private static int PORT = 8888;
	
	private static String MulticastHost="239.245.245.245";
	/**
	 * �����鲥��socket
	 * */
	private MulticastSocket ms = null;
	/**
	 * �����鲥�İ�ť
	 * */
	private Button btn_send = null;

	private MulticastLock multicastLock = null;

	EditText et_content = null;

	LinearLayout layout = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects().penaltyLog().penaltyDeath()
				.build());
		allowMulticast();
		init();
	}

	/**
	 * ��ʼ������
	 * */
	public void init() {
		setContentView(R.layout.activity_main);
		btn_send = (Button) findViewById(R.id.btn_send);
		et_content = (EditText) findViewById(R.id.et_content);
		layout = (LinearLayout) findViewById(R.id.layout_client);
		btn_send.setOnClickListener(new SendMulticastListener());
		try {//����socketʵ��
			ms = new MulticastSocket();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * ������ťʱ�����;������㲥
	 * */
	class SendMulticastListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			DatagramPacket dp = null;
			try {
				ms.setTimeToLive(4);
				byte[] data = et_content.getText().toString().getBytes();
				InetAddress address = InetAddress.getByName(MulticastHost);
				dp = new DatagramPacket(data, data.length, address, PORT);
				ms.send(dp);
				SimpleDateFormat formatter = new SimpleDateFormat("yyyy��MM��dd��   HH:mm:ss");
				Date curDate = new Date(System.currentTimeMillis());// ��ȡ��ǰʱ��
				String str = formatter.format(curDate);
				TextView tv = new TextView(MulticastClient.this);
				tv.setText(str + " - "+ new String(data, 0, dp.getLength()));
				layout.addView(tv);
			} catch (Exception e) {
				ms.close();
				e.printStackTrace();
			}
		}
	}

	private void allowMulticast() {
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multicastLock = wifiManager.createMulticastLock("multicast.test");
		multicastLock.acquire();
	}
}