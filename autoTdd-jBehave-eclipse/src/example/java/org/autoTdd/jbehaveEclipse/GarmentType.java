package org.autoTdd.jbehaveEclipse;

public class GarmentType {
	public final String name;
	public final String colour;

	public GarmentType(String nameAndColour){
		int index = nameAndColour.indexOf(' ');
		if (index == -1)
			throw new IllegalArgumentException(nameAndColour);
		name = LanguageHelper.singalize(nameAndColour.substring(index+1));
		colour = nameAndColour.substring(0, index);
	}
}
