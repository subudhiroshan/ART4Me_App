package org.trial.hiv;

import java.io.File;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class SQLiteHelper extends SQLiteOpenHelper {

	public static SQLiteDatabase DB;
	public static String DBPath;
	public static String DBName = "ART4Me.db";
	public static final int version = '1';
	public static Context currentContext;
	public static String tableName = "Messages";

	public SQLiteHelper(Context context) {
		super(context, DBName, null, version);
		currentContext = context;
		if(android.os.Build.VERSION.SDK_INT >= 17){
		       DBPath = context.getApplicationInfo().dataDir + "/databases/";         
		    }
		    else
		    {
		       DBPath = "/data/data/" + context.getPackageName() + "/databases/";
		    }
	}

	@Override
	public void onCreate(SQLiteDatabase arg0) {
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
	}

	public static void createDatabase(Context context) {
		try{
			currentContext = context;
			if(android.os.Build.VERSION.SDK_INT >= 17){
			       DBPath = context.getApplicationInfo().dataDir + "/databases/";         
			    }
			    else
			    {
			       DBPath = "/data/data/" + context.getPackageName() + "/databases/";
			    }
			
			if (!checkDbExists(context))
			{
				DB = currentContext.openOrCreateDatabase(DBPath + DBName, Context.MODE_PRIVATE, null);
				DB.execSQL("CREATE TABLE IF NOT EXISTS " + tableName +	" (Timestamp VARCHAR(30), MessageBody VARCHAR(160), Sender VARCHAR(4));");
				DB.close();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private static boolean checkDbExists(Context context) {
		
		File dbFile = context.getDatabasePath(DBName);
		return dbFile.exists();
	}

	public static boolean insertIntoDatabase(String timeStamp, String msgBody, String send){
		ContentValues contentValues = new ContentValues();
		contentValues.put("Timestamp", timeStamp);
		contentValues.put("MessageBody", msgBody);
		contentValues.put("Sender", send);
		DB = currentContext.openOrCreateDatabase(DBName, Context.MODE_PRIVATE, null);
		if (DB.insert(tableName, null, contentValues)<0){
			DB.close();
			return false;
		}
		else{
			DB.close();
			return true;
		}
	}

	public void close(){
		super.close();
	}

	public static void clearDatabase(){
		DB = currentContext.openOrCreateDatabase(DBName, 0, null);
		DB.execSQL("DELETE FROM " + tableName + ";");
		DB.close();
	}
}
