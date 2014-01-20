package com.paylogic.scanwarelite.dialogs.events;

import android.content.Context;
import android.content.DialogInterface;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.ScanwareLiteApplication;
import com.paylogic.scanwarelite.activities.EventsActivity;
import com.paylogic.scanwarelite.activities.EventsActivity.DownloadSpqTask;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;
import com.paylogic.scanwarelite.models.Event;
import com.paylogic.scanwarelite.models.User;

public class DownloadDialog extends CommonAlertDialog {
	private EventsActivity eventsActivity;
	private ScanwareLiteApplication app;
	private User user;
	public DownloadDialog(final Context context, final Event event) {
		super(context);
		setCanceledOnTouchOutside(false);
		eventsActivity = (EventsActivity) context;
		app = (ScanwareLiteApplication) eventsActivity.getApplication();
		user= app.getUser();
		
		setTitle(context.getString(R.string.dialog_title_confirm_download));
		setMessage(String.format(context.getString(R.string.dialog_msg_confirm_download), event.getName()));
		
		setButton(BUTTON_POSITIVE, context.getString(R.string.dialog_btn_download), new OnClickListener() {
			
				public void onClick(DialogInterface dialog, int which) {
					DownloadSpqTask spqTask = eventsActivity.new DownloadSpqTask(
							user.getUsername(), user.getPassword(), event);
					spqTask.execute();
				}
			
		});
		
		setButton(BUTTON_NEGATIVE, context.getString(R.string.dialog_btn_cancel), new OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		});
	}

}
