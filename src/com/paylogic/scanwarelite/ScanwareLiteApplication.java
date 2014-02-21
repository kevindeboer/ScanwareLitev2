package com.paylogic.scanwarelite;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import android.app.Application;

public class ScanwareLiteApplication extends Application {
	private ObjectGraph applicationGraph;
	private boolean encrypted;
	private boolean running;

	@Override
	public void onCreate() {
		super.onCreate();

		applicationGraph = ObjectGraph.create(getModules().toArray());
	}

	public List<Object> getModules() {
		return Arrays.<Object> asList(new ScanwareLiteModule(this));
	}

	public ObjectGraph getObjectGraph() {
		return applicationGraph;
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
}
