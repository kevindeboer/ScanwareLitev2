package com.paylogic.scanwarelite.dialogs.login;

import android.content.Context;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;

public class EmptyPasswordDialog extends CommonAlertDialog {
	
	
	public EmptyPasswordDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_no_password));
		setMessage(context.getString(R.string.dialog_msg_no_password));
	}
}
