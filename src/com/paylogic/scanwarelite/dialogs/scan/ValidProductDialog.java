package com.paylogic.scanwarelite.dialogs.scan;

import android.content.Context;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.models.Barcode;

public class ValidProductDialog extends ScanAlertDialog {

	public ValidProductDialog(final Context context, Barcode barcode) {
		super(context, barcode, R.color.scanOK, getTitle(context), getMessage(
				context, barcode.getName(), barcode.getProductName()));
	}

	private static String getTitle(Context context) {
		return context.getString(R.string.tv_scan_dialog_title_OK);
	}

	private static String getMessage(Context context, String name,
			String product) {
		return String.format(context.getString(R.string.tv_scan_dialog_msg_OK),
				name, product);
	}
}
