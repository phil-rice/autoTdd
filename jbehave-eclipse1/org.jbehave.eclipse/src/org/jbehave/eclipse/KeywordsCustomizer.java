package org.jbehave.eclipse;

import org.autoTdd.steps.AutoTddStepTypes;
import org.jbehave.core.configuration.Keywords;

public class KeywordsCustomizer {

	public static <K extends Keywords> K customize(K keywords){
		keywords.addStartingWord(AutoTddStepTypes.BECAUSE, "Because");
		keywords.addStartingWord(AutoTddStepTypes.CALLED, "Called");
		return keywords;

	}
}
