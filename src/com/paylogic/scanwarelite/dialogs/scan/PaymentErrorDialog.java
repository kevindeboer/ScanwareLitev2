package com.paylogic.scanwarelite.dialogs.scan;

import android.content.Context;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.models.Barcode;

public class PaymentErrorDialog extends ScanAlertDialog {

	public PaymentErrorDialog(Context context, Barcode barcode) {
		super(context, barcode, R.color.scanInvalid, getTitle(context),
				getMessage(context));
	}

	private static String getTitle(Context context) {
		return context.getString(R.string.tv_scan_dialog_title_invalid);
	}

	private static String getMessage(Context context) {
		return context.getString(R.string.tv_scan_dialog_msg_payment_error);
	}
}
