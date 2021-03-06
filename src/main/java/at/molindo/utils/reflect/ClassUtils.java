/**
 * Copyright 2016 Molindo GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package at.molindo.utils.reflect;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import at.molindo.thirdparty.org.springframework.core.GenericTypeResolver;
import at.molindo.utils.collections.ArrayUtils;
import at.molindo.utils.collections.IteratorUtils;

public class ClassUtils {
	private ClassUtils() {
	}

	/**
	 * @param type
	 * @param genericCls
	 * @return first actual type arguments of gernicCls in type hierarchy of cls
	 */
	public static Class<?> getTypeArgument(Class<?> cls, Class<?> genericCls) {
		return ArrayUtils.first(getTypeArguments(cls, genericCls));
	}

	/**
	 * @param type
	 * @param genericCls
	 * @return actual type arguments of gernicCls in type hierarchy of cls
	 */
	public static Class<?>[] getTypeArguments(Class<?> cls, Class<?> genericCls) {
		return GenericTypeResolver.resolveTypeArguments(cls, genericCls);
	}

	public static Class<?> toClass(Class<?> declaringCls, Type type) {
		return GenericTypeResolver.extractClass(declaringCls, type);
	}

	/**
	 * @return <code>true</code> if <code>cls</code> is assignable to at least
	 *         one class in <code>classes</code>
	 */
	public static boolean isAssignable(Class<?> cls, Set<Class<?>> classes) {
		if (cls == null || classes.isEmpty()) {
			return false;
		}
		for (Class<?> c : classes) {
			if (c.isAssignableFrom(cls)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return <code>true</code> if <code>cls</code> is assignable to all
	 *         <code>classes</code>
	 */
	public static boolean isAssignableToAll(Class<?> cls, Set<Class<?>> classes) {
		if (cls == null) {
			return false;
		}
		for (Class<?> c : classes) {
			if (!c.isAssignableFrom(cls)) {
				return false;
			}
		}
		return true;
	}

	public static Class<?> forName(String classname) throws ClassNotFoundException {
		return forName(classname, false, null, null);
	}

	public static Class<?> forName(String classname, boolean init) throws ClassNotFoundException {
		return forName(classname, init, null, null);
	}

	public static Class<?> forName(String classname, boolean init, Thread thread) throws ClassNotFoundException {
		return forName(classname, init, thread, null);
	}

	public static Class<?> forName(String classname, boolean init, Class<?> fallback) throws ClassNotFoundException {
		return forName(classname, init, null, fallback);
	}

	/**
	 * @param
	 * @param classname
	 *            fully qualified name of the desired class
	 * @param init
	 *            whether the class must be initialized
	 * @param thread
	 *            thread to use for context classloader or <code>null</code> for
	 *            current thread
	 * @param fallback
	 *            {@link ClassLoader} providing class if no context classloader
	 *            or <code>null</code> for this class
	 * @return class object representing the desired class
	 * @throws ClassNotFoundException
	 *             if the class cannot be located by the specified class loader
	 * 
	 * @see Thread#currentThread()
	 * @see Thread#getContextClassLoader()
	 * @see Class#getClassLoader()
	 * @see Class#forName(String, boolean, ClassLoader)
	 */
	public static Class<?> forName(String classname, boolean init, Thread thread, Class<?> fallback)
			throws ClassNotFoundException {

		return Class.forName(classname, init, getClassLoader(thread, fallback));
	}

	public static ClassLoader getClassLoader() {
		return getClassLoader(null, null);
	}

	public static ClassLoader getClassLoader(Class<?> fallback) {
		return getClassLoader(null, fallback);
	}

	public static ClassLoader getClassLoader(Thread thread) {
		return getClassLoader(thread, null);
	}

	/**
	 * @param thread
	 *            {@link Thread} to use for
	 *            {@link Thread#getContextClassLoader() context ClassLoader} or
	 *            <code>null</code> for {@link Thread#currentThread() current
	 *            thread}
	 * @param fallback
	 *            {@link ClassLoader} providing class if no context classloader
	 *            or <code>null</code> for this class
	 * @return never <code>null</code>
	 */
	public static ClassLoader getClassLoader(Thread thread, Class<?> fallback) {
		if (thread == null) {
			thread = Thread.currentThread();
		}
		ClassLoader loader = thread.getContextClassLoader();

		if (loader == null) {
			loader = (fallback != null ? fallback : ClassUtils.class).getClassLoader();
		}

		if (loader == null) {
			ClassLoader.getSystemClassLoader();
		}

		return loader;
	}

	/**
	 * @see #getPackageResourcePath(Class, String)
	 * @see ClassLoader#getResource(String)
	 */
	public static URL getClasspathResource(Class<?> scope, String resource) {
		return getClassLoader(scope).getResource(getPackageResourcePath(scope, resource));
	}

	/**
	 * @see #getPackageResourcePath(Class, String)
	 * @see ClassLoader#getResourceAsStream(String)
	 */
	public static InputStream getClasspathResourceAsStream(Class<?> scope, String resource) {
		return getClassLoader(scope).getResourceAsStream(getPackageResourcePath(scope, resource));
	}

	/**
	 * @see #getPackageResourcePath(Class, String)
	 * @see ClassLoader#getResources(String)
	 */
	public static Enumeration<URL> getClasspathResources(Class<?> scope, String resource) throws IOException {
		return getClassLoader(scope).getResources(getPackageResourcePath(scope, resource));
	}

	/**
	 * @return the full classpath for the resource in the same package as scope
	 */
	public static String getPackageResourcePath(Class<?> scope, String resource) {
		return scope.getPackage().getName().replace('.', '/') + '/' + resource;
	}

	/**
	 * @return an {@link Iterator} over the {@link Class} hierarchy
	 * 
	 * @see Class#getSuperclass()
	 */
	public static Iterator<Class<?>> hierarchy(Class<?> cls) {
		if (cls == null) {
			return IteratorUtils.empty();
		}

		class ClassHierarchyIterator implements Iterator<Class<?>> {

			private Class<?> _next;

			private ClassHierarchyIterator(Class<?> cls) {
				if (cls == null) {
					throw new NullPointerException("cls");
				}
				_next = cls;
			}

			@Override
			public boolean hasNext() {
				return _next != null;
			}

			@Override
			public Class<?> next() {
				if (!hasNext()) {
					throw new NoSuchElementException();
				}

				Class<?> next = _next;
				_next = _next.getSuperclass();
				return next;
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException("read-only");
			}
		}

		return new ClassHierarchyIterator(cls);
	}
}
