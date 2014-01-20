package com.paylogic.scanwarelite.models;

public class User {
	private int UserId;
	private String username;
	private String password;

	
	public User(int UserId, String username, String password){
		this.UserId = UserId;
		this.username = username;
		this.password = password;
	}
	
	public int getUserId() {
		return UserId;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
}
