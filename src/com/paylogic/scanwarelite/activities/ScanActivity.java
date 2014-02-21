package com.paylogic.scanwarelite.activities;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.paylogic.scanwarelite.BarcodeProcessor;
import com.paylogic.scanwarelite.BarcodeProcessor.InvalidBarcodeLengthException;
import com.paylogic.scanwarelite.BarcodeProcessor.InvalidChecksumException;
import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.scan.AlreadySeenDialog;
import com.paylogic.scanwarelite.dialogs.scan.BarcodeNotFoundDialog;
import com.paylogic.scanwarelite.dialogs.scan.DisabledProductDialog;
import com.paylogic.scanwarelite.dialogs.scan.InitCameraDialog;
import com.paylogic.scanwarelite.dialogs.scan.InvalidBarcodeDialog;
import com.paylogic.scanwarelite.dialogs.scan.InvalidBarcodeLengthDialog;
import com.paylogic.scanwarelite.dialogs.scan.ManualInputDialog;
import com.paylogic.scanwarelite.dialogs.scan.PaymentErrorDialog;
import com.paylogic.scanwarelite.dialogs.scan.ValidProductDialog;
import com.paylogic.scanwarelite.helpers.DatabaseHelper;
import com.paylogic.scanwarelite.models.Barcode;
import com.paylogic.scanwarelite.views.CameraPreview;

public class ScanActivity extends CommonActivity {
	static {
		System.loadLibrary("iconv");
	}

	private BarcodeProcessor barcodeProcessor;

	private TextView startScanningView;
	private CameraPreview mPreview;
	private Camera mCamera;
	private ImageScanner scanner;
	private InitCameraTask initCameraTask;
	private MediaPlayer mediaPlayer;
	private boolean hasFlashlight;
	private boolean flashlightEnabled;
	private Barcode barcode;
	private boolean scanning = false;
	private Handler autoFocusHandler;
	private Runnable doAutoFocus = new Runnable() {
		public void run() {
			if (mCamera != null) {
				mCamera.autoFocus(autoFocusCB);
			}
		}
	};

