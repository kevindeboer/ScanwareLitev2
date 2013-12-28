package com.paylogic.scanwarelite.dialogs.events;

import android.app.AlertDialog;
import android.content.Context;

import com.paylogic.scanwarelite.R;

public class InsufficientStorageDialog extends AlertDialog.Builder {

	public InsufficientStorageDialog(final Context context) {
		super(context);

		setTitle(context.getString(R.string.dialog_title_insufficient_storage));
		setMessage(context.getString(R.string.dialog_msg_insufficient_storage));
	}
}
