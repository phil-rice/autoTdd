package org.autoTdd.engine;

import java.util.List;

import org.autoTdd.builder.internal.Node;

public interface IEngineAsTree extends IEngine , IRooted{

	List<Node> allNodes();
	
	Object defaultOutput();

}
