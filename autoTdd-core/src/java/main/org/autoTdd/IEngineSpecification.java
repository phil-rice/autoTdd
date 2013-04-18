package org.autoTdd;

public interface IEngineSpecification extends ITyped{

	IEngineStrategy engineStrategy();

	Object defaultOutput();
	
}
