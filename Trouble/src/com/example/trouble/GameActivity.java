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

public class GameActivity extends Activity {
	
	protected static String serverAddress = "ec2-54-185-38-25.us-west-2.compute.amazonaws.com";
	protected static int serverPort = 20000;
	public static final int MAX_PACKET_SIZE = 512;
	
	private int userID;
	private String userName;
	private int gameNum;
	private int playerNum;
	
	private String gamestate;
	private int dieNum;
	
	private int numPlayers;
	private int curTurn;
	private int[][] pieces = new int[4][4];
	
	
	private Button roll;
	private Button update;
	private Button piece0;
	private Button piece1;
	private Button piece2;
	private Button piece3;
	private Button endTurn;
	private Button endGame;
	private TextView die;
	private TextView chat;
	private EditText chatbox;
	private Button send;
	private Button poll;
	private TextView gameDisp;
	
	List<String> msgs;

	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		Bundle b = getIntent().getExtras();
		userID = b.getInt("userID");
		userName = b.getString("userName");
		playerNum = b.getInt("playerNum");
		
		roll = (Button) findViewById(R.id.roll);
		update = (Button) findViewById(R.id.update);
		piece0 = (Button) findViewById(R.id.piece0);
		piece1 = (Button) findViewById(R.id.piece1);
		piece2 = (Button) findViewById(R.id.piece2);
		piece3 = (Button) findViewById(R.id.piece3);
		endTurn = (Button) findViewById(R.id.next);
		endGame = (Button) findViewById(R.id.endgame);
		die = (TextView) findViewById(R.id.die);
		chat = (TextView) findViewById(R.id.chatGame);
		gameDisp = (TextView) findViewById(R.id.gameDisp);
		

		send = (Button) findViewById(R.id.send2);
		poll = (Button) findViewById(R.id.poll2);
		chatbox = (EditText) findViewById(R.id.chatbox2);
		
		chat.setMovementMethod(new ScrollingMovementMethod());
		chat.setText(b.getString("chat"));
		
		roll.setEnabled(false);
		piece0.setEnabled(false);
		piece1.setEnabled(false);
		piece2.setEnabled(false);
		piece3.setEnabled(false);
		endTurn.setEnabled(false);
		endGame.setEnabled(true);
		
		
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
				
			}
		}.start();
		chatbox.clearComposingText();
	}
	
	public void update (View view) {
		Thread t = new Thread() {
			@Override
			public void run() {
				String response = send("GETGAMESTATE " + userID);
				gamestate = response;
			}
		};
		t.start();
		try {
			t.join();
		} catch (Exception e) {}
		gameDisp.setText(gamestate);
		Scanner scan = new Scanner (gamestate);
		numPlayers = scan.nextInt();
		curTurn = scan.nextInt();
		dieNum = scan.nextInt();
		die.setText(""+dieNum);
		int count = 0;
		while (scan.hasNextInt()) {
			pieces[count/4][count%4] = scan.nextInt();
		}
		
		if (curTurn == playerNum) {
			roll.setEnabled(true);
			update.setEnabled(false);
		}
		
	}
	
	public void roll(View view) {
		Thread t = new Thread() {
			@Override
			public void run() {
				String response = send("ROLL " + userID);
				dieNum = new Scanner(response).nextInt();
			}
		};
		t.start();
		try {
			t.join();
		} catch (Exception e) {}
		die.setText(""+dieNum);
		
		roll.setEnabled(false);
		piece0.setEnabled(true);
		piece1.setEnabled(true);
		piece2.setEnabled(true);
		piece3.setEnabled(true);
		endTurn.setEnabled(true);
	}
	
	public void next(View view) {
		Thread t = new Thread() {
			@Override
			public void run() {
				String response = send("NEXT " + userID);
				dieNum = new Scanner(response).nextInt();
			}
		};
		t.start();
		
		update.setEnabled(true);
		roll.setEnabled(false);
		piece0.setEnabled(false);
		piece1.setEnabled(false);
		piece2.setEnabled(false);
		piece3.setEnabled(false);
		endTurn.setEnabled(false);
	}
	
	public void endgame(View view) {
		new Thread() {
			@Override
			public void run() {
				send("ENDGAME " + userID);
			}
		}.start();
		Intent intent = new Intent(GameActivity.this, MainActivity.class);
		Bundle b = new Bundle();
		b.putInt("userID", userID);
		b.putString("userName", userName);
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}
	
	public void piece0(View view) {
		new Thread() {
			@Override
			public void run() {
				send("MOVE " + userID + " " + playerNum + " " + 0 + " " + (pieces[playerNum][0]+dieNum));
			}
		}.start();
	}
	
	public void piece1(View view) {
		new Thread() {
			@Override
			public void run() {
				send("MOVE " + userID + " " + playerNum + " " + 1 + " " + (pieces[playerNum][1]+dieNum));
			}
		}.start();
	}
	
	public void piece2(View view) {
		new Thread() {
			@Override
			public void run() {
				send("MOVE " + userID + " " + playerNum + " " + 2 + " " + (pieces[playerNum][2]+dieNum));
			}
		}.start();
	}
	
	public void piece3(View view) {
		new Thread() {
			@Override
			public void run() {
				send("MOVE " + userID + " " + playerNum + " " + 3 + " " + (pieces[playerNum][3]+dieNum));
			}
		}.start();
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
