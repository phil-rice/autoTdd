package org.autoTdd.builder.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.autoTdd.IAutoTddFactory;
import org.autoTdd.IEngineSpecification;
import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.ISystem;
import org.autoTdd.builder.IEngineBuilder;
import org.autoTdd.engine.IEngine;
import org.autoTdd.engine.IEngineAsTree;
import org.softwarefm.utilities.maps.Maps;

public class SystemBuilder implements IMutableSystemBuilder {
	private IAutoTddFactory factory;
	private final Map<String, IEngineSpecification> specMap = Maps.newMap();
	private final Map<String, IEngineBuilder> map = Maps.newMap();

	public SystemBuilder(IAutoTddFactory factory) {
		this.factory = factory;
	}
	
	@Override
	public ISystem build() {
		Map<String,IEngineAsTree> result = new HashMap<String, IEngineAsTree>();
		for (Entry<String, IEngineBuilder> entry: map.entrySet()){
			String engineName = entry.getKey();
			IEngineBuilder builder = entry.getValue();
			IEngineAsTree tree = builder.tree();
			result.put(engineName, tree);
		}
		return new System(result);
	}

	@Override
	public void specify(String engineName, IEngineSpecification specification) {
		factory.builderFor(engineName, specification);
		Maps.putNoDuplicates(specMap, engineName, specification);

	}

	@Override
	public IEngineAsTree tree(final String engineName) {
		preBuilderFor();
		IEngineBuilder builder = currentBuilderFor(engineName);
		return builder.tree();

	}

	@Override
	public IEngine engine(String engineName) {
		return tree(engineName);
	}

	private IEngineBuilder currentBuilderFor(final String engineName) {
		final IEngineSpecification engineSpecification = Maps.getOrException(specMap, engineName);
		IEngineBuilder builder = Maps.findOrCreate(map, engineName, new Callable<IEngineBuilder>() {
			@Override
			public IEngineBuilder call() throws Exception {
				return factory.builderFor(engineName, engineSpecification);
			}
		});
		return builder;
	}

	protected void preBuilderFor() {
	}

	@Override
	public void addConstraint(String systemName, Object result, Object because, Object... inputs) {
		IEngineBuilder builder = currentBuilderFor(systemName);
		IEngineBuilder newBuilder = builder.addConstraint(result, because, inputs);
		map.put(systemName, newBuilder);
	}

	@Override
	public List<String> engineNames() {
		return new ArrayList<String>(specMap.keySet());
	}

}
