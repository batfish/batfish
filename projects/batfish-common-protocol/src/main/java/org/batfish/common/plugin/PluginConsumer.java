package org.batfish.common.plugin;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.apache.commons.io.IOUtils;
import org.batfish.common.BatfishException;
import org.batfish.common.BfConsts;
import org.batfish.common.util.BatfishObjectInputStream;

public abstract class PluginConsumer implements IPluginConsumer {

  private static final String CLASS_EXTENSION = ".class";

  /**
   * A byte-array containing the first 4 bytes of the header for a file that is the output of java
   * serialization
   */
  private static final byte[] JAVA_SERIALIZED_OBJECT_HEADER = {
    (byte) 0xac, (byte) 0xed, (byte) 0x00, (byte) 0x05
  };

  private ClassLoader _currentClassLoader;

  private final List<Path> _pluginDirs;

  private final boolean _serializeToText;

  public PluginConsumer(boolean serializeToText, List<Path> pluginDirs) {
    // _currentClassLoader = getClass().getClassLoader();
    _currentClassLoader = Thread.currentThread().getContextClassLoader();
    _serializeToText = serializeToText;
    _pluginDirs = new ArrayList<>(pluginDirs);
    String questionPluginDirStr = System.getProperty(BfConsts.PROP_QUESTION_PLUGIN_DIR);
    // try to place question plugin first if system property is defined
    if (questionPluginDirStr != null) {
      Path questionPluginDir = Paths.get(questionPluginDirStr);
      if (_pluginDirs.isEmpty() || !_pluginDirs.get(0).equals(questionPluginDir)) {
        _pluginDirs.add(0, questionPluginDir);
      }
    }
  }

  protected <S extends Serializable> S deserializeObject(byte[] data, Class<S> outputClass) {
    ByteArrayInputStream stream = new ByteArrayInputStream(data);
    return deserializeObject(stream, outputClass);
  }

  protected <S extends Serializable> S deserializeObject(InputStream stream, Class<S> outputClass) {
    try {
      // Allows us to peek at the beginning of the stream and then push the bytes back in for
      // downstream consumers to read.
      PushbackInputStream pbstream =
          new PushbackInputStream(stream, JAVA_SERIALIZED_OBJECT_HEADER.length);
      boolean isJavaSerializationData = isJavaSerializationData(pbstream);
      ObjectInputStream ois;
      if (!isJavaSerializationData) {
        XStream xstream = new XStream(new DomDriver("UTF-8"));
        xstream.setClassLoader(_currentClassLoader);
        ois = xstream.createObjectInputStream(pbstream);
      } else {
        ois = new BatfishObjectInputStream(pbstream, _currentClassLoader);
      }
      Object o = ois.readObject();
      return outputClass.cast(o);
    } catch (IOException | ClassNotFoundException | ClassCastException e) {
      throw new BatfishException(
          "Failed to deserialize object of type '" + outputClass.getCanonicalName() + "' from data",
          e);
    }
  }

  protected <S extends Serializable> S deserializeObject(Path inputFile, Class<S> outputClass) {
    try {
      // Awkward nested try blocks required because we refuse to throw IOExceptions.
      try (Closer closer = Closer.create()) {
        FileInputStream fis = closer.register(new FileInputStream(inputFile.toFile()));
        GZIPInputStream gis = closer.register(new GZIPInputStream(fis));
        return deserializeObject(gis, outputClass);
      }
    } catch (IOException e) {
      throw new BatfishException(
          String.format(
              "Failed to deserialize object of type %s from file %s",
              outputClass.getCanonicalName(), inputFile),
          e);
    }
  }

  protected byte[] fromGzipFile(Path inputFile) {
    try {
      // Awkward nested try blocks required because we refuse to throw IOExceptions.
      try (Closer closer = Closer.create()) {
        FileInputStream fis = closer.register(new FileInputStream(inputFile.toFile()));
        GZIPInputStream gis = closer.register(new GZIPInputStream(fis));
        return IOUtils.toByteArray(gis);
      }
    } catch (IOException e) {
      throw new BatfishException("Failed to gunzip file: " + inputFile, e);
    }
  }

