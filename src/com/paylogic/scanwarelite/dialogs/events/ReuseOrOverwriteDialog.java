package com.paylogic.scanwarelite.dialogs.events;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.ScanwareLiteApplication;
import com.paylogic.scanwarelite.activities.EventsActivity;
import com.paylogic.scanwarelite.activities.ProductsActivity;
import com.paylogic.scanwarelite.activities.EventsActivity.DownloadSpqTask;
import com.paylogic.scanwarelite.models.Event;

public class ReuseOrOverwriteDialog extends AlertDialog.Builder {
	private EventsActivity eventsActivity;
	private ScanwareLiteApplication app;

	
	public ReuseOrOverwriteDialog(final Context context, final Event event) {
		super(context);
		eventsActivity = (EventsActivity) context;
		app = (ScanwareLiteApplication) eventsActivity.getApplication();
		
		setTitle(context.getString(R.string.dialog_title_reuse_or_overwrite));
		setMessage(context.getString(R.string.dialog_msg_reuse_or_overwrite));

		setPositiveButton(context.getString(R.string.dialog_btn_overwrite),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						DownloadSpqTask spqTask = eventsActivity.new DownloadSpqTask(
								app.getUsername(), app.getPassword(), event);
						spqTask.execute();
					}
				});

		setNeutralButton(context.getString(R.string.dialog_btn_reuse),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(context,
								ProductsActivity.class);
						context.startActivity(intent);
					}
				});
		
		setNegativeButton(context.getString(R.string.dialog_btn_cancel),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
	}

}
