package com.paylogic.scanwarelite.dialogs.scan;

import android.content.Context;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.models.Barcode;

public class DisabledProductDialog extends ScanAlertDialog {

	public DisabledProductDialog(Context context, Barcode barcode) {
		super(context, barcode, R.color.scanDisabled, getTitle(context),
				getMessage(context, barcode.getProductName()));
	}

	private static String getTitle(Context context) {
		return context.getString(R.string.tv_scan_dialog_title_disabled);
	}

	private static String getMessage(Context context, String product) {
		return String
				.format(context
						.getString(R.string.tv_scan_dialog_msg_product_disabled),
						product);
	}
}