  public ClassLoader getCurrentClassLoader() {
    return _currentClassLoader;
  }

  public abstract PluginClientType getType();

  /**
   * Determines whether the data in the stream is Java serialized bytes. Requires a {@link
   * PushbackInputStream} so that the inspected bytes can be put back into the stream after reading.
   */
  private boolean isJavaSerializationData(PushbackInputStream stream) throws IOException {
    byte[] header = new byte[JAVA_SERIALIZED_OBJECT_HEADER.length];
    ByteStreams.readFully(stream, header);
    boolean ret = Arrays.equals(header, JAVA_SERIALIZED_OBJECT_HEADER);
    stream.unread(header);
    return ret;
  }

  private boolean loadPluginJar(Path path, String searchPkg) {
    /*
     * Adapted from
     * http://stackoverflow.com/questions/11016092/how-to-load-classes-at-
     * runtime-from-a-folder-or-jar Retrieved: 2016-08-31 Original Authors:
     * Kevin Crain http://stackoverflow.com/users/2688755/kevin-crain
     * Apfelsaft http://stackoverflow.com/users/1447641/apfelsaft License:
     * https://creativecommons.org/licenses/by-sa/3.0/
     */
    String pathString = path.toString();
    if (pathString.endsWith(".jar")) {
      try {
        URL[] urls = {new URL("jar:file:" + pathString + "!/")};
        URLClassLoader cl = URLClassLoader.newInstance(urls, _currentClassLoader);
        _currentClassLoader = cl;
        Thread.currentThread().setContextClassLoader(cl);
        JarFile jar = new JarFile(path.toFile());
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
          JarEntry element = entries.nextElement();
          String name = element.getName();
          if (element.isDirectory() || !name.endsWith(CLASS_EXTENSION)) {
            continue;
          }
          String className =
              name.substring(0, name.length() - CLASS_EXTENSION.length()).replace("/", ".");
          try {
            loadPluginClass(cl, className, searchPkg);
          } catch (ClassNotFoundException e) {
            jar.close();
            throw new BatfishException("Unexpected error loading classes from jar", e);
          }
        }
        jar.close();
        return true;
      } catch (IOException e) {
        throw new BatfishException("Error loading plugin jar: '" + path + "'", e);
      }
    }

