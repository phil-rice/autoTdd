package org.autoTdd;

import java.util.Date;

import org.autoTdd.engine.CatagorisationConstraint;
import org.autoTdd.engine.Engine;

public class Schengen2 extends Engine<Entrant, SchengenResult, SchengenResult> {

	private static Visa normal = new Visa("UK", new Date(2004, 12, 1),
			new Date(2014, 12, 1));

	static abstract class SchengenConstraint extends
			CatagorisationConstraint<Entrant, SchengenResult> {

		public SchengenConstraint(Entrant input, SchengenResult result) {
			super(input, result);
		}
	}

	SchengenConstraint deborah = new SchengenConstraint(//
			new Entrant("Deborah", 1000, normal, null), //
			SchengenResult.accept) {
		@Override
		public SchengenResult match(Entrant input) {
			return SchengenResult.accept;
		}
	};

	SchengenConstraint francisco = new SchengenConstraint(//
			new Entrant("Francisco", 10000, normal, null), //
			SchengenResult.detain) {
		@Override
		public SchengenResult match(Entrant input) {
			if (input.cash >= 10000)
				return SchengenResult.detain;
			return null;
		}
	};

	SchengenConstraint gary = new SchengenConstraint(//
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
	public Schengen2() {
		super(SchengenResult.accept);
		add(deborah, francisco, gary);
	}

	public static void main(String[] args) {
		Schengen2 schengen = new Schengen2();
		System.out
				.println("deborah: " + schengen.apply(schengen.deborah.input));
		System.out.println("francisco: "
				+ schengen.apply(schengen.francisco.input));
		System.out.println("gary: " + schengen.apply(schengen.gary.input));
	}

}
