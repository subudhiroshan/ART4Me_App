package org.trial.hiv;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class HIVActivity extends ListActivity {

	private static ArrayList<String> msgList = new ArrayList<String>();
	private String[] msgListArray;
	private String tableName = SQLiteHelper.tableName;
	static SharedPreferences pref;

	static String flagUserMsg = "###UserXX";
	static String flagReadMsg = "###ReadXX";
	static String flagPillTaken = "Yes ###UserXX";
	static String flagLastActivity = "###LastXX";
	static String serverNumber =//"+13476742609";// USC Google Voice
								//"5554";// Emulator
								"+18572509178";// ART4Me Server
								//"+19315486862";// My Google Voice
	Button pillTaken;
	Button sendButton;
	EditText messageBody;
	String SENT = "SMS_SENT";
	String DELIVERED = "SMS_DELIVERED";
	BroadcastReceiver sentReceiver, deliveredReceiver;
	static TextView greetText;
	static String text = "";
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_hiv);

		greetText = (TextView) findViewById(R.id.textview1);
		greetText.setText(R.string.greeting);
		
		final Handler handler = new Handler();
		final Runnable r=new Runnable()
		{
			public void run() 
			{
				String greetingText = "";
				Calendar c = Calendar.getInstance(); 
				int hour = c.get(Calendar.HOUR_OF_DAY);
				
				if (hour>5 && hour<12){
					greetingText = "Good morning. Have a great day.";
				}else if (hour>=12 && hour <16){
					greetingText = "Good afternoon.";
				}else if (hour>=16 && hour<20){
					greetingText = "Good evening.";
				}else{
					greetingText = "Good night. Sleep well.";
				}
				
				greetText.setText(greetingText);
			}
		};
		Thread greetingThread = new Thread()
		{
			@Override
			public void run() {
				while(true){
					try {
						sleep(5*1000);
						handler.post(r);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		greetingThread.start();		
		
		SQLiteHelper.createDatabase(getApplicationContext());

		sentReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode())
				{
				case Activity.RESULT_OK:
					new VToast(getBaseContext(), "SMS sent", Toast.LENGTH_SHORT);
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					new VToast(getBaseContext(), "Generic failure", Toast.LENGTH_SHORT);
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
					new VToast(getBaseContext(), "No service", Toast.LENGTH_SHORT);
					break;
				case SmsManager.RESULT_ERROR_NULL_PDU:
					new VToast(getBaseContext(), "Null PDU", Toast.LENGTH_SHORT);
					break;
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					new VToast(getBaseContext(), "Radio off", Toast.LENGTH_SHORT);
					break;
				}
			}
		};

		deliveredReceiver = new BroadcastReceiver(){
			@Override
			public void onReceive(Context arg0, Intent arg1) {
				switch (getResultCode())
				{
				case Activity.RESULT_OK:
					new VToast(getBaseContext(), "SMS delivered", Toast.LENGTH_SHORT);
					break;
				case Activity.RESULT_CANCELED:
					new VToast(getBaseContext(), "SMS not delivered", Toast.LENGTH_SHORT);
					break;					    
				}
			}
		}; 

//		pref = getApplicationContext().getSharedPreferences("art4mePref", Context.MODE_PRIVATE);
		pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		if (!pref.getBoolean("userFlag", false)){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle("User Opt-In");
			alert.setMessage(R.string.user_consent);
			alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					SharedPreferences.Editor editor = pref.edit();
					editor.putBoolean("userFlag", true);
					editor.apply();
				}
			});
			alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					SharedPreferences.Editor editor = pref.edit();
					editor.putBoolean("userFlag", false);
					editor.apply();
					finish();
				}
			});
			alert.show();
		}

		SmsManager sms = SmsManager.getDefault();
		if(pref.getBoolean("userFlag", false)){
			sms.sendTextMessage(serverNumber, null, flagReadMsg , null, null);
		}

		drawMsgList();

		pillTaken = (Button) findViewById(R.id.button2);
		sendButton = (Button) findViewById(R.id.button1);
		messageBody = (EditText) findViewById(R.id.editText1);

		InputMethodManager imm=(InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(messageBody.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);

		pillTaken.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SmsManager smsRead = SmsManager.getDefault();
				if(pref.getBoolean("userFlag", false)){
					smsRead.sendTextMessage(serverNumber, null, flagPillTaken, null, null);
					new VToast(getBaseContext(), "The ART4Me server is notified.", Toast.LENGTH_SHORT);
				}
			}
		});

		sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				String msg = messageBody.getText().toString();
				messageBody.setText("");
				if (msg.length() < 1){
					new VToast(getBaseContext(), "Enter a message body...", Toast.LENGTH_SHORT);
				}
				if ((msg.length() > 0) && (msg.contains("*")||msg.contains("#"))){
					new VToast(getBaseContext(), "Do not use '*' or '#' in your message body.", Toast.LENGTH_SHORT);
					msg = "";
				}
				if ((msg.length() > 0) && !(msg.contains("*")||msg.contains("#"))){
					sendTextMessage(msg + " " + flagUserMsg);
					SQLiteHelper.insertIntoDatabase(System.currentTimeMillis()+"", msg, "person");
				}
				drawMsgList();
			}
		});    
	}

	@Override
	public void onStart(){
		super.onStart();
		drawMsgList();
	}

	@Override
	public void onResume(){
		super.onResume();
		drawMsgList();
		registerReceiver(sentReceiver, new IntentFilter(SENT));
		registerReceiver(deliveredReceiver, new IntentFilter(DELIVERED));
		SmsManager smsRead = SmsManager.getDefault();
		if(pref.getBoolean("userFlag", false)){
			smsRead.sendTextMessage(serverNumber, null, flagReadMsg, null, null);
		}
	}    

	@Override
	public void onPause(){
		super.onPause();
		try{
			unregisterReceiver(sentReceiver);
			unregisterReceiver(deliveredReceiver);
		}
		catch (IllegalArgumentException iae){
			Log.e(getClass().getSimpleName(), "Pause - Receiver failed to unregister because never registered.");
		}
	}

	@Override
	public void onStop(){
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void sendTextMessage(String messageBody){

		final PendingIntent sentPI = PendingIntent.getBroadcast(this, 0, new Intent(SENT), 0);
		final PendingIntent deliveredPI = PendingIntent.getBroadcast(this, 0, new Intent(DELIVERED), 0);

		SmsManager sms = SmsManager.getDefault();
		if(pref.getBoolean("userFlag", false)){
			sms.sendTextMessage(serverNumber, null, messageBody, sentPI, deliveredPI); 
		}
	}

	void drawMsgList() {
		SQLiteHelper sqlHelper;
		SQLiteDatabase newDB = null;
		try {
			msgList.clear();
			sqlHelper = new SQLiteHelper(getApplicationContext());
			newDB = sqlHelper.getReadableDatabase();
			Cursor c = newDB.rawQuery("SELECT MessageBody, Timestamp, Sender FROM " + tableName , null);
			startManagingCursor(c);
			if (c != null ) {
				if  (c.moveToLast()) {
					do {
						String msgBody = c.getString(c.getColumnIndex("MessageBody"));
						String time = c.getString(c.getColumnIndex("Timestamp"));
						String send = c.getString(c.getColumnIndex("Sender"));
						String full = send + "*" + time + "#" + msgBody;
						msgList.add(full.toString());
					}while (c.moveToPrevious());
				} 
			}
			stopManagingCursor(c);
			c.close();
			newDB.close();
		}
		catch (Exception e ) {
			Log.e(getClass().getSimpleName(), "Could not open the cursor/ Requerying a closed cursor");
		}
		finally{
			newDB.close();
		}

		msgListArray = msgList.toArray(new String[msgList.size()]);
		MsgListAdapter adapter = new MsgListAdapter(this, msgListArray);
		setListAdapter(adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_hiv, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		String versionName = null;
		int versionCode = 0;
		try {
		    versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		    versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
		    Log.e("tag", e.getMessage());
		}
		String msg = "Version v" + versionName + "." + versionCode + " \nServer No : " + serverNumber;
		switch (item.getItemId()) {
		case R.id.menu_lastactivity:
			SmsManager smsRead = SmsManager.getDefault();
			if(pref.getBoolean("userFlag", false)){
				smsRead.sendTextMessage(serverNumber, null, flagLastActivity, null, null);
			}
			new VToast(getBaseContext(), "The ART4Me Server is notified", Toast.LENGTH_SHORT);
			return true;
		case R.id.menu_clearlog:
			SQLiteHelper.clearDatabase();
			drawMsgList();
			new VToast(getBaseContext(), "All your messages have been cleared", Toast.LENGTH_SHORT);
			return true;
		case R.id.menu_clearpreferences:
			SharedPreferences.Editor editor = pref.edit();
			editor.remove("userFlag");
			editor.commit();
			finish();
			new VToast(getBaseContext(), "Your preferences have been reset", Toast.LENGTH_SHORT);
			return true;
		case R.id.menu_details:
			AlertDialog.Builder detailsAlert = new AlertDialog.Builder(HIVActivity.this);
			detailsAlert.setTitle("ART4Me App");
			detailsAlert.setMessage(msg);
			detailsAlert.setIcon(R.drawable.app_icon);
			detailsAlert.setPositiveButton("Close", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
				}
			});
			detailsAlert.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}