    return false;
  }

  private void loadPluginFolder(Path pluginPath, String searchPkg) throws IOException {
    URL[] urls = {pluginPath.toUri().toURL()};
    final URLClassLoader cl = URLClassLoader.newInstance(urls);
    _currentClassLoader = cl;
    Thread.currentThread().setContextClassLoader(cl);

    final int baseLen = pluginPath.toString().length() + 1;

    Files.walkFileTree(
        pluginPath,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path path, BasicFileAttributes attrs)
              throws IOException {
            String name = path.toString();
            if (name.endsWith(CLASS_EXTENSION)) {
              String className =
                  name.substring(baseLen, name.length() - CLASS_EXTENSION.length())
                      .replace(File.separatorChar, '.');
              try {
                loadPluginClass(cl, className, searchPkg);
              } catch (ClassNotFoundException e) {
                throw new BatfishException("Unexpected error loading from folder " + path, e);
              }
            }
            return FileVisitResult.CONTINUE;
          }
        });
  }

  private void loadPluginClass(URLClassLoader cl, String className,
                               String searchPkg) throws ClassNotFoundException {
    if (!className.startsWith(searchPkg)) {
      return;
    }
    Class<?> pluginClass = null;
    try {
      cl.loadClass(className);
      pluginClass = Class.forName(className, true, cl);
    } catch (ClassNotFoundException | NoClassDefFoundError e) {
      // Ignoring this exception is a potentially dangerous hack
      // but I don't quite know yet why some classes cannot be loaded and how to do things cleanly
      getLogger().warn("Couldn't load class " + className + ". Skipping.\n");
      return;
    }
    if (!Plugin.class.isAssignableFrom(pluginClass)
        || Modifier.isAbstract(pluginClass.getModifiers())) {
      return;
    }
    Constructor<?> pluginConstructor;
    try {
      pluginConstructor = pluginClass.getConstructor();
    } catch (NoSuchMethodException | SecurityException e) {
      throw new BatfishException(
          "Could not find default constructor in plugin: '" + className + "'", e);
    }
    Object pluginObj;
    try {
      pluginObj = pluginConstructor.newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | IllegalArgumentException
        | InvocationTargetException e) {
      throw new BatfishException(
          "Could not instantiate plugin '" + className + "' from constructor", e);
    }
    Plugin plugin = (Plugin) pluginObj;
    plugin.initialize(this);
  }

  private class JarVisitor extends SimpleFileVisitor<Path> {
    boolean _hasJars = false;
    boolean _hasClasses = false;
    String _searchPkg = "";

    JarVisitor(String searchPkg) {
      _searchPkg = searchPkg;
    }

    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
      if (loadPluginJar(path, _searchPkg)) {
        _hasJars = true;
      } else if (path.toString().endsWith(CLASS_EXTENSION)) {
        _hasClasses = true;
      }
      return FileVisitResult.CONTINUE;
    }
  }

  protected final void loadPlugins() {
    // this supports loading plugins either from a directory of jars, or
    // from a folder of classfiles (useful for eclipse development)
    for (Path pluginDir : _pluginDirs) {
      // some jars have annoying dependencies and take a long time to scan,
      // thus we allow restricting loading from only a particular package by appending
      // !PACKAGE.NAME to the end of the search path
      String searchPkg = "";
      String dirStr = pluginDir.toString();
      int bang = dirStr.lastIndexOf('!');
      if (bang >= 0) {
        searchPkg = dirStr.substring(bang + 1);
        pluginDir = Paths.get(dirStr.substring(0, bang));
      }
      if (Files.exists(pluginDir)) {
        JarVisitor jarVisitor = new JarVisitor(searchPkg);
        try {
          // first load any jars
          Files.walkFileTree(pluginDir, jarVisitor);
        } catch (IOException e) {
          throw new BatfishException("Error walking through plugin dir: '" + pluginDir + "'", e);
        }

        // if there are class files and no jars, then try to load as a folder
        if (jarVisitor._hasClasses && !jarVisitor._hasJars) {
          try {
            loadPluginFolder(pluginDir, searchPkg);
          } catch (IOException e) {
            throw new BatfishException(
                "Error loading plugin folder: '" + pluginDir.toString() + "'", e);
          }
        }
      }
    }
  }

  /** Serializes the given object to a file with the given output name, using GZIP compression. */
  public void serializeObject(Serializable object, Path outputFile) {
    try {
      try (OutputStream out = Files.newOutputStream(outputFile)) {
        serializeToGzipData(object, out);
      }
    } catch (IOException e) {
      throw new BatfishException(
          "Failed to serialize object to gzip output file: " + outputFile, e);
    }
  }

  /** Serializes the given object to a GZIP-compressed byte[]. */
  protected byte[] toGzipData(Serializable object) {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    serializeToGzipData(object, baos);
    return baos.toByteArray();
  }

  private static class CloseIgnoringOutputStream extends FilterOutputStream {

    protected CloseIgnoringOutputStream(OutputStream out) {
      super(out);
    }

    /** Does nothing, deliberately. */
    @Override
    public void close() {}
  }

  /** Serializes the given object to the given stream, using GZIP compression. */
  private void serializeToGzipData(Serializable object, OutputStream out) {
    // This is a hack:
    //   XStream requires that its streams be closed to properly finish serialization,
    //   but we do not actually want to close the passed-in output stream.
    out = new CloseIgnoringOutputStream(out);

    try (Closer closer = Closer.create()) {
      GZIPOutputStream gos = closer.register(new GZIPOutputStream(out));
      ObjectOutputStream oos;
      if (_serializeToText) {
        XStream xstream = new XStream(new DomDriver("UTF-8"));
        oos = closer.register(xstream.createObjectOutputStream(gos));
      } else {
        oos = closer.register(new ObjectOutputStream(gos));
      }
      oos.writeObject(object);
    } catch (IOException e) {
      throw new BatfishException("Failed to convert object to gzip data", e);
    }
  }
}
