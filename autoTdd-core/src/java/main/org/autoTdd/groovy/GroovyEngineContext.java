package org.autoTdd.groovy;

import groovy.lang.GroovyShell;
import groovy.lang.Script;

import java.util.Map;

import org.autoTdd.internal.Constraint;
import org.softwarefm.utilities.maps.Maps;

public class GroovyEngineContext {
	final GroovyShell shell = new GroovyShell();
	final Map<Constraint, Script> scriptCache = Maps.newMap();
}