package com.paylogic.scanwarelite.helpers;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.text.InputFilter;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.activities.ScanActivity;
import com.paylogic.scanwarelite.models.Event;

public class DialogHelper {
	public static final int DIALOG_POSITIVE_BUTTON_LISTENER = 300;
	public static final int DIALOG_NEUTRAL_BUTTON_LISTENER = 301;
	public static final int DIALOG_NEGATIVE_BUTTON_LISTENER = 302;
	public static final int VIEW_ON_CLICK_LISTENER = 303;

	// Dialogs used in EventsActivity
	public static final int ONLY_REUSE_DIALOG = 7;
	public static final int REUSE_OR_OVERWRITE_DIALOG = 8;
	public static final int OVERWRITE_DIALOG = 9;
	public static final int DOWNLOAD_DIALOG = 10;
	public static final int NO_RESOURCES_DIALOG = 11;
	public static final int GET_EVENTS_DIALOG = 12;
	public static final int DOWNLOAD_SPQ_DIALOG = 13;
	public static final int ERROR_500_DIALOG = 14;
	public static final int NOT_ENOUGH_DISK_SPACE_DIALOG = 15;
	// Dialogs used in menus
	public static final int EXIT_DIALOG = 16;
	public static final int MANUAL_INPUT_DIALOG = 17;

	// Dialogs used in SettingsActivity
	public static final int DELETE_DATA_DIALOG = 18;

	// Dialogs used in ScanActivity
	public static final int INVALID_BARCODE_DIALOG = 19;
	public static final int BARCODE_NOT_FOUND_DIALOG = 20;
	public static final int PAYMENT_ERROR_DIALOG = 21;
	public static final int ALREADY_SEEN_DIALOG = 22;
	public static final int INIT_CAMERA_DIALOG = 23;


	public static AlertDialog createAlertDialogById(Context context, int id) {
		return createAlertDialogById(context, id, null, null);
	}

	public static AlertDialog createAlertDialogById(Context context, int id,
			Event event) {
		return createAlertDialogById(context, id, null, event);
	}

	public static AlertDialog createAlertDialogById(Context context, int id,
			SparseArray<OnClickListener> dialogHandlers) {
		return createAlertDialogById(context, id, dialogHandlers, null);
	}

	public static AlertDialog createAlertDialogById(Context context, int id,
			SparseArray<OnClickListener> dialogHandlers, Event event) {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		switch (id) {
		case ONLY_REUSE_DIALOG:
			alertDialogBuilder = createOnlyReuseDialog(context, dialogHandlers,
					alertDialogBuilder);
			break;
		case REUSE_OR_OVERWRITE_DIALOG:
			alertDialogBuilder = createReuseOrOverwriteDialog(context,
					dialogHandlers, alertDialogBuilder);
			break;
		case OVERWRITE_DIALOG:
			alertDialogBuilder = createOverwriteDialog(context, dialogHandlers,
					event, alertDialogBuilder);
			break;
		case DOWNLOAD_DIALOG:
			alertDialogBuilder = createDownloadDialog(context, dialogHandlers,
					event, alertDialogBuilder);
			break;
		case NO_RESOURCES_DIALOG:
			alertDialogBuilder = createNoResourcesDialog(context,
					alertDialogBuilder);
			break;

		case ERROR_500_DIALOG:
			alertDialogBuilder = createError500Dialog(context,
					alertDialogBuilder);
			break;
		case NOT_ENOUGH_DISK_SPACE_DIALOG:
			alertDialogBuilder = createNotEnoughDiskSpaceDialog(context,
					alertDialogBuilder);
			break;
		case EXIT_DIALOG:
			alertDialogBuilder = createExitDialog(context, dialogHandlers,
					alertDialogBuilder);
			break;
		case INIT_CAMERA_DIALOG:
			alertDialogBuilder = createInitCameraDialog(context,
					alertDialogBuilder);
			break;
		case MANUAL_INPUT_DIALOG:
			alertDialogBuilder = createManualInputDialog(context);
			break;

		case DELETE_DATA_DIALOG:
			alertDialogBuilder = createDeleteDataDialog(context,
					dialogHandlers, alertDialogBuilder);
			break;
		}
		return alertDialogBuilder.create();
	}

