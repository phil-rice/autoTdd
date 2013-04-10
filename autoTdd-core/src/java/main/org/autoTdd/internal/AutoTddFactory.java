package org.autoTdd.internal;

import java.util.Map;
import java.util.concurrent.Callable;

import org.autoTdd.IAutoTddFactory;
import org.autoTdd.ISystemSpecification;
import org.autoTdd.builder.IEngineBuilder;
import org.autoTdd.builder.internal.EngineBuilder;
import org.softwarefm.utilities.maps.Maps;

public class AutoTddFactory implements IAutoTddFactory {

	private final Map<String, ISystemSpecification> strategyMap = Maps.newMap();
	private final Map<String, IEngineBuilder> builderMap = Maps.newMap();

	@Override
	public IEngineBuilder builderFor(final ISystemSpecification specification, String systemName) {
		ISystemSpecification existing = strategyMap.get(systemName);
		if (existing == null)
			strategyMap.put(systemName, specification);
		else if (existing != specification)
			throw new IllegalArgumentException();
		return (IEngineBuilder) Maps.findOrCreate(builderMap, systemName, new Callable<IEngineBuilder>() {
			@Override
			public IEngineBuilder call() throws Exception {
				return new EngineBuilder(specification);
			}
		});
	}
}
