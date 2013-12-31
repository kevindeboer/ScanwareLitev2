package com.paylogic.scanwarelite.dialogs.scan;

import android.app.AlertDialog;
import android.content.Context;

import com.paylogic.scanwarelite.R;

public class InvalidBarcodeLengthDialog extends AlertDialog.Builder {
	
	public InvalidBarcodeLengthDialog(final Context context) {
		super(context);
		setTitle(context.getString(R.string.dialog_title_invalid_barcode_length));
		setMessage(context.getString(R.string.dialog_msg_invalid_barcode_length));
	}
}
