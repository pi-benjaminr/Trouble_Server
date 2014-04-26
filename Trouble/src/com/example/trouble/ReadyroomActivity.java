package com.example.trouble;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



public class ReadyroomActivity extends Activity {
	
	protected static String serverAddress = "ec2-54-185-38-25.us-west-2.compute.amazonaws.com";
	protected static int serverPort = 20000;
	public static final int MAX_PACKET_SIZE = 512;
	
	private Button launch;
	private Button leave;
	private TextView gamenumDisplay;
	private TextView chat;
	private EditText chatbox;
	private Button send;
	private Button poll;
	
	private int userID;
	private String userName;
	private int gameNum;
	private int playerNum;
	
	List<String> msgs;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_readyroom);
		Bundle b = getIntent().getExtras();
		userID = b.getInt("userID");
		userName = b.getString("userName");
		gameNum = b.getInt("gameNum");
		playerNum = b.getInt("playerNum");

		launch = (Button) findViewById(R.id.launch);
		leave = (Button) findViewById(R.id.leave);
		gamenumDisplay = (TextView) findViewById(R.id.gamenumDisplay);
		chat = (TextView) findViewById(R.id.chat);
		send = (Button) findViewById(R.id.send);
		poll = (Button) findViewById(R.id.poll);
		chatbox = (EditText) findViewById(R.id.chatbox);
		
		chat.setMovementMethod(new ScrollingMovementMethod());
		
		gamenumDisplay.setText("Game Number: " + gameNum + "\nLaunch when all your friends have joined!");
		
		
		
		
		
		
	}
	
	public void poll(View view) {
		Thread t = new Thread() {
			@Override
			public void run() {
				msgs = sendMult("POLL " + userID);
			}
		};
		t.start();
		try {
			t.join();
		} catch (Exception e) {}

		for (String msg : msgs) {
			chat.setText(chat.getText() + msg);
		}
	}
	
	public void send(View view) {
		Log.i("trouble", "abcdeft");
		new Thread() {
			@Override
			public void run() {
				send("SEND " + userID + " " + chatbox.getText().toString());
				chatbox.clearComposingText();
			}
		}.start();
	}
	
	public void launch(View view) {
		new Thread() {
			@Override
			public void run() {
				send("LAUNCH " + userID);
			}
		}.start();
		Intent intent = new Intent(ReadyroomActivity.this, GameActivity.class);
		Bundle b = new Bundle();
		b.putInt("userID", userID);
		b.putString("userName", userName);
		b.putInt("playerNum", playerNum);
		b.putString("chat", chat.getText().toString());
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}
	
	public void leave(View view) {
		
		new Thread() {
			@Override
			public void run() {
				send("LEAVE " + userID);
			}
		}.start();
		Intent intent = new Intent(ReadyroomActivity.this, MainActivity.class);
		Bundle b = new Bundle();
		b.putInt("userID", userID);
		b.putString("userName", userName);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
	
	protected static List<String> sendMult(String command) {
		List<String> msgs = new ArrayList<String>();
		String payload = "";
		try {
			InetSocketAddress serverSocketAddress = new InetSocketAddress(
					serverAddress, serverPort);
			DatagramSocket socket = new DatagramSocket();
			DatagramPacket txPacket = new DatagramPacket(command.getBytes(),
					command.length(), serverSocketAddress);
			socket.send(txPacket);

			while (! payload.startsWith("END")) {

				byte[] buf = new byte[MAX_PACKET_SIZE];
				DatagramPacket rxPacket = new DatagramPacket(buf, buf.length);
				socket.receive(rxPacket);
				payload = new String(rxPacket.getData(), 0, rxPacket.getLength());
				msgs.add(payload);
				
			}
			msgs.remove(msgs.size()-1);

			socket.close();
		} catch (SocketException e) {
		} catch (IOException e) {
		}
		return msgs;
	}

}
