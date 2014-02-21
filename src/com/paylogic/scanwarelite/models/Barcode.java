package com.paylogic.scanwarelite.models;

import java.util.Date;

public class Barcode {

	private String barcode;
	private boolean found;
	private boolean validPayment;
	private boolean seen;
	private Date seenDate;
	private boolean allowed;
	private String productName;
	private String name;

	public Barcode(String barcode, boolean found) {
		this.barcode = barcode;
		this.found = found;
	}

	public Barcode(String barcode, boolean found, boolean validPayment, boolean seen,
			Date seenDate, boolean allowed, String productName, String name) {
		this.found = found;
		this.barcode = barcode;
		this.validPayment = validPayment;
		this.seen = seen;
		this.seenDate = seenDate;
		this.allowed = allowed;
		this.productName = productName;
		this.name = name;
	}

	public String getBarcode() {
		return barcode;
	}

	public boolean isFound() {
		return found;
	}

	public boolean isPaymentValid() {
		return validPayment;
	}

	public boolean isSeen() {
		return seen;
	}

	public Date getSeenDate() {
		return seenDate;
	}

	public boolean isAllowed() {
		return allowed;
	}

	public String getProductName() {
		return productName;
	}

	public String getName() {
		return name;
	}

}