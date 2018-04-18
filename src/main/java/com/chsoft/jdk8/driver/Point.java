package com.chsoft.jdk8.driver;

public class Point {
	private String dateTime;
	private double longitude;
	private double latitude;
	
	public Point(String dateTime, double longitude, double latitude) {
		super();
		this.dateTime = dateTime;
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	public String getDateTime() {
		return dateTime;
	}
	public void setDateTime(String dateTime) {
		this.dateTime = dateTime;
	}
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
}