	private static AlertDialog.Builder createInitCameraDialog(Context context,
			Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_loading_camera));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_loading_camera));
		return alertDialogBuilder;
	}

	public static AlertDialog createInvalidScanAlertDialog(Context context,
			int id, SparseArray<View.OnClickListener> handlers, String barcode) {
		return createInvalidScanAlertDialog(context, id, handlers, barcode,
				null);
	}

	public static AlertDialog createInvalidScanAlertDialog(Context context,
			int id, SparseArray<View.OnClickListener> viewHandlers,
			String barcode, String seenDate) {

		Resources resources = context.getResources();
		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		View scanDialogView = li.inflate(R.layout.barcode_dialog_view, null);

		TextView scanResultTitleView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_title);
		TextView scanResultBarcodeView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_barcode);
		TextView scanResultMessageView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_message);

		scanDialogView.setBackgroundColor(resources
				.getColor(R.color.scanInvalid));
		scanResultTitleView.setText(context
				.getString(R.string.tv_scan_dialog_title_invalid));
		scanResultBarcodeView.setText(String.format(context.getString(
				R.string.tv_scan_dialog_barcode, barcode)));

		scanDialogView.setOnClickListener(viewHandlers
				.get(VIEW_ON_CLICK_LISTENER));

		alertDialogBuilder.setView(scanDialogView);

		switch (id) {
		case INVALID_BARCODE_DIALOG:
			scanResultMessageView.setText(context
					.getString(R.string.tv_scan_dialog_msg_invalid_checksum));
			return alertDialogBuilder.create();

		case BARCODE_NOT_FOUND_DIALOG:
			scanResultMessageView.setText(context
					.getString(R.string.tv_scan_dialog_msg_not_found));
			return alertDialogBuilder.create();

		case PAYMENT_ERROR_DIALOG:
			scanResultMessageView.setText(context
					.getString(R.string.tv_scan_dialog_msg_payment_error));
			return alertDialogBuilder.create();

		case ALREADY_SEEN_DIALOG:
			scanResultMessageView.setText(String.format(context
					.getString(R.string.tv_scan_dialog_msg_already_scanned),
					seenDate));
			return alertDialogBuilder.create();
		default:
			return null;
		}
	}

	public static AlertDialog createDisabledScanAlertDialog(Context context,
			SparseArray<View.OnClickListener> handlers, String barcode,
			String product) {

		Resources resources = context.getResources();
		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		View scanDialogView = li.inflate(R.layout.barcode_dialog_view, null);

		TextView scanResultTitleView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_title);
		TextView scanResultBarcodeView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_barcode);
		TextView scanResultMessageView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_message);

		scanDialogView.setBackgroundColor(resources
				.getColor(R.color.scanDisabled));
		scanResultTitleView.setText(context
				.getString(R.string.tv_scan_dialog_title_disabled));
		scanResultBarcodeView.setText(String.format(context.getString(
				R.string.tv_scan_dialog_barcode, barcode)));
		scanResultMessageView
				.setText(String.format(
						context.getString(R.string.tv_scan_dialog_msg_product_disabled),
						product));
		scanDialogView.setOnClickListener(handlers.get(VIEW_ON_CLICK_LISTENER));

		alertDialogBuilder.setView(scanDialogView);
		return alertDialogBuilder.create();
	}

	public static AlertDialog createValidScanAlertDialog(Context context,
			SparseArray<View.OnClickListener> handlers, String barcode,
			String product, String name) {

		Resources resources = context.getResources();
		LayoutInflater li = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				context);

		View scanDialogView = li.inflate(R.layout.barcode_dialog_view, null);

		TextView scanResultTitleView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_title);
		TextView scanResultBarcodeView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_barcode);
		TextView scanResultMessageView = (TextView) scanDialogView
				.findViewById(R.id.textView_result_message);

		scanDialogView.setBackgroundColor(resources.getColor(R.color.scanOK));
		scanResultTitleView.setText(context
				.getString(R.string.tv_scan_dialog_title_OK));
		scanResultBarcodeView.setText(String.format(context.getString(
				R.string.tv_scan_dialog_barcode, barcode)));
		scanResultMessageView.setText(String.format(
				context.getString(R.string.tv_scan_dialog_msg_OK), name,
				product));
		scanDialogView.setOnClickListener(handlers.get(VIEW_ON_CLICK_LISTENER));

		alertDialogBuilder.setView(scanDialogView);
		return alertDialogBuilder.create();
	}

	public static ProgressDialog createProgressDialogById(Context context,
			int id) {
		ProgressDialog progressDialog = new ProgressDialog(context);

		switch (id) {
		case GET_EVENTS_DIALOG:
			progressDialog.setTitle(context
					.getString(R.string.dialog_title_retrieving_events));
			progressDialog.setMessage(context
					.getString(R.string.dialog_msg_retrieving_events));
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(true);
			break;
		case DOWNLOAD_SPQ_DIALOG:
			progressDialog.setTitle(context
					.getString(R.string.dialog_title_retrieving_event_data));
			progressDialog.setMessage(context
					.getString(R.string.dialog_msg_retrieving_event_data));
			progressDialog.setIndeterminate(true);
			progressDialog.setCancelable(false);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			break;
		}
		return progressDialog;
	}

	private static AlertDialog.Builder createDeleteDataDialog(Context context,
			SparseArray<OnClickListener> dialogHandlers,
			AlertDialog.Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_delete_data));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_delete_data));
		alertDialogBuilder.setPositiveButton(
				context.getString(R.string.dialog_btn_ok),
				dialogHandlers.get(DIALOG_POSITIVE_BUTTON_LISTENER));
		alertDialogBuilder.setNegativeButton(
				context.getString(R.string.dialog_btn_cancel),
				dialogHandlers.get(DIALOG_NEGATIVE_BUTTON_LISTENER));
		return alertDialogBuilder;
	}

	private static AlertDialog.Builder createManualInputDialog(Context context) {
		final ScanActivity scannActivity = (ScanActivity) context;
		scannActivity.setRunning(false);
		
		AlertDialog.Builder alertDialogBuilder;
		alertDialogBuilder = new AlertDialog.Builder(context);
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_manual_input));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_manual_input));

		final EditText input = new EditText(context);

		InputFilter[] filterArray = new InputFilter[1];
		filterArray[0] = new InputFilter.LengthFilter(13);
		input.setFilters(filterArray);
		input.setRawInputType(Configuration.KEYBOARD_QWERTY);
		input.setSingleLine(true);
		alertDialogBuilder.setView(input);
		
		alertDialogBuilder.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						String barcode = input.getText().toString();

						scannActivity.processBarcode(barcode);
						scannActivity.setRunning(true);
					}
				});

		alertDialogBuilder.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						scannActivity.setRunning(true);
					}
				});
		return alertDialogBuilder;
	}

	private static AlertDialog.Builder createExitDialog(Context context,
			SparseArray<OnClickListener> dialogHandlers,
			AlertDialog.Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_confirm_exit));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_confirm_exit));
		alertDialogBuilder.setPositiveButton(
				context.getString(R.string.dialog_btn_exit),
				dialogHandlers.get(DIALOG_POSITIVE_BUTTON_LISTENER));
		alertDialogBuilder.setNegativeButton(
				context.getString(R.string.dialog_btn_cancel),
				dialogHandlers.get(DIALOG_NEGATIVE_BUTTON_LISTENER));
		return alertDialogBuilder;
	}

	private static AlertDialog.Builder createNoResourcesDialog(Context context,
			AlertDialog.Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_no_resources));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_no_resources));
		return alertDialogBuilder;
	}

	private static AlertDialog.Builder createDownloadDialog(Context context,
			SparseArray<OnClickListener> dialogHandlers, Event event,
			AlertDialog.Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_confirm_download));
		alertDialogBuilder.setMessage(String.format(
				context.getString(R.string.dialog_msg_confirm_download),
				event.getId()));
		alertDialogBuilder.setPositiveButton(
				context.getString(R.string.dialog_btn_download),
				dialogHandlers.get(DIALOG_POSITIVE_BUTTON_LISTENER));
		alertDialogBuilder.setNegativeButton(
				context.getString(R.string.dialog_btn_cancel),
				dialogHandlers.get(DIALOG_NEGATIVE_BUTTON_LISTENER));
		return alertDialogBuilder;
	}

	private static AlertDialog.Builder createOverwriteDialog(Context context,
			SparseArray<OnClickListener> dialogHandlers, Event event,
			AlertDialog.Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_overwrite));
		alertDialogBuilder.setMessage(String.format(
				context.getString(R.string.dialog_msg_overwrite), event.getId()));
		alertDialogBuilder.setPositiveButton(
				context.getString(R.string.dialog_btn_overwrite),
				dialogHandlers.get(DIALOG_POSITIVE_BUTTON_LISTENER));
		alertDialogBuilder.setNegativeButton(
				context.getString(R.string.dialog_btn_cancel),
				dialogHandlers.get(DIALOG_NEGATIVE_BUTTON_LISTENER));
		return alertDialogBuilder;
	}

	private static AlertDialog.Builder createReuseOrOverwriteDialog(
			Context context, SparseArray<OnClickListener> dialogHandlers,
			AlertDialog.Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_reuse_or_overwrite));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_reuse_or_overwrite));
		alertDialogBuilder.setPositiveButton(
				context.getString(R.string.dialog_btn_overwrite),
				dialogHandlers.get(DIALOG_POSITIVE_BUTTON_LISTENER));
		alertDialogBuilder.setNeutralButton(
				context.getString(R.string.dialog_btn_reuse),
				dialogHandlers.get(DIALOG_NEUTRAL_BUTTON_LISTENER));
		alertDialogBuilder.setNegativeButton(
				context.getString(R.string.dialog_btn_cancel),
				dialogHandlers.get(DIALOG_NEGATIVE_BUTTON_LISTENER));
		return alertDialogBuilder;
	}

	private static AlertDialog.Builder createOnlyReuseDialog(Context context,
			SparseArray<OnClickListener> dialogHandlers,
			AlertDialog.Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_only_reuse));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_only_reuse));
		alertDialogBuilder.setPositiveButton(
				context.getString(R.string.dialog_btn_reuse),
				dialogHandlers.get(DIALOG_POSITIVE_BUTTON_LISTENER));
		alertDialogBuilder.setNegativeButton(
				context.getString(R.string.dialog_btn_cancel),
				dialogHandlers.get(DIALOG_NEGATIVE_BUTTON_LISTENER));
		return alertDialogBuilder;
	}
	
	private static AlertDialog.Builder createError500Dialog(Context context,
			Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_error_500));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_error_500));
		return alertDialogBuilder;
	}
	private static AlertDialog.Builder createNotEnoughDiskSpaceDialog(Context context,
			Builder alertDialogBuilder) {
		alertDialogBuilder.setTitle(context
				.getString(R.string.dialog_title_not_enough_disk_space));
		alertDialogBuilder.setMessage(context
				.getString(R.string.dialog_msg_not_enough_disk_space));
		return null;
	}
}
