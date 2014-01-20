package com.paylogic.scanwarelite.models;

public class Product {

	private int totalTickets;
	private int checkedInTickets;
	private int id;
	private String title;
	private boolean allowed;

	public Product(int id, String title, boolean allowed) {
		this.id = id;
		this.title = title;
		this.allowed = allowed;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public void setAllowed(boolean allowed) {
		this.allowed = allowed;
	}

	public void setTotalTickets(int totalTickets) {
		this.totalTickets = totalTickets;
	}

	public int getTotalTickets() {
		return this.totalTickets;
	}

	public void setCheckedInTickets(int totalTickets) {
		this.checkedInTickets = totalTickets;
	}

	public int getCheckedInTickets() {
		return this.checkedInTickets;
	}
}
