package com.paylogic.scanwarelite.dialogs.events;

import android.content.Context;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;

public class NoResourcesDialog extends CommonAlertDialog {

	public NoResourcesDialog(final Context context) {
		super(context);

		setTitle(context.getString(R.string.dialog_title_no_resources));
		setMessage(context.getString(R.string.dialog_msg_no_resources));
	}
}
