package org.autoTdd.schengen;

import org.autoTdd.jbehave.because.IBecause2;

public abstract class AbstractSchengenBecause implements IBecause2<Schengen, Person>{

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;
		return obj.getClass()== getClass();
	}
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}

}
