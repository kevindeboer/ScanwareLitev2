package com.paylogic.scanwarelite.dialogs.events;

import com.paylogic.scanwarelite.R;

import android.app.ProgressDialog;
import android.content.Context;

public class DownloadSpqDialog extends ProgressDialog {

	public DownloadSpqDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_retrieving_event_data));
		setMessage(context.getString(R.string.dialog_msg_retrieving_event_data));
		setIndeterminate(true);
		setCancelable(false);
		setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	}

}
