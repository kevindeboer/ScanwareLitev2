package com.paylogic.scanwarelite.models;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Event {

	private int id;
	private String name;
	private Date endDate;
	private Date deadline;
	


	public Event(int id, String name, String endDate, String deadline) {
		this.id = id;
		this.name = name;
		if (endDate != null) {
			this.endDate = setEndDate(endDate);
		}
		if (deadline != null) {
			this.deadline = setDeadline(deadline);
		}
	}

	private Date setDeadline(String deadline) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return df.parse(deadline);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Date setEndDate(String endDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return df.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Date getDeadline() {
		return deadline;
	}

	public Date getEndDate() {
		return endDate;
	}
	
}
