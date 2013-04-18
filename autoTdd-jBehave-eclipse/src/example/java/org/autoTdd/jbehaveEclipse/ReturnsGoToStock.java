package org.autoTdd.jbehaveEclipse;

import org.autoTdd.jbehave.DSL;
import org.autoTdd.jbehave.junit.AutoTddJUnitReportingRunner;
import org.autoTdd.jbehave.junit.AutoTddJUnitStory;
import org.autoTdd.jbehaveEclipse.steps.StockSteps;
import org.junit.runner.RunWith;
import org.softwarefm.schengen.IEngineSteps;
import org.softwarefm.utilities.runnable.Runnables;

@RunWith(AutoTddJUnitReportingRunner.class)
public class ReturnsGoToStock extends AutoTddJUnitStory {
	@Override
	public void specify() {
		systemBuilder().specify("he returns the $item for a refund", DSL.specification(Runnable.class, Runnables.noRunnable, Stock.class));
		systemBuilder().specify("he returns the $dummyItem for a replacement in $newColor", DSL.specification(Runnable.class, Runnables.noRunnable, Stock.class));
	}

	@Override
	protected IEngineSteps[] engineSteps() {
		return new IEngineSteps[] { new StockSteps() };
	}

}
