package com.paylogic.scanwarelite.dialogs.scan;

import android.content.Context;

import com.paylogic.scanwarelite.R;

public class DisabledProductDialog extends ScanAlertDialog {

	public DisabledProductDialog(final Context context, String barcode,
			String product) {
		super(context, barcode, R.color.scanDisabled, getTitle(context),
				getMessage(context, product));

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
