package com.mvcApp.test.mvcApp.rest;

import java.util.ArrayList;

public class Roommate implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String name;
	String email;
	int gender;
	int gender_o;
	int wake;
	int wake_o;
	int sleep;
	int sleep_o;
	int party;
	int party_o;
	int politics;
	int politics_o;
	int religion;
	int religion_o;
	
	static String[] genderMap = new String[] {"", "Male", "Female", "Other"};
	static String[] wakeMap = new String[] {"", "", "", "Before 4 AM", "4 AM", "5 AM", "6 AM", "7 AM", "8 AM", "9 AM", "10 AM", "After 10 AM"};
	static String[] sleepMap = new String[] {"", "1 AM", "2 AM", "After 2 AM", "", "", "", "Before 8 PM", "8 PM", "9 PM", "10 PM", "11 PM", "12 AM"};
	static String[] partyMap = new String[] {"", "1 (Introvert)", "2 (Not introverted, but not a big extrovert)", "3 (Extrovert)"};
	static String[] politicsMap = new String[] {"No Preference", "Democrat", "Moderate", "Republican", "Independent"};
	static String[] religionMap = new String[] {"No Preference", "Christian", "Islamic", "Hinduism", "Buddhism", "Athiest"};
	
	public Roommate(String name, String email, int gender, int gender_o, int wake, int wake_o, int sleep, int sleep_o,
			int party, int party_o, int politics, int politics_o, int religion, int religion_o) {
		super();
		this.name = name;
		this.email = email;
		this.gender = gender;
		this.gender_o = gender_o;
		this.wake = wake;
		this.wake_o = wake_o;
		this.sleep = sleep;
		this.sleep_o = sleep_o;
		this.party = party;
		this.party_o = party_o;
		this.politics = politics;
		this.politics_o = politics_o;
		this.religion = religion;
		this.religion_o = religion_o;
	}

	public int getGender() {
		return gender;
	}

	public int getGender_o() {
		return gender_o;
	}

	public String getName() {
		return name;
	}

	public String getEmail() {
		return email;
	}

	public int getWake() {
		return wake;
	}

	public int getWake_o() {
		return wake_o;
	}

	public int getSleep() {
		return sleep;
	}

	public int getSleep_o() {
		return sleep_o;
	}

	public int getParty() {
		return party;
	}

	public int getParty_o() {
		return party_o;
	}

	public int getPolitics() {
		return politics;
	}

	public int getPolitics_o() {
		return politics_o;
	}

	public int getReligion() {
		return religion;
	}

	public int getReligion_o() {
		return religion_o;
	}

	@Override
	public boolean equals(Object obj) {
		Roommate other = (Roommate) obj;
		if(other.name.contentEquals(name)) {
			if(other.email.contentEquals(email)) {
				return true;
			}
		}
		return false;
	}
	
	public int getDiff(Roommate r2) {
		if(wake_o != 0 && r2.wake < wake_o)
			return -1;
		else if (r2.wake_o != 0 && wake < r2.wake_o)
			return -2;
		
		if(sleep_o != 0 && r2.sleep > sleep_o)
			return -1;
		else if (r2.sleep_o != 0 && sleep > r2.sleep_o)
			return -2;
		
		if(party_o != 0 && r2.party != party_o)
			return -1;
		else if (r2.party_o != 0 && party != r2.party_o)
			return -2;
		
		if(politics_o != 0 && r2.politics != politics_o)
			return -1;
		else if (r2.politics_o != 0 && politics < r2.politics_o)
			return -2;
		
		if(religion_o != 0 && r2.religion < religion_o)
			return -1;
		else if (r2.religion_o != 0 && religion < r2.religion_o)
			return -2;
		
		return 0;
	}
	
}
