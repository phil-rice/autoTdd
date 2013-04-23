package org.autoTdd.schengen;

public class Person {

	public int money;

	String name;

	public boolean carryingDrugs;

	public void setMoney(int money) {
		this.money = money;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		Person person = new Person();
		person.setMoney(money);
		person.setName(name);
		person.setCarryingDrugs(carryingDrugs);
		return person;
	}

	@Override
	public String toString() {
		return "Person [name=" + name + ", money=" + money + ", carryingDrugs="
				+ carryingDrugs + "]";
	}

	public void setCarryingDrugs(boolean carryingDrugs) {
		this.carryingDrugs = carryingDrugs;

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (carryingDrugs ? 1231 : 1237);
		result = prime * result + money;
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
		Person other = (Person) obj;
		if (carryingDrugs != other.carryingDrugs)
			return false;
		if (money != other.money)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

}
