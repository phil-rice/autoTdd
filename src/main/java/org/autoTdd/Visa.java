package org.autoTdd;

import java.util.Date;

public class Visa {
	public String country;
	public Date validFrom;
	public Date validTo;

	public Visa(String country, Date validFrom, Date validTo) {
		this.country = country;
		this.validFrom = validFrom;
		this.validTo = validTo;
	}

	@Override
	public String toString() {
		return "Visa [country=" + country + ", validFrom=" + validFrom + ", validTo=" + validTo + "]";
	}
}