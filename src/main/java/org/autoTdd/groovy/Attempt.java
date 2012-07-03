/* This file is part of AutoTDD

/* AutoTDD is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.*/
/* AutoTDD is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. */
/* You should have received a copy of the GNU General Public License along with AutoTDD. If not, see <http://www.gnu.org/licenses/> */package org.autoTdd.groovy;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

public class Attempt {
	public static void main(String[] args) {
		Binding binding = new Binding();
		binding.setVariable("foo", new Integer(2));
		GroovyShell shell = new GroovyShell(binding);

		Object value = shell.evaluate("println 'Hello World!'; x = 123; return foo * 10");
		System.out.println(value);
		assert value.equals(new Integer(20));
		assert binding.getVariable("x").equals(new Integer(123));
	}
}
