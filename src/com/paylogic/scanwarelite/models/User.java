package com.paylogic.scanwarelite.models;

public class User {
	private int userId;
	private String username;
	private String password;

	private static User instance;

	public static User createInstance(int userId, String username,
			String password) {
		instance = new User(userId, username, password);
		return instance;
	}

	public static User getInstance() throws UserNotFoundException {
		if (instance == null)
			throw new UserNotFoundException("User was not created");
		return instance;
	}

	private User(int userId, String username, String password) {
		this.userId = userId;
		this.username = username;
		this.password = password;
	}

	public int getUserId() {
		return userId;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public static class UserNotFoundException extends Exception {
		public UserNotFoundException(String message) {
			super(message);
		}
	}
}
