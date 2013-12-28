package com.paylogic.scanwarelite.dialogs.login;

import android.app.AlertDialog;
import android.content.Context;

import com.paylogic.scanwarelite.R;

public class EmptyUsernameDialog extends AlertDialog.Builder {

	public EmptyUsernameDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_no_username));
		setMessage(context.getString(R.string.dialog_msg_no_username));
	}
}
