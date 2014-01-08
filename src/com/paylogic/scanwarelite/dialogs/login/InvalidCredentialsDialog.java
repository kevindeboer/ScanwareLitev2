package com.paylogic.scanwarelite.dialogs.login;

import android.content.Context;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;

public class InvalidCredentialsDialog extends CommonAlertDialog {

	public InvalidCredentialsDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_invalid_credentials));
		setMessage(context.getString(R.string.dialog_msg_invalid_credentials));
	}
}
