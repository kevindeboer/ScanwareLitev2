package com.paylogic.scanwarelite.dialogs.login;

import android.content.Context;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.CommonAlertDialog;

public class EmptyInputDialog extends CommonAlertDialog {

	public EmptyInputDialog(Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_no_credentials));
		setMessage(context.getString(R.string.dialog_msg_no_credentials));
	}
}
