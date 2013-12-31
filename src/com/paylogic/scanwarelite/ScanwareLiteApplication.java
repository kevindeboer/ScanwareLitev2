package com.paylogic.scanwarelite;

import android.app.Application;

public class ScanwareLiteApplication extends Application {

	private int userId;
	private String username;
	private String password;
	private boolean encrypted;
	private boolean running;
	public int getUserId() {
		return userId;
	}

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

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
