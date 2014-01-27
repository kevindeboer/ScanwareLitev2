package com.paylogic.scanwarelite.dialogs.events;

import android.content.Context;
import android.content.DialogInterface;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.activities.EventsActivity;
import com.paylogic.scanwarelite.activities.EventsActivity.DownloadSpqTask;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;
import com.paylogic.scanwarelite.models.Event;
import com.paylogic.scanwarelite.models.User;
import com.paylogic.scanwarelite.models.User.UserNotFoundException;

public class OverwriteDialog extends CommonAlertDialog {
	private EventsActivity eventsActivity;
	private User user;

	public OverwriteDialog(final Context context, final Event event) {
		super(context);
		eventsActivity = (EventsActivity) context;
		try {
			user = User.getInstance();
		} catch (UserNotFoundException e) {
			e.printStackTrace();
		}

		setTitle(context.getString(R.string.dialog_title_overwrite));
		setMessage(String.format(
				context.getString(R.string.dialog_msg_overwrite),
				event.getName()));

		setButton(BUTTON_POSITIVE,
				context.getString(R.string.dialog_btn_overwrite),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						DownloadSpqTask spqTask = eventsActivity.new DownloadSpqTask(
								user.getUsername(), user.getPassword(), event);
						spqTask.execute();
					}

				});

		setButton(BUTTON_NEGATIVE,
				context.getString(R.string.dialog_btn_cancel),
				new OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
	}

}
