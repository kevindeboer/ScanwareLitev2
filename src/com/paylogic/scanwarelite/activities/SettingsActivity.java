package com.paylogic.scanwarelite.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.paylogic.scanwarelite.R;
import com.paylogic.scanwarelite.dialogs.settings.DeleteDataDialog;

public class SettingsActivity extends CommonActivity {
	private Button deleteAllDataButton;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		deleteAllDataButton = (Button) findViewById(R.id.button_delete_all_data);
	}

	protected void onResume() {
		super.onResume();
		deleteAllDataButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
		
				alertDialog = new DeleteDataDialog(SettingsActivity.this).create();
				alertDialog.show();
			}
		});
	}
	
	public void dismissDialog(){
		alertDialog.cancel();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		return true;
	}


	public boolean onOptionsItemSelected(MenuItem item) {
		 super.onOptionsItemSelected(item);
		 return false;
	}
}
