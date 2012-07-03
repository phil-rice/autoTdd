/* This file is part of AutoTDD

/* AutoTDD is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.*/
/* AutoTDD is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
/* You should have received a copy of the GNU General Public License along with AutoTDD. If not, see <http://www.gnu.org/licenses/> */package org.autoTdd.groovy;

import java.util.Date;

import org.autoTdd.AutoTdd;
import org.autoTdd.Constraint;
import org.autoTdd.Entrant;
import org.autoTdd.NsisEntry;
import org.autoTdd.SchengenResult;
import org.autoTdd.Visa;

@SuppressWarnings("unchecked")
public class ExampleG extends AutoTdd<Entrant, SchengenResult> {
	@SuppressWarnings("deprecation")
	private static Visa normal = new Visa("UK", new Date(2004, 12, 1), new Date(2014, 12, 1));

	public ExampleG(SchengenResult defaultValue) {
		super(defaultValue);
	}

	@Override
	protected void addDefaultConstraints() {
		super.add(//
				new Constraint<Entrant, SchengenResult>(//
						new Entrant("Deborah", 1000, normal, null), //
						SchengenResult.accept,//
						"true"),//
				new Constraint<Entrant, SchengenResult>(//
						new Entrant("Francisco", 10000, normal, null), //
						SchengenResult.detain,//
						"input.cash >= 10000"), //
				new Constraint<Entrant, SchengenResult>(//
						new Entrant("Gary", 10000, normal, new NsisEntry("Crook")), //
						SchengenResult.arrest,//
						"input.nsisEntry != null"),//
				new Constraint<Entrant, SchengenResult>(//
						new Entrant("Bob", 1000, normal, new NsisEntry("Crook")), //
						SchengenResult.arrest,//
						"input.nsisEntry != null"));
	}

	public static void main(String[] args) {
		// now how does this tie into JUnit. Answer it should have a suite() method.
		// how do I use it... its just a class with a method
		// how do I see what it generated: answer the toString method
		// Debugging...can see trace.
		ExampleG engine = new ExampleG(SchengenResult.accept);
		System.out.println(engine);
		engine.trace(new Entrant("Deborah", 1000, normal, null));
		// eta: a few days
		// eat your own dog food...

	}

	private void trace(Entrant entrant) {
		// TODO Auto-generated method stub

	}
}
