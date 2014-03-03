package org.trial.hiv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Vibrator;
import android.widget.Toast;


@SuppressLint("ViewConstructor")
public class VToast extends Toast {

	public VToast(Context context, String text, int duration) {
		super(context);
		long[] vib = {300,100,300};
		Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	    vibrator.vibrate(vib,-1); //Vibration on toast
	    super.makeText(context, text, duration).show();
	}

}
