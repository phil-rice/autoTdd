package org.autoTdd.schengen;

public class Person {

	public int money;

	String name;

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
		return person;
	}
	@Override
	public String toString() {
		return "Person [name=" + name + ", money=" + money + "]";
	}

}
