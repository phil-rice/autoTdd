package org.autoTdd;

import java.util.List;

import org.autoTdd.engine.IEngine;
import org.autoTdd.engine.IEngineAsTree;

public interface ISystem {
	IEngine engine(String engineName);

	IEngineAsTree tree(String engineName);

	List<String> engineNames();

}
