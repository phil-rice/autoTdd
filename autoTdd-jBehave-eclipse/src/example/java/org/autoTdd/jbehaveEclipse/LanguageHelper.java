package org.autoTdd.jbehaveEclipse;

public class LanguageHelper {

	public static String singalize(String rawString) {
		if (rawString.endsWith("s"))
			return rawString.substring(0, rawString.length() - 1);
		else
			return rawString;
	}
}
