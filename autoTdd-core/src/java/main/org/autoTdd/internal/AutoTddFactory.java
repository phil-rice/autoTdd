package org.autoTdd.internal;

import java.util.Map;
import java.util.concurrent.Callable;

import org.autoTdd.IAutoTddFactory;
import org.autoTdd.IEngineSpecification;
import org.autoTdd.builder.IEngineBuilder;
import org.autoTdd.builder.internal.EngineBuilder;
import org.softwarefm.utilities.maps.Maps;

public class AutoTddFactory implements IAutoTddFactory {

	private final Map<String, IEngineSpecification> strategyMap = Maps.newMap();
	private final Map<String, IEngineBuilder> builderMap = Maps.newMap();

	@Override
	public IEngineBuilder builderFor(String engineName, final IEngineSpecification specification) {
		IEngineSpecification existing = strategyMap.get(engineName);
		if (existing == null)
			strategyMap.put(engineName, specification);
		else if (existing != specification)
			throw new IllegalArgumentException();
		return Maps.findOrCreate(builderMap, engineName, new Callable<IEngineBuilder>() {
			@Override
			public IEngineBuilder call() throws Exception {
				return new EngineBuilder(specification);
			}
		});
	}
}
