package org.autoTdd;


public class Entrant {
	public String name;
	public int cash;
	public Visa visa;
	public final NsisEntry nsisEntry;

	public Entrant(String name, int cash, Visa visa, NsisEntry nsisEntry) {
		this.name = name;
		this.cash = cash;
		this.visa = visa;
		this.nsisEntry = nsisEntry;
	}

	@Override
	public String toString() {
		return "Entrant [name=" + name + ", cash=" + cash + ", nsisEntry=" + nsisEntry + "]";
	}
}