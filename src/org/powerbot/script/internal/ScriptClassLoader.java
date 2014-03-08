package org.powerbot.script.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.powerbot.util.IOUtils;
import org.powerbot.util.TarReader;

/**
 * @author Paris
 */
public final class ScriptClassLoader extends ClassLoader {
	private final URL base;
	private final Map<String, byte[]> files;

	public ScriptClassLoader(final URL base) {
		this.base = base;
		files = null;
	}

	public ScriptClassLoader(final TarReader in) {
		files = new HashMap<String, byte[]>();
		for (final Map.Entry<String, byte[]> e : in) {
			files.put(e.getKey(), e.getValue());
		}
		base = null;
	}

	@Override
	public Class<?> loadClass(final String name) throws ClassNotFoundException {
		final Class<?> clazz = findLoadedClass(name);
		if (clazz != null) {
			return clazz;
		}
		try {
			final String path = name.replace('.', '/') + ".class";
			final byte[] buf = base == null ? files.get(path) : IOUtils.read(getResourceAsStream(path));
			return defineClass(name, buf, 0, buf.length);
		} catch (final Exception ignored) {
			return super.loadClass(name);
		}
	}

	@Override
	public InputStream getResourceAsStream(final String name) {
		if (base == null) {
			return files.containsKey(name) ? new ByteArrayInputStream(files.get(name)) : null;
		}
		try {
			return new URL(base, name).openStream();
		} catch (final IOException ignored) {
			return null;
		}
	}
}

