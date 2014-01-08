package com.paylogic.scanwarelite.dialogs.events;

import android.content.Context;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;

public class InsufficientStorageDialog extends CommonAlertDialog {

	public InsufficientStorageDialog(final Context context) {
		super(context);

		setTitle(context.getString(R.string.dialog_title_insufficient_storage));
		setMessage(context.getString(R.string.dialog_msg_insufficient_storage));
	}
}
