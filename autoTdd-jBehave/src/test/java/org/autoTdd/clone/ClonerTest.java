package org.autoTdd.clone;

import junit.framework.TestCase;

import org.softwarefm.utilities.clone.ICloneObject;
import org.softwarefm.utilities.clone.ICloner;

public class ClonerTest extends TestCase {

	private ICloner cloner;

	public void testGetNewClonerEachTime() {
		assertNotSame(ICloner.Utils.cloner(), ICloner.Utils.cloner());
	}

	// actually just checks that it does something
	public void testCopy() {
		cloner.register(Object.class, ICloner.Utils.copy());
		checkCloneCopies(1);
		checkCloneCopies("fred");
	}

	public void testUsesOrder1() {
		cloner.register(String.class, new ICloneObject<String>() {
			@Override
			public String makeCopyof(String t) {
				return new String(t);
			}
		});
		cloner.register(Object.class, ICloner.Utils.copy());
		checkCloneCopies(1);
		checkCloneGivesEqualsButNotSame("fred");
		checkCloneCopies(null);
	}

	public void testUsesOrder2() {
		cloner.register(Object.class, ICloner.Utils.copy());
		cloner.register(String.class, new ICloneObject<String>() {
			@Override
			public String makeCopyof(String t) {
				return new String(t);
			}
		});
		checkCloneCopies(1);
		checkCloneCopies("fred");
		checkCloneCopies(null);
	}

	public void testCopyConstructor() {
		cloner.register(Object.class, ICloner.Utils.copyConstructor());
		checkCloneGivesEqualsButNotSame("fred");
		checkCloneCopies(null);
	}

	private void checkCloneCopies(Object original) {
		Object right = cloner.makeCopyof(original);
		assertSame(original, right);
	}

	private void checkCloneGivesEqualsButNotSame(Object original) {
		Object right = cloner.makeCopyof(original);
		assertNotSame(original, right);
		assertEquals(original, right);

	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cloner = ICloner.Utils.cloner();
	}
}
