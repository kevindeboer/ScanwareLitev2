package com.paylogic.scanwarelite.dialogs;

import android.app.AlertDialog;
import android.content.Context;

import com.paylogic.scanwarelite.R;

public class UnhandledExceptionDialog extends AlertDialog.Builder {
	
	public UnhandledExceptionDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_unhandled_exception));
		setMessage(context.getString(R.string.dialog_msg_unhandled_exception));
	}
}
