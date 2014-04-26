package com.example.trouble;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;





import java.util.Scanner;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	protected static String serverAddress = "ec2-54-185-38-25.us-west-2.compute.amazonaws.com";
	protected static int serverPort = 20000;
	public static final int MAX_PACKET_SIZE = 512;
	
	private int userID;
	private String userName;
	private int gameNum = -1;
	private int playerNum;
	
	private EditText username;
	private EditText gamenum;
	private Button create;
	private Button register;
	private Button join;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		username = (EditText) findViewById(R.id.username);
		gamenum = (EditText) findViewById(R.id.gamenumDisplay);
		create = (Button) findViewById(R.id.create);
		register = (Button) findViewById(R.id.register);
		join = (Button) findViewById(R.id.join);
		
		Intent intent = getIntent();
		Bundle b = getIntent().getExtras();
		if (b != null){
			userID = b.getInt("userID");
			userName = b.getString("userName");
			
			create.setEnabled(true);
			join.setEnabled(true);
			register.setEnabled(false);
		} else {
			create.setEnabled(false);
			join.setEnabled(false);
			register.setEnabled(true);
		}
		
		
		
		
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void create(View view) {
		Thread t = new Thread() {
			@Override
			public void run() {
				String response = send("CREATE " + userID);
				gameNum = new Scanner(response).nextInt();
				playerNum = 0;
			}
		};
		t.start();
		try {
			t.join();
		} catch (Exception e) {}
		startReadyroom();
		
	}
	
	public void join(View view) {
		Thread t = new Thread() {
			@Override
			public void run() {
				gameNum = Integer.parseInt(gamenum.getText().toString());
				String response = send("JOIN " + userID + " " + gameNum);
				if (response.startsWith("FAIL")) {
					Toast.makeText(getApplicationContext(),"Failed to join game",Toast.LENGTH_SHORT);
					gameNum = -1;
				} else {
					playerNum = response.split(" ").length-1;
				}
			}
		};
		t.start();
		try {
			t.join();
		} catch (Exception e) {}
		
		if (gameNum != -1) {
			startReadyroom();
		}

		
	}
	
	public void startReadyroom() {
		Intent intent = new Intent(MainActivity.this, ReadyroomActivity.class);
		Bundle b = new Bundle();
		b.putInt("userID", userID);
		b.putString("userName", userName);
		b.putInt("gameNum", gameNum);
		b.putInt("playerNum", playerNum);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}
	
	
	public void register(View view) {
		new Thread() {
			@Override
			public void run() {
				String name = username.getText().toString();
				String response = send("REGISTER " + name);
				userID = new Scanner(response).nextInt();
			}
		}.start();

		create.setEnabled(true);
		join.setEnabled(true);
		register.setEnabled(false);
		
	}
	
	protected static String send(String command) {
		String payload = "";
		try {
			InetSocketAddress serverSocketAddress = new InetSocketAddress(
					serverAddress, serverPort);
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket txPacket = new DatagramPacket(command.getBytes(),
					command.length(), serverSocketAddress);
			socket.send(txPacket);

			byte[] buf = new byte[MAX_PACKET_SIZE];
			DatagramPacket rxPacket = new DatagramPacket(buf, buf.length);
			socket.receive(rxPacket);
			payload = new String(rxPacket.getData(), 0, rxPacket.getLength());

			socket.close();
		} catch (SocketException e) {
		} catch (IOException e) {
		}
		return payload;
	}

}
