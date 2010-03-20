package org.seamoo.gaeForTest;

import java.lang.reflect.Constructor;

public final class ConstructorUtils {
	public static final <T> Constructor<T> findConstructor(Class<T> clz,
			int parameterLength) {

		Constructor[] ctors = clz.getDeclaredConstructors();
		for (int i = 0; i < ctors.length; i++) {
			if (ctors[i].getParameterTypes().length == parameterLength) {
				ctors[i].setAccessible(true);
				return ctors[i];
			}
		}
		return null;
	}
}
