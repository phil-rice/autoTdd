package org.autoTdd;

import java.util.Date;

import org.autoTdd.engine.CatagorisationConstraint;
import org.autoTdd.engine.Engine;

public class Schengen {

	private static Visa normal = new Visa("UK", new Date(2004, 12, 1), new Date(2014, 12, 1));

	static abstract class SchengenConstraint extends CatagorisationConstraint<Entrant, SchengenResult>{

		public SchengenConstraint(Entrant input, SchengenResult result) {
			super(input, result);
		}
		
	}
	static SchengenConstraint deborah = new SchengenConstraint(//
			new Entrant("Deborah", 1000, normal, null), //
			SchengenResult.accept) {
		@Override
		public SchengenResult match(Entrant input) {
			return SchengenResult.accept;
		}
	};

	static SchengenConstraint francisco = new SchengenConstraint(//
			new Entrant("Francisco", 10000, normal, null), //
			SchengenResult.detain) {
		@Override
		public SchengenResult match(Entrant input) {
			if (input.cash >= 10000)
				return SchengenResult.detain;
			return null;
		}
	};

	static SchengenConstraint gary = new SchengenConstraint(//
			new Entrant("Gary", 10000, normal, new NsisEntry("crook")), //
			SchengenResult.arrest) {
		@Override
		public SchengenResult match(Entrant input) {
			if (input.nsisEntry != null)
				return SchengenResult.arrest;
			return null;
		}
	};

	@SuppressWarnings({ "unchecked" })
	public static void main(String[] args) {
		Engine<Entrant, SchengenResult, SchengenResult> engine = new Engine<Entrant, SchengenResult, SchengenResult>(SchengenResult.accept);
		engine.add(deborah, francisco, gary);
		System.out.println("deborah: " + engine.apply(deborah.input));
		System.out.println("francisco: " + engine.apply(francisco.input));
		System.out.println("gary: " + engine.apply(gary.input));
	}

}
