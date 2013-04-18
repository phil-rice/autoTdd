package org.autoTdd.builder.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.autoTdd.ISystem;
import org.autoTdd.engine.IEngine;
import org.autoTdd.engine.IEngineAsTree;
import org.softwarefm.utilities.maps.Maps;

public class System implements ISystem {

	private Map<String, IEngineAsTree> map;
	private List<String> names;

	public System(Map<String, IEngineAsTree> map) {
		this.map = new HashMap<String, IEngineAsTree>(map);
		this.names = new ArrayList<String>(map.keySet());
	}

	@Override
	public IEngine engine(String engineName) {
		return Maps.getOrException(map, engineName);
	}

	@Override
	public IEngineAsTree tree(String engineName) {
		return Maps.getOrException(map, engineName);
	}

	@Override
	public List<String> engineNames() {
		return names;
	}

}
