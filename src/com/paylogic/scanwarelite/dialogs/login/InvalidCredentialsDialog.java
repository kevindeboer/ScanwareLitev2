package com.paylogic.scanwarelite.dialogs.login;

import android.app.AlertDialog;
import android.content.Context;

import com.paylogic.scanwarelite.R;

public class InvalidCredentialsDialog extends AlertDialog.Builder {

	public InvalidCredentialsDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_invalid_credentials));
		setMessage(context.getString(R.string.dialog_msg_invalid_credentials));
	}
}
