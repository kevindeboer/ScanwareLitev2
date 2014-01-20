package com.paylogic.scanwarelite.activities;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.TextView;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.products.GetProductsDialog;
import com.paylogic.scanwarelite.helpers.ScanwareLiteOpenHelper;
import com.paylogic.scanwarelite.models.Product;

public class ProductsActivity extends CommonActivity {
	private ListView productsView;
	private Button continueButton;
	
	private ArrayList<Product> products;
	private ProductsAdapter products_adapter;

	private TextView eventNameView;
	private GetProductsTask getProductsTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_products);

		products = new ArrayList<Product>();

		productsView = (ListView) findViewById(R.id.listView_products);
		continueButton = (Button) findViewById(R.id.button_products_continue);
		eventNameView = (TextView) findViewById(R.id.textView_event_name);
		scanwareLiteOpenHelper = new ScanwareLiteOpenHelper(
				ProductsActivity.this, ScanwareLiteOpenHelper.DATABASE_NAME,
				null, ScanwareLiteOpenHelper.DATABASE_VERSION);



		products_adapter = new ProductsAdapter(ProductsActivity.this,
				android.R.layout.simple_list_item_1, products);
		productsView.setAdapter(products_adapter);

		getProductsTask = new GetProductsTask();
		getProductsTask.execute();
	}
	
	protected void onResume(){
		super.onResume();
		productsView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				products_adapter.toggleChecked(position);
			}
		});
		continueButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(ProductsActivity.this,
						ScanActivity.class);
				startActivity(intent);
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_products, menu);
		super.onCreateOptionsMenu(menu);
		return true;
	}


	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menu_settings:
			intent = new Intent(ProductsActivity.this, SettingsActivity.class);
			startActivity(intent);
			break;
		}

		return false;
	}

	public void deselectAll(View v) {
		products_adapter.uncheckAll();
	}

	public void selectAll(View v) {
		products_adapter.checkAll();
	}

	private class ProductsAdapter extends ArrayAdapter<Product> {

		public ProductsAdapter(Context context, int resource,
				ArrayList<Product> products) {
			super(context, resource, products);

		}

		public void toggleChecked(int position) {
			Product product = products.get(position);
			ContentValues updatedValues = new ContentValues();

			db = scanwareLiteOpenHelper.getWritableDatabase();

			if (product.isAllowed()) {
				product.setAllowed(false);

				updatedValues.put(ScanwareLiteOpenHelper.PRODUCTS_KEY_ALLOWED,
						product.isAllowed());

				String whereClause = ScanwareLiteOpenHelper.PRODUCTS_KEY_ID
						+ "= ?";
				String[] whereArgs = new String[] { Integer
						.toString(product.getId()) };

				db.update(ScanwareLiteOpenHelper.PRODUCTS_TABLE, updatedValues,
						whereClause, whereArgs);
			} else {
				product.setAllowed(true);

				updatedValues.put(ScanwareLiteOpenHelper.PRODUCTS_KEY_ALLOWED,
						product.isAllowed());

				String whereClause = ScanwareLiteOpenHelper.PRODUCTS_KEY_ID
						+ "= ?";
				String[] whereArgs = new String[] { Integer
						.toString(product.getId()) };

				db.update(ScanwareLiteOpenHelper.PRODUCTS_TABLE, updatedValues,
						whereClause, whereArgs);
			}
			notifyDataSetChanged();
		}

		public void checkAll() {
			for (Product p : products) {
				if (!p.isAllowed()) {
					p.setAllowed(true);
				}
			}
			
			ContentValues updatedValues = new ContentValues();
			updatedValues
					.put(ScanwareLiteOpenHelper.PRODUCTS_KEY_ALLOWED, true);

			db.update(ScanwareLiteOpenHelper.PRODUCTS_TABLE, updatedValues,
					null, null);

			notifyDataSetChanged();
		}

		public void uncheckAll() {
			for (Product p : products) {
				if (p.isAllowed()) {
					p.setAllowed(false);
				}
			}
			
			ContentValues updatedValues = new ContentValues();
			updatedValues.put(ScanwareLiteOpenHelper.PRODUCTS_KEY_ALLOWED,
					false);

			db.update(ScanwareLiteOpenHelper.PRODUCTS_TABLE, updatedValues,
					null, null);

			notifyDataSetChanged();

		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View v = convertView;

			if (v == null) {
				LayoutInflater inflater = (LayoutInflater) getContext()
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.view_products_item, null);
			}
			Product product = products.get(position);

			if (product != null) {
				CheckedTextView productNameView = (CheckedTextView) v
						.findViewById(R.id.textView_productName);

				productNameView.setText(product.getTitle());

				Boolean checked = products.get(position).isAllowed();
				productNameView.setChecked(checked);

			}
			return v;
		}
	}

	private class GetProductsTask extends AsyncTask<Void, Product, Void> {
		String eventName;
		@Override
		protected void onPreExecute() {
			progressDialog = new GetProductsDialog(ProductsActivity.this);
			progressDialog.show();
			products.clear();
			products_adapter.notifyDataSetChanged();

			db = scanwareLiteOpenHelper.getReadableDatabase();
		}

		@Override
		protected void onPostExecute(Void result) {
			eventNameView.setText(String.format(
					getString(R.string.tv_event_name), eventName));
			progressDialog.dismiss();
		}

		@Override
		protected Void doInBackground(Void... params) {
			String[] columns = new String[] {
					ScanwareLiteOpenHelper.PRODUCTS_KEY_ID,
					ScanwareLiteOpenHelper.PRODUCTS_KEY_TITLE,
					ScanwareLiteOpenHelper.PRODUCTS_KEY_ALLOWED };
			Cursor cursor = db.query(ScanwareLiteOpenHelper.PRODUCTS_TABLE,
					columns, null, null, null, null, null);
			while (cursor.moveToNext()) {
				int id = cursor
						.getInt(cursor
								.getColumnIndex(ScanwareLiteOpenHelper.PRODUCTS_KEY_ID));
				String title = cursor
						.getString(cursor
								.getColumnIndex(ScanwareLiteOpenHelper.PRODUCTS_KEY_TITLE));
				boolean allowed = cursor
						.getInt(cursor
								.getColumnIndex(ScanwareLiteOpenHelper.PRODUCTS_KEY_ALLOWED)) > 0;
				Product product = new Product(id, title, allowed);
				publishProgress(product);
			}

			columns = new String[] { ScanwareLiteOpenHelper.EVENT_KEY_TITLE };
			cursor = db.query(ScanwareLiteOpenHelper.EVENTS_TABLE, columns,
					null, null, null, null, null, "1");
			cursor.moveToFirst();

			eventName = cursor.getString(cursor
					.getColumnIndex(ScanwareLiteOpenHelper.EVENT_KEY_TITLE));



			return null;
		}

		@Override
		protected void onProgressUpdate(Product... product) {
			super.onProgressUpdate(product);
			products.add(product[0]);
			products_adapter.notifyDataSetChanged();
		}
	}
}
