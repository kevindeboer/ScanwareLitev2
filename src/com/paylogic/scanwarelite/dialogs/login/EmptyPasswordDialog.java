package com.paylogic.scanwarelite.dialogs.login;

import android.app.AlertDialog;
import android.content.Context;

import com.paylogic.scanwarelite.R;

public class EmptyPasswordDialog extends AlertDialog.Builder {
	
	
	public EmptyPasswordDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_no_password));
		setMessage(context.getString(R.string.dialog_msg_no_password));
	}
}
