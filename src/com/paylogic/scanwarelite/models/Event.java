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
	private boolean onlineEvent;

	public Event(int id, String name, String endDate, String deadline) {
		this.id = id;
		this.name = name;
		this.onlineEvent = true;
		if (endDate != null) {
			this.endDate = setEndDateByString(endDate);
		}
		if (deadline != null) {
			this.deadline = setDeadlineByString(deadline);
		}

	}

	private Date setDeadlineByString(String deadline) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return df.parse(deadline);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	private Date setEndDateByString(String endDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return df.parse(endDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void setEndDate(Date endDate){
		this.endDate = endDate;
	}
	
	public void setDeadline(Date deadline){
		this.deadline = deadline;
	}


	public boolean isOnlineEvent() {
		return onlineEvent;
	}
	
	public void setOnlineEvent(boolean onlineEvent) {
		this.onlineEvent = onlineEvent;
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

	public void setName(String name) {
		this.name = name;
	}

}
