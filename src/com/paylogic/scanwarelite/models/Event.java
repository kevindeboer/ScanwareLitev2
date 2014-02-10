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
	private boolean localEvent;
	
	public Event(int id, String name, Date endDate, Date deadline) {
		this.id = id;
		this.name = name;
		this.endDate = endDate;
		this.deadline = deadline;
		this.localEvent = false;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public void setDeadline(Date deadline) {
		this.deadline = deadline;
	}
	
	public void setLocalEvent(boolean isLocalEvent) {
		this.localEvent = isLocalEvent;
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
	
	public boolean isLocalEvent() {
		return localEvent;
	}


}
