/* This file is part of AutoTDD

/* AutoTDD is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.*/
/* AutoTDD is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
/* You should have received a copy of the GNU General Public License along with AutoTDD. If not, see <http://www.gnu.org/licenses/> */package org.autoTdd;

import java.util.Date;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class SchengenG {

	@SuppressWarnings("deprecation")
	private static Visa normal = new Visa("UK", new Date(2004, 12, 1), new Date(2014, 12, 1));

	static Constraint deborah = new Constraint(//
			new Entrant("Deborah", 1000, normal, null), //
			SchengenResult.accept,//
			"true");

	static Constraint francisco = new Constraint(//
			new Entrant("Francisco", 10000, normal, null), //
			SchengenResult.detain,//
			"input.cash >= 10000");

	static Constraint gary = new Constraint(//
			new Entrant("Gary", 10000, normal, new NsisEntry("Dodgy")), //
			SchengenResult.arrest,//
			"input.nsisEntry != null");

	public static void main(String[] args) {
		AutoTdd engine1 = new AutoTdd(SchengenResult.accept);
		engine1.add(deborah, francisco, gary);
		System.out.println("deborah: " + engine1.apply(deborah.input));
		System.out.println("francisco: " + engine1.apply(francisco.input));
		System.out.println("gary: " + engine1.apply(gary.input));
		System.out.println(engine1);

		AutoTdd engine2 = new AutoTdd(SchengenResult.accept, gary, francisco, deborah);
		System.out.println("deborah: " + engine2.apply(deborah.input));
		System.out.println("francisco: " + engine2.apply(francisco.input));
		System.out.println("gary: " + engine2.apply(gary.input));
		System.out.println(engine2);
	}

}
