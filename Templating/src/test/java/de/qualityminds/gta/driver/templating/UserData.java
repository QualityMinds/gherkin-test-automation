package de.qualityminds.gta.driver.templating;

// Test-Class for TemplateNullHelperTest

class UserData {
	private String firstName;
	private String lastName;
	private String email;
	private Double balance;

	public UserData(String firstName, String lastName, String email, double balance) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.balance = balance;
	}

	public UserData() {
	}

	public String getFirstName() {
		return this.firstName;
	}

	public String getLastName() {
		return this.lastName;
	}

	public String getEmail() {
		return this.email;
	}

	public double getBalance() {
		return this.balance;
	}
}