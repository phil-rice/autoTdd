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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((colour == null) ? 0 : colour.hashCode());
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
		GarmentType other = (GarmentType) obj;
		if (colour == null) {
			if (other.colour != null)
				return false;
		} else if (!colour.equals(other.colour))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "GarmentType [name=" + name + ", colour=" + colour + "]";
	}
	
}
