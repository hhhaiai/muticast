package ncepu.wsy.udp_multicast_server;

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
import android.os.Handler;
import android.os.Message;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MulticastServer extends Activity implements Runnable {
	
	private MulticastSocket ms;
	
	private static String MulticastHost="239.245.245.245";
	
	private static int PORT = 8888;
	
	InetAddress receiveAddress;
	
	String result = "";
	
	private MulticastLock multicastLock;
	
	LinearLayout layout;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		allowMulticast();
		setContentView(R.layout.activity_main);
		layout = (LinearLayout) findViewById(R.id.layout);
		try {
			ms = new MulticastSocket(PORT);
			receiveAddress=InetAddress.getByName(MulticastHost);
			ms.joinGroup(receiveAddress);
			new Thread(this).start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	 Handler hd = new Handler(){
		 @Override
		 public void handleMessage(Message msg)										
		  {											
			 super.handleMessage(msg);			
			 switch(msg.what){
			   case 1:
				   SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyy年MM月dd日   HH:mm:ss");       
				   Date   curDate   =   new   Date(System.currentTimeMillis());//获取当前时间        
				   String   str   =   formatter.format(curDate);  
				   TextView tv =new TextView(MulticastServer.this);
				   tv.setText(str+" - "+result);
				   layout.addView(tv);
				   break; 
			 }
		  }						
	 };
	@Override
	public void run() {
		byte data[] = new byte[1024];
		DatagramPacket dp = new DatagramPacket(data, 1024);
		while (true) {
			try {
				ms.receive(dp);
				result = new String(data, 0, dp.getLength());
				Message msg = new Message();
				msg.what = 1;
				hd.sendMessage(msg);
			} catch (Exception e) {
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