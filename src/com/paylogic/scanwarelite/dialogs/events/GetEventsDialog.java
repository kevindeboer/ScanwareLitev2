package com.paylogic.scanwarelite.dialogs.events;

import com.paylogic.scanwarelite.R;

import android.app.ProgressDialog;
import android.content.Context;

public class GetEventsDialog extends ProgressDialog {

	public GetEventsDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_retrieving_events));
		setMessage(context.getString(R.string.dialog_msg_retrieving_events));
		setIndeterminate(true);
		setCancelable(false);
	}

}
