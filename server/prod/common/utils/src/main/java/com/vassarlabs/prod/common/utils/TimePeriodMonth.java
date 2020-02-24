package com.vassarlabs.prod.common.utils;

public class TimePeriodMonth {

	private String nameOfMonthYear;
	private long monthStart;
	private long monthEnd;
	
	public String getNameOfMonthYear() {
		return nameOfMonthYear;
	}
	public void setNameOfMonthYear(String nameOfMonthYear) {
		this.nameOfMonthYear = nameOfMonthYear;
	}
	public long getMonthStart() {
		return monthStart;
	}
	public void setMonthStart(long monthStart) {
		this.monthStart = monthStart;
	}
	public long getMonthEnd() {
		return monthEnd;
	}
	public void setMonthEnd(long monthEnd) {
		this.monthEnd = monthEnd;
	}
	@Override
	public String toString() {
		return "TimePeriodMonth [nameOfMonthYear=" + nameOfMonthYear
				+ ", monthStart=" + monthStart + ", monthEnd=" + monthEnd + "]";
	}
}