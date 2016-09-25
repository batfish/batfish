package org.batfish.common.plugin;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public abstract class PluginConsumer implements IPluginConsumer {

   private static final String CLASS_EXTENSION = ".class";

   /**
    * A byte-array containing the first 4 bytes of the header for a file that is
    * the output of java serialization
    */
   private static final byte[] JAVA_SERIALIZED_OBJECT_HEADER = { (byte) 0xac,
         (byte) 0xed, (byte) 0x00, (byte) 0x05 };

   private ClassLoader _currentClassLoader;

   private final List<Path> _pluginDirs;

   private final boolean _serializeToText;

   public PluginConsumer(boolean serializeToText, List<Path> pluginDirs) {
      _currentClassLoader = getClass().getClassLoader();
      _serializeToText = serializeToText;
      _pluginDirs = new ArrayList<>(pluginDirs);
      String questionPluginDirStr = System
            .getProperty(BfConsts.PROP_QUESTION_PLUGIN_DIR);
      // try to place question plugin first if system property is defined
      if (questionPluginDirStr != null) {
         Path questionPluginDir = Paths.get(questionPluginDirStr);
         if (_pluginDirs.isEmpty()
               || !_pluginDirs.get(0).equals(questionPluginDir)) {
            _pluginDirs.add(0, questionPluginDir);
         }
      }
      return;

   }

   public final Object deserializeObject(Path inputFile) {
      Object o = null;
      ObjectInputStream ois;
      try {
         byte[] data = Files.readAllBytes(inputFile);
         boolean isJavaSerializationData = isJavaSerializationData(data);
         ByteArrayInputStream bais = new ByteArrayInputStream(data);
         if (!isJavaSerializationData) {
            XStream xstream = new XStream(new DomDriver("UTF-8"));
            xstream.setClassLoader(_currentClassLoader);
            ois = xstream.createObjectInputStream(bais);
         }
         else {
            ois = new BatfishObjectInputStream(bais, _currentClassLoader);
         }
         o = ois.readObject();
         ois.close();
      }
      catch (IOException | ClassNotFoundException e) {
         throw new BatfishException("Failed to deserialize object from file: "
               + inputFile.toString(), e);
      }
      return o;
   }

   public Object deserializeObjectFromGzip(Path inputFile) {
      GZIPInputStream gis;
      FileInputStream fis;
      Object o = null;
      ObjectInputStream ois;
      try {
         fis = new FileInputStream(inputFile.toFile());
         gis = new GZIPInputStream(fis);
         byte[] data = IOUtils.toByteArray(gis);
         boolean isJavaSerializationData = isJavaSerializationData(data);
         ByteArrayInputStream bais = new ByteArrayInputStream(data);
         if (!isJavaSerializationData) {
            XStream xstream = new XStream(new DomDriver("UTF-8"));
            xstream.setClassLoader(_currentClassLoader);
            ois = xstream.createObjectInputStream(bais);
         }
         else {
            ois = new BatfishObjectInputStream(bais, _currentClassLoader);
         }
         o = ois.readObject();
         ois.close();
      }
      catch (IOException | ClassNotFoundException e) {
         throw new BatfishException("Failed to deserialize object from file: "
               + inputFile.toString(), e);
      }
      return o;
   }

   public ClassLoader getCurrentClassLoader() {
      return _currentClassLoader;
   }

   public abstract PluginClientType getType();

   private boolean isJavaSerializationData(byte[] fileBytes) {
      int headerLength = JAVA_SERIALIZED_OBJECT_HEADER.length;
      byte[] headerBytes = new byte[headerLength];
      for (int i = 0; i < headerLength; i++) {
         headerBytes[i] = fileBytes[i];
      }
      return Arrays.equals(headerBytes, JAVA_SERIALIZED_OBJECT_HEADER);
   }

   private void loadPluginJar(Path path) {
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
            URL[] urls = { new URL("jar:file:" + pathString + "!/") };
            URLClassLoader cl = URLClassLoader.newInstance(urls,
                  _currentClassLoader);
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
               String className = name
                     .substring(0, name.length() - CLASS_EXTENSION.length())
                     .replace("/", ".");
               try {
                  cl.loadClass(className);
                  Class<?> pluginClass = Class.forName(className, true, cl);
                  if (!Plugin.class.isAssignableFrom(pluginClass)
                        || Modifier.isAbstract(pluginClass.getModifiers())) {
                     continue;
                  }
                  Constructor<?> pluginConstructor;
                  try {
                     pluginConstructor = pluginClass.getConstructor();
                  }
                  catch (NoSuchMethodException | SecurityException e) {
                     throw new BatfishException(
                           "Could not find default constructor in plugin: '"
                                 + className + "'",
                           e);
                  }
                  Object pluginObj;
                  try {
                     pluginObj = pluginConstructor.newInstance();
                  }
                  catch (InstantiationException | IllegalAccessException
                        | IllegalArgumentException
                        | InvocationTargetException e) {
                     throw new BatfishException("Could not instantiate plugin '"
                           + className + "' from constructor", e);
                  }
                  Plugin plugin = (Plugin) pluginObj;
                  plugin.initialize(this);

               }
               catch (ClassNotFoundException e) {
                  jar.close();
                  throw new BatfishException(
                        "Unexpected error loading classes from jar", e);
               }
            }
            jar.close();
         }
         catch (IOException e) {
            throw new BatfishException(
                  "Error loading plugin jar: '" + path.toString() + "'", e);
         }
      }
   }

   protected final void loadPlugins() {
      for (Path pluginDir : _pluginDirs) {
         try {
            Files.walkFileTree(pluginDir, new SimpleFileVisitor<Path>() {
               @Override
               public FileVisitResult visitFile(Path path,
                     BasicFileAttributes attrs) throws IOException {
                  loadPluginJar(path);
                  return FileVisitResult.CONTINUE;
               }
            });
         }
         catch (IOException e) {
            throw new BatfishException("Error walking through plugin dir: '"
                  + pluginDir.toString() + "'", e);
         }
      }
   }

   public final void serializeObject(Object object, OutputStream outputStream) {
      try {
         ObjectOutputStream oos;
         if (_serializeToText) {
            XStream xstream = new XStream(new DomDriver("UTF-8"));
            oos = xstream.createObjectOutputStream(outputStream);
         }
         else {
            oos = new ObjectOutputStream(outputStream);
         }
         oos.writeObject(object);
         oos.close();
      }
      catch (IOException e) {
         throw new BatfishException(
               "Failed to serialize object to output stream", e);
      }
   }

   public final void serializeObject(Object object, Path outputFile) {
      FileOutputStream fos;
      try {
         fos = new FileOutputStream(outputFile.toFile());
         serializeObject(object, fos);
      }
      catch (IOException e) {
         throw new BatfishException(
               "Failed to serialize object to output file: "
                     + outputFile.toString(),
               e);
      }
   }

   public void serializeObjectToGzip(Object object, Path outputFile) {
      FileOutputStream fos;
      GZIPOutputStream gos;
      try {
         fos = new FileOutputStream(outputFile.toFile());
         gos = new GZIPOutputStream(fos);
         serializeObject(object, gos);
      }
      catch (IOException e) {
         throw new BatfishException(
               "Failed to serialize object to gzip output file: "
                     + outputFile.toString(),
               e);
      }
   }

}
