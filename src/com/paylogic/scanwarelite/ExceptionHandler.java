package com.paylogic.scanwarelite;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import com.paylogic.scanwarelite.activities.LoginActivity;

public class ExceptionHandler implements
		java.lang.Thread.UncaughtExceptionHandler {

	private Context context;
	private File sdCard;
	private Date now;
	private StringWriter stackTrace;
	private String stackTraceString;
	private FileOutputStream fOut;
	private OutputStreamWriter out;

	public ExceptionHandler(Context context) {
		this.context = context;
		sdCard = Environment.getExternalStorageDirectory();
	}

	@Override
	public void uncaughtException(Thread thread, Throwable exception) {
		stackTrace = new StringWriter();
		exception.printStackTrace(new PrintWriter(stackTrace));
		stackTraceString = stackTrace.toString();
		
		Log.e("Error", stackTraceString);

		now = new Date();
		DateFormat df = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss");
		String nowString = df.format(now);
		String fileName = "ScanwareLite_crash_" + nowString + ".log";
		File logFile = new File(sdCard, fileName);

		try {
			fOut = new FileOutputStream(logFile);
			out = new OutputStreamWriter(fOut);
			out.write(stackTraceString);
			out.close();
		} catch (FileNotFoundException e) {

		} catch (IOException e) {

		} finally {
			Intent intent = new Intent(context, LoginActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.putExtra("error", true);
			context.startActivity(intent);
			
			android.os.Process.killProcess(android.os.Process.myPid());
			System.exit(10);
		}

	}
}
