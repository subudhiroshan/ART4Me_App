package org.trial.hiv;

import java.text.SimpleDateFormat;
import org.trial.hiv.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteException;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


@SuppressLint("SimpleDateFormat")
public class MsgListAdapter extends ArrayAdapter<String> {
	  String s;
	  ViewHolder holder;
	  SimpleDateFormat msgDate = new java.text.SimpleDateFormat("EEE HH:mm");
	  private final Activity context;
	  private final String[] values;

	  static class ViewHolder {
		    public TextView text;
		    public TextView time;
		    public ImageView image;
		  }
	  
	  public MsgListAdapter(Context context, String[] msgListArray) {
	    super(context, R.layout.msglist_row, msgListArray);
	    this.context = (Activity) context;
	    this.values = msgListArray;
	  }

	@Override
	  public View getView(int position, View convertView, ViewGroup parent) {
	    View rowView = convertView;
	    
	    if (rowView == null) {
	      LayoutInflater inflater = context.getLayoutInflater();
	      rowView = inflater.inflate(R.layout.msglist_row, null);
	      ViewHolder viewHolder = new ViewHolder();
	      viewHolder.image = (ImageView) rowView.findViewById(R.id.icon);
	      viewHolder.text = (TextView) rowView.findViewById(R.id.msgbody);
	      viewHolder.time = (TextView) rowView.findViewById(R.id.timestamp);
	      rowView.setTag(viewHolder);
	    }
	    try{
	    holder = (ViewHolder) rowView.getTag();
	    s = values[position];
	    holder.time.setText(msgDate.format(new java.util.Date(Long.parseLong(s.substring(s.indexOf('*')+1, s.indexOf('#'))))));
	    holder.text.setText(s.substring(s.indexOf('#')+1));
	    }
		catch (SQLiteException se ) {
        	Log.e(getClass().getSimpleName(), "Could not open the cursor");
        }
	    holder.time.setTextColor(Color.GRAY);
	    if (s.substring(0,s.indexOf('*')).equals("person")) {
	      holder.image.setImageResource(R.drawable.mobile_phone);
	      holder.text.setTextColor(Color.BLACK);
	      holder.text.setTypeface(null, Typeface.ITALIC);
	    } else {
	      holder.image.setImageResource(R.drawable.doctor);
	     // holder.text.setBackgroundColor(Color.MAGENTA);
	      holder.text.setTextColor(Color.BLUE);
	    }
	    return rowView;
	    
	  }
	}
