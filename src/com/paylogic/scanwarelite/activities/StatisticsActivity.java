package com.paylogic.scanwarelite.activities;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.helpers.ScanwareLiteOpenHelper;
import com.paylogic.scanwarelite.models.Product;

public class StatisticsActivity extends CommonActivity {

	private ArrayList<Product> products;
	private StatisticsAdapter statisticsAdapter;
	private ListView statisticsView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_statistics);
		products = new ArrayList<Product>();
		statisticsView = (ListView) findViewById(R.id.listView_statistics);

	}

	protected void onResume() {
		super.onResume();
		db = scanwareLiteOpenHelper.getReadableDatabase();
		products = getProducts(db);
		getTotalTicketsForProducts(db);
		getCheckedInTicketsForProducts(db);

		statisticsAdapter = new StatisticsAdapter(StatisticsActivity.this,
				android.R.layout.simple_list_item_1, products);
		statisticsView.setAdapter(statisticsAdapter);
	}

	private ArrayList<Product> getProducts(SQLiteDatabase db) {
		ArrayList<Product> products = new ArrayList<Product>();
		String[] columns = new String[] {
				ScanwareLiteOpenHelper.PRODUCTS_KEY_ID,
				ScanwareLiteOpenHelper.PRODUCTS_KEY_TITLE,
				ScanwareLiteOpenHelper.PRODUCTS_KEY_ALLOWED };
		String whereClause = ScanwareLiteOpenHelper.PRODUCTS_KEY_ALLOWED
				+ "= ?";
		String[] whereArgs = { "1" };

		Cursor cursor = db.query(ScanwareLiteOpenHelper.PRODUCTS_TABLE,
				columns, whereClause, whereArgs, null, null, null);
		while (cursor.moveToNext()) {
			int id = cursor.getInt(cursor
					.getColumnIndex(ScanwareLiteOpenHelper.PRODUCTS_KEY_ID));
			String title = cursor.getString(cursor
					.getColumnIndex(ScanwareLiteOpenHelper.PRODUCTS_KEY_TITLE));
			boolean allowed = cursor
					.getInt(cursor
							.getColumnIndex(ScanwareLiteOpenHelper.PRODUCTS_KEY_ALLOWED)) > 0;
			if (allowed) {
				Product product = new Product(id, title, allowed);
				products.add(product);
			}
		}
		return products;
	}

	private void getTotalTicketsForProducts(SQLiteDatabase db) {
		String query = "SELECT "
				+ ScanwareLiteOpenHelper.BARCODES_KEY_PRODUCTID
				+ ", COUNT(*) FROM " + ScanwareLiteOpenHelper.BARCODES_TABLE
				+ " WHERE " + ScanwareLiteOpenHelper.BARCODES_KEY_PAYSTATUS
				+ "=? GROUP BY "
				+ ScanwareLiteOpenHelper.BARCODES_KEY_PRODUCTID;
		String[] whereArgs = new String[] { "102" };
		Cursor cursor = db.rawQuery(query, whereArgs);
		while (cursor.moveToNext()) {
			int productID = cursor.getInt(0);
			int totalTickets = cursor.getInt(1);
			
			for (Product product : products) {
				if (product.getId() == productID) {
					product.setTotalTickets(totalTickets);
					break;
				}
			}
		}

	}

	private void getCheckedInTicketsForProducts(SQLiteDatabase db) {
		String query = "SELECT "
				+ ScanwareLiteOpenHelper.BARCODES_KEY_PRODUCTID
				+ ", COUNT(*) FROM " + ScanwareLiteOpenHelper.BARCODES_TABLE
				+ " WHERE " + ScanwareLiteOpenHelper.BARCODES_KEY_PAYSTATUS
				+ "=? AND " + ScanwareLiteOpenHelper.BARCODES_KEY_SEEN
				+ "=? GROUP BY "
				+ ScanwareLiteOpenHelper.BARCODES_KEY_PRODUCTID;
		String[] whereArgs = new String[] { "102" , "1"};
		Cursor cursor = db.rawQuery(query, whereArgs);
		while (cursor.moveToNext()) {
			int productID = cursor.getInt(0);
			int checkedInTickets = cursor.getInt(1);
			
			for (Product product : products) {
				if (product.getId() == productID) {
					product.setCheckedInTickets(checkedInTickets);
					break;
				}
			}
		}
	}

	private class StatisticsAdapter extends ArrayAdapter<Product> {

		public StatisticsAdapter(Context context, int resource,
				ArrayList<Product> products) {
			super(context, resource, products);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflater.inflate(R.layout.view_statistics_item,
						null);
			}
			Product product = products.get(position);

			if (product != null) {
				ProgressBar scannedTicketProgressBar = (ProgressBar) convertView
						.findViewById(R.id.progressBar_tickets_checked_in);
				TextView scannedTicketTextView = (TextView) convertView
						.findViewById(R.id.textView_tickets_checked_in);

				scannedTicketTextView.setText(String.format(
						getString(R.string.tv_tickets_checked_in),
						product.getTitle(),
						Integer.toString(product.getCheckedInTickets()),
						Integer.toString(product.getTotalTickets())));

				scannedTicketProgressBar.setMax(product.getTotalTickets());
				scannedTicketProgressBar.setProgress(product
						.getCheckedInTickets());
			}
			return convertView;
		}
	}
}
