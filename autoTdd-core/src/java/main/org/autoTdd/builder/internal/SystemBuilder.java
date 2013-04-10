package org.autoTdd.builder.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.autoTdd.IAutoTddFactory;
import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.ISystemSpecification;
import org.autoTdd.builder.IEngineBuilder;
import org.softwarefm.utilities.maps.Maps;

public class SystemBuilder implements IMutableSystemBuilder {
private IAutoTddFactory factory;
	private final Map<String, ISystemSpecification> specMap = Maps.newMap();
	private final Map<String, IEngineBuilder> map = Maps.newMap();

	public SystemBuilder(IAutoTddFactory factory) {
		this.factory = factory;
	}

	@Override
	public void specify(String systemName, ISystemSpecification specification) {
		Maps.putNoDuplicates(specMap, systemName, specification);

	}

	@SuppressWarnings("unchecked")
	@Override
	public IEngineBuilder builderFor(final String systemName) {
		preBuilderFor();
		final ISystemSpecification systemSpecification = Maps.getOrException(specMap, systemName);
		return (IEngineBuilder) Maps.findOrCreate(map, systemName, new Callable<IEngineBuilder>() {
			@Override
			public IEngineBuilder call() throws Exception {
				return factory.builderFor(systemSpecification, systemName);
			}
		});
		
	}

	protected void preBuilderFor() {
	}

	@Override
	public  void addConstraint(String systemName, Object result, Object because, Object... inputs) {
		IEngineBuilder builder = builderFor(systemName);
		IEngineBuilder newBuilder = builder.addConstraint(result, because, inputs);
		map.put(systemName, newBuilder);
	}

	@Override
	public List<String> systemNames() {
		return new ArrayList<String>(specMap.keySet());
	}

}