	private AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};

	private PreviewCallback previewCb = new PreviewCallback() {
		public void onPreviewFrame(byte[] data, Camera camera) {
			if (scanning) {
				Camera.Parameters parameters = camera.getParameters();
				Size size = parameters.getPreviewSize();

				Image barcodeImage = new Image(size.width, size.height, "Y800");
				barcodeImage.setData(data);

				int result = scanner.scanImage(barcodeImage);
				if (result != 0) {
					stopScanning();

					String barcodeString = null;

					SymbolSet syms = scanner.getResults();
					for (Symbol sym : syms) {
						barcodeString = sym.getData();
						// Im only interested in the first result, but there is
						// no way to index SymbolSet
						break;
					}

					processBarcode(barcodeString);
				}
			}
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		barcodeProcessor = new BarcodeProcessor(databaseHelper);
		hasFlashlight = getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA_FLASH);

	}

	@Override
	protected void onResume() {
		super.onResume();
		initCameraTask = new InitCameraTask();
		initCameraTask.execute();
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
		startScanningView.setVisibility(View.GONE);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_scan, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		MenuItem flashlightMenuItem = menu.findItem(R.id.menu_flashlight);
		MenuItem manualInputMenuitem = menu.findItem(R.id.menu_manual_input);
		MenuItem pauseScanningMenuItem = menu
				.findItem(R.id.menu_pause_scanning);

		if (hasFlashlight) {
			if (!flashlightEnabled) {
				flashlightMenuItem.setTitle(resources
						.getString(R.string.menu_item_enable_flashlight));
			} else {
				flashlightMenuItem.setTitle(resources
						.getString(R.string.menu_item_disable_flashlight));
			}
		} else {
			menu.removeItem(R.id.menu_flashlight);
		}

		if (!scanning) {
			pauseScanningMenuItem.setVisible(false);
			manualInputMenuitem.setVisible(false);

		} else {
			pauseScanningMenuItem.setVisible(true);
			manualInputMenuitem.setVisible(true);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menu_manual_input:
			alertDialog = new ManualInputDialog(ScanActivity.this);
			alertDialog.show();
			break;

		case R.id.menu_settings:
			flashlightEnabled = false;
			intent = new Intent(ScanActivity.this, SettingsActivity.class);
			startActivity(intent);
			break;

		case R.id.menu_flashlight:

			Parameters p = mCamera.getParameters();
			if (!flashlightEnabled) {
				p.setFlashMode(Parameters.FLASH_MODE_TORCH);
				flashlightEnabled = true;
			} else {
				p.setFlashMode(Parameters.FLASH_MODE_OFF);
				flashlightEnabled = false;
			}
			mCamera.setParameters(p);

			break;

		case R.id.menu_pause_scanning:
			stopScanning();
			startScanningView.setVisibility(View.VISIBLE);
			break;

		case R.id.menu_view_statistics:
			intent = new Intent(ScanActivity.this, StatisticsActivity.class);
			startActivity(intent);
		}

		return true;
	}

	public static Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open();
		} catch (Exception e) {
		}
		return c;
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mPreview.getHolder().removeCallback(mPreview);
			mCamera.release();
			mCamera = null;
		}
	}

	public void dismissDialog() {
		alertDialog.dismiss();
	}

	public void startScanning() {
		scanning = true;
		invalidateOptionsMenu();
	}

	public void stopScanning() {
		scanning = false;
		invalidateOptionsMenu();
	}

	public void processBarcode(String barcodeString) {
		try {
			barcode = barcodeProcessor.process(barcodeString);
			if (!barcode.isFound()) {
				showBarcodeNotFoundDialog();
				return;
			}
			if (!barcode.isPaymentValid()) {
				showPaymentErrorDialog();
				return;
			}

			if (barcode.isSeen()) {
				showAlreadySeenDialog();
				return;
			}

			if (!barcode.isAllowed()) {
				showDisabledProductDialog();
				return;
			}

			showValidBarcodeDialog();
			barcodeProcessor.markBarcodeAsSeen(barcode);

		} catch (InvalidChecksumException e) {
			alertDialog = new InvalidBarcodeDialog(ScanActivity.this, barcode);
			alertDialog.show();

			mediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.invalid);
			mediaPlayer.start();
		} catch (InvalidBarcodeLengthException e) {
			alertDialog = new InvalidBarcodeLengthDialog(ScanActivity.this);
			alertDialog.show();
			
			mediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.invalid);
			mediaPlayer.start();
		}
	}

	private void showBarcodeNotFoundDialog() {
		alertDialog = new BarcodeNotFoundDialog(ScanActivity.this, barcode);
		alertDialog.show();

		mediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.invalid);
		mediaPlayer.start();
	}

	private void showValidBarcodeDialog() {
		alertDialog = new ValidProductDialog(ScanActivity.this, barcode);
		alertDialog.show();

		mediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.valid);
		mediaPlayer.start();
	}

	private void showDisabledProductDialog() {
		alertDialog = new DisabledProductDialog(ScanActivity.this, barcode);
		alertDialog.show();

		mediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.invalid);
		mediaPlayer.start();
	}

	private void showAlreadySeenDialog() {
		alertDialog = new AlreadySeenDialog(ScanActivity.this, barcode);
		alertDialog.show();

		mediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.invalid);
		mediaPlayer.start();
	}

	private void showPaymentErrorDialog() {
		alertDialog = new PaymentErrorDialog(ScanActivity.this, barcode);
		alertDialog.show();

		mediaPlayer = MediaPlayer.create(ScanActivity.this, R.raw.invalid);
		mediaPlayer.start();
	}

	private class InitCameraTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			progressDialog = new InitCameraDialog(ScanActivity.this);
			progressDialog.show();
			setContentView(R.layout.activity_scan);
			startScanningView = (TextView) findViewById(R.id.textView_start_scanning);
		}

		@Override
		protected void onPostExecute(Void result) {

			startScanningView = (TextView) findViewById(R.id.textView_start_scanning);

			autoFocusHandler = new Handler();

			progressDialog.dismiss();
		}

		@Override
		protected Void doInBackground(Void... params) {
			mCamera = getCameraInstance();

			scanner = new ImageScanner();
			scanner.setConfig(0, Config.X_DENSITY, 3);
			scanner.setConfig(0, Config.Y_DENSITY, 3);
			runOnUiThread(new Runnable() {
				public void run() {
					FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
					mPreview = new CameraPreview(ScanActivity.this, mCamera,
							previewCb, autoFocusCB);

					preview.addView(mPreview);
					mCamera.startPreview();
					if (!scanning) {
						startScanningView.setVisibility(View.VISIBLE);
					}
					mPreview.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (!scanning) {
								Toast.makeText(ScanActivity.this,
										"Starting to scan", Toast.LENGTH_LONG)
										.show();
								startScanning();
								startScanningView.setVisibility(View.GONE);
							}
						}
					});

				}
			});

			return null;
		}

	}
}
