package com.paylogic.scanwarelite;

import android.app.Application;

import com.paylogic.scanwarelite.models.User;

public class ScanwareLiteApplication extends Application {

	private boolean encrypted;
	private boolean running;

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

}
