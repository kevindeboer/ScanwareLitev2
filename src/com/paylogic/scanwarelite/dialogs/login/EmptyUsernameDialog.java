package com.paylogic.scanwarelite.dialogs.login;

import android.content.Context;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;

public class EmptyUsernameDialog extends CommonAlertDialog {

	public EmptyUsernameDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_no_username));
		setMessage(context.getString(R.string.dialog_msg_no_username));
	}
}
