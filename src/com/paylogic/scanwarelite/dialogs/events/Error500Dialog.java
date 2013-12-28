package com.paylogic.scanwarelite.dialogs.events;

import android.app.AlertDialog;
import android.content.Context;

import com.paylogic.scanwarelite.R;

public class Error500Dialog extends AlertDialog.Builder {

	public Error500Dialog(final Context context) {
		super(context);

		setTitle(context.getString(R.string.dialog_title_error_500));
		setMessage(context.getString(R.string.dialog_msg_error_500));
	}
}
