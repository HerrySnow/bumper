package com.chsoft.guava;

import java.util.Date;

public class Test {
	private int hour;
	private String name;
	private Date dateTime;
	public Test(int hour,String name,Date dateTime){
		this.hour=hour;
		this.name=name;
		this.dateTime=dateTime;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dateTime == null) ? 0 : dateTime.hashCode());
		result = prime * result + hour;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Test other = (Test) obj;
		if (dateTime == null) {
			if (other.dateTime != null)
				return false;
		} else if (!dateTime.equals(other.dateTime))
			return false;
		if (hour != other.hour)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	public static void main(String[] args) {
		Test t1=new Test(1,"tom",new Date());
		Test t2=new Test(1,"tom1",new Date());
		System.out.println(t1.equals(t2));
	}
}
