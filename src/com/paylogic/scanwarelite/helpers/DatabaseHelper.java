package com.paylogic.scanwarelite.helpers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_PATH = "/data/data/com.paylogic.scanwarelite/databases/";

	public static final String DATABASE_NAME = "event.spq";

	public static final String DATABASE_FILEPATH = DATABASE_PATH
			+ DATABASE_NAME;
	public static final int DATABASE_VERSION = 1;

	public static final String EVENTS_TABLE = "module";
	public static final String PRODUCTS_TABLE = "products";
	public static final String BARCODES_TABLE = "barcodes";
	public static final String USER_TABLE = "user";

	public static final String EVENT_KEY_ID = "id";
	public static final String EVENT_KEY_TITLE = "title";

	public static final String PRODUCTS_KEY_ID = "id";
	public static final String PRODUCTS_KEY_TITLE = "title";
	public static final String PRODUCTS_KEY_ALLOWED = "allowed";

	public static final String BARCODES_KEY_CODE = "code";
	public static final String BARCODES_KEY_NAME = "name";
	public static final String BARCODES_KEY_PAYSTATUS = "payStatus";
	public static final String BARCODES_KEY_PRODUCTID = "productID";
	public static final String BARCODES_KEY_SEEN = "seen";
	public static final String BARCODES_KEY_SEENDATE = "seenDate";
	public static final String BARCODES_KEY_SCANNER = "scanner";

	public static final String USER_KEY_ID = "id";

	private Context context;

	public DatabaseHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		this.context = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public boolean databaseExists() {
		return context.getDatabasePath(DatabaseHelper.DATABASE_NAME).exists();
	}

	public void importDatabase(InputStream source) throws IOException {
		close();
		if (source != null) {
			
			// Create /data/data/com.paylogic.scanwarelite/databases/ if it doesnt exist yet
			File dbPath = new File(DATABASE_PATH);
			if (!dbPath.exists()) {
				dbPath.mkdirs();
			}
			OutputStream mOutput = new FileOutputStream(DATABASE_PATH
					+ getDatabaseName());
			
			// Write contents of source to database file
			byte[] buffer = new byte[1024];
			int length;
			while ((length = source.read(buffer)) > 0) {
				mOutput.write(buffer, 0, length);
			}

			mOutput.flush();
			mOutput.close();
			source.close();

			getWritableDatabase().close();

		}
	}
}
