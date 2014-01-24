package com.paylogic.scanwarelite.helpers;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Context;

public class OfflineLoginHelper {

	private final String userFile = "user";

	private Context context;

	public OfflineLoginHelper(Context context) {
		this.context = context;
	}

	public boolean userFileExists() {
		return context.getFileStreamPath(userFile).exists();
	}

	public FileOutputStream openUserFileOutput() throws FileNotFoundException {
		return context.openFileOutput(userFile, Context.MODE_PRIVATE);

	}

	public FileInputStream openUserFileInput() throws FileNotFoundException {
		return context.openFileInput(userFile);
	}

	public String getUserFileContent() {
		String line = "";
		String fileContent = "";

		FileInputStream inputStream;
		try {
			inputStream = openUserFileInput();
			InputStreamReader inputStreamReader = new InputStreamReader(
					inputStream);
			BufferedReader bufferedReader = new BufferedReader(
					inputStreamReader);
			while ((line = bufferedReader.readLine()) != null) {
				fileContent += line;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fileContent;
	}
	
	public void deleteUserFile(){
		context.deleteFile(userFile);
	}
}
