package com.paylogic.scanwarelite.dialogs.login;

import com.paylogic.scanwarelite.R;

import android.app.ProgressDialog;
import android.content.Context;

public class LoginDialog extends ProgressDialog {

	public LoginDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_attempting_login));
		setMessage(context.getString(R.string.dialog_msg_attempting_login));
		setIndeterminate(true);
		setCancelable(false);
	}

}
