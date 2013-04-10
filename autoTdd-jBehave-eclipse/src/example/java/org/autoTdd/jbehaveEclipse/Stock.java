package org.autoTdd.jbehaveEclipse;

import java.util.HashMap;
import java.util.Map;

import org.softwarefm.utilities.maps.Maps;

public class Stock {
	private Map<GarmentType, Integer> map = new HashMap<GarmentType, Integer>();

	public void clear() {
		map.clear();
	}

	public void addToStockLevel(int count, GarmentType item) {
		Maps.add(map, item, count);
	}

	public void setStockForTest(int count, String item) {
		map.put(new GarmentType(item), count);
	}

	public int getStockLevel(GarmentType garmentType) {
		return Maps.intFor(map, garmentType);
	}

}
