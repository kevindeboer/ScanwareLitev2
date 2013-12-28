package com.paylogic.scanwarelite.dialogs;

import android.app.AlertDialog;
import android.content.Context;

import com.paylogic.scanwarelite.R;

public class EmptyInputDialog extends AlertDialog.Builder {

	public EmptyInputDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_no_credentials));
		setMessage(context.getString(R.string.dialog_msg_no_credentials));
	}
}
