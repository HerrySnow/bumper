package com.chsoft.jdk8.driver;

import java.util.Date;

public class Points {
	private Date time;
	private double longitude;
	private double latitude;
	
	public Points(Date time, double longitude, double latitude) {
		super();
		this.time = time;
		this.longitude = longitude;
		this.latitude = latitude;
	}
	
	
	public Date getTime() {
		return time;
	}


	public void setTime(Date time) {
		this.time = time;
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
