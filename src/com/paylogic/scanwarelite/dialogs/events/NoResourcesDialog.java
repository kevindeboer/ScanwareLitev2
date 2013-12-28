package com.paylogic.scanwarelite.dialogs.events;

import android.app.AlertDialog;
import android.content.Context;

import com.paylogic.scanwarelite.R;

public class NoResourcesDialog extends AlertDialog.Builder {

	public NoResourcesDialog(final Context context) {
		super(context);

		setTitle(context.getString(R.string.dialog_title_no_resources));
		setMessage(context.getString(R.string.dialog_msg_no_resources));
	}
}
