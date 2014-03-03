package org.trial.hiv;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;


public class SMSReceiver extends BroadcastReceiver
{

	public void onReceive(final Context context, Intent intent)
	{
		if (intent.getAction().equalsIgnoreCase("android.provider.Telephony.SMS_RECEIVED")){
			
			Bundle bundle = null;
			do{
				bundle = intent.getExtras();
			}while (bundle == null);
			
			Object pdus[] = (Object[]) bundle.get("pdus");
			SmsMessage smsMessage[] = new SmsMessage[pdus.length];
			for (int n = 0; n<pdus.length; n++)
			{
				smsMessage[n] = SmsMessage.createFromPdu((byte[]) pdus[n]);
			}
			final String sender = smsMessage[0].getOriginatingAddress();
			final String body = smsMessage[0].getMessageBody();

//			Log.d("Roshan", "Pref outside is " + HIVActivity.dpref.getBoolean("userFlag", false));
//			Toast.makeText(context, "Pref outside is " + HIVActivity.pref.getBoolean("userFlag", false), Toast.LENGTH_SHORT).show();
//			if (HIVActivity.pref.getBoolean("userFlag", false)){
				final Handler handler=new Handler();
				final Runnable r=new Runnable()
				{
					public void run() 
					{
						receivedSMS(context, sender, body);
					}
				};

				Thread smsReceivedThread = new Thread()
				{
					@Override
					public void run() {
						handler.post(r);
					}
				};
				smsReceivedThread.start();

//				this.abortBroadcast();
//			}
		}
	}

	public void receivedSMS(final Context context, String senderNo, String messageBody){

		final String id;
		String actualMessage;

//		if (HIVActivity.pref.getBoolean("userFlag", false) && senderNo.equalsIgnoreCase(HIVActivity.serverNumber)){
			if (messageBody.indexOf("###")<0){
				id = "";
				actualMessage = messageBody;
			}else{
				id = messageBody.substring(messageBody.indexOf("###")+3);
				actualMessage = messageBody.substring(0, messageBody.indexOf("###"));
			}

			if (id.length() == 6){
				new VToast(context , "ART4Me Pill Reminder received.", Toast.LENGTH_SHORT);
				SQLiteHelper sqlSender = new SQLiteHelper(context);
				SQLiteHelper.insertIntoDatabase(System.currentTimeMillis() + "" , actualMessage, "server");
				sqlSender.close();
//				this.abortBroadcast();
				deleteSMS(context);
				Notifier.displayNotice(context);
				Handler handler2 = new Handler(); 
				handler2.postDelayed(new Runnable() { 
					public void run() { 
						SmsManager sms = SmsManager.getDefault();
						sms.sendTextMessage(HIVActivity.serverNumber, null, "Message delivered ###" + id, null, null);
						new VToast(context, "Delivery report sent.", Toast.LENGTH_SHORT);
					} 
				}, 2000);
			}else{
				new VToast(context, "Invalid ART4Me Server message.", Toast.LENGTH_SHORT);
			}
			//		}
	}

	public void deleteSMS(Context context) {
		try {
			Uri uriSms = Uri.parse("content://sms/inbox");
			Cursor c = context.getContentResolver().query(uriSms,
					new String[] { "_id", "thread_id", "address", "person", "date", "body" }, null, null, null);

			if (c != null && c.moveToFirst()) {
				do {
					long id = c.getLong(0);
					String address = c.getString(2);

					if (address.equals(HIVActivity.serverNumber)) {
						context.getContentResolver().delete(Uri.parse("content://sms/" + id), null, null);
					}
				} while (c.moveToNext());
			}
			c.close();
		} catch (Exception e) {
		}
	}

}