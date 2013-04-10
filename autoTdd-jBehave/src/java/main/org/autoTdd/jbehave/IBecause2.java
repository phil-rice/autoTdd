package org.autoTdd.jbehave;

public interface IBecause2<Server,Value> extends IBecause {

	boolean evaluate(Server server, Value value);
	
}
