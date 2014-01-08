package com.paylogic.scanwarelite.dialogs;

import android.content.Context;

import com.paylogic.scanwarelite.R;

public class UnhandledExceptionDialog extends CommonAlertDialog {
	
	public UnhandledExceptionDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_unhandled_exception));
		setMessage(context.getString(R.string.dialog_msg_unhandled_exception));
	}
}
