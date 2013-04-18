package org.autoTdd.jbehave;

import org.autoTdd.IMutableSystemBuilder;
import org.autoTdd.jbehave.junit.AutoTddJUnitStory;
import org.softwarefm.utilities.clone.ICloner;

public interface ISpecifiesEngines {

	void specify(IMutableSystemBuilder systemBuilder);

	IMutableSystemBuilder systemBuilder();

	ICloner cloner();

	public static class Utils {

		public static void specify(Object mightBeSpecifiesEngine, IMutableSystemBuilder systemBuilder) {
			if (mightBeSpecifiesEngine instanceof ISpecifiesEngines) {
				((ISpecifiesEngines) mightBeSpecifiesEngine).specify(systemBuilder);
			}
			if (mightBeSpecifiesEngine instanceof AutoTddJUnitStory)
				((AutoTddJUnitStory) mightBeSpecifiesEngine).specify();

		}

	}
}
