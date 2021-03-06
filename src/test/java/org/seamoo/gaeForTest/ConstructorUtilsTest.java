package org.seamoo.gaeForTest;

import java.lang.reflect.Constructor;

import junit.framework.Assert;


import org.junit.Test;
import org.seamoo.gaeForTest.ConstructorUtils;

public class ConstructorUtilsTest {

	@Test
	public void zeroArgumentConstructorShouldInstantiateFieldToZero()
			throws Exception {
		Constructor<PrivateCtorClass> ctor = ConstructorUtils.findConstructor(PrivateCtorClass.class, 0);
		PrivateCtorClass wow = ctor.newInstance(new Object[] {});
		Assert.assertEquals("0", wow.getPublicField());
	}

	@Test
	public void oneArgumentConstructorShouldInstantiateFieldToOne()
			throws Exception {
		Constructor<PrivateCtorClass> ctor = ConstructorUtils.findConstructor(PrivateCtorClass.class, 1);
		PrivateCtorClass wow = ctor.newInstance(new Object[] { null });
		Assert.assertEquals("1", wow.getPublicField());
	}
}
