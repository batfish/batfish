package org.batfish.common.plugin;

import com.google.common.collect.Lists;
import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;
import net.jpountz.lz4.LZ4FrameInputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;
import org.batfish.common.BatfishException;

public abstract class PluginConsumer implements IPluginConsumer {

  /** Supported formats we can deserialize from */
  public enum Format {
    JAVA_SERIALIZED,
    LZ4,
    GZIP,
    UNKNOWN
  }

  public static final int DEFAULT_HEADER_LENGTH_BYTES = 4;

  private static final int GZIP_HEADER_LENGTH_BYTES = 2;

  /**
   * A byte-array containing the first 4 bytes of the header for a file that is the output of java
   * serialization
   */
  private static final byte[] JAVA_SERIALIZED_OBJECT_HEADER = {
    (byte) 0xac, (byte) 0xed, (byte) 0x00, (byte) 0x05
  };

  private static final byte[] LZ4_MAGIC_BYTES = {
    (byte) 0x04, (byte) 0x22, (byte) 0x4D, (byte) 0x18
  };

  private static final byte[] GZIP_MAGIC_BYTES = {(byte) 0x1f, (byte) 0x8b};

  private ClassLoader _currentClassLoader;

  protected PluginConsumer() {
    _currentClassLoader = Thread.currentThread().getContextClassLoader();
  }

  private static <S extends Serializable> S deserializeObject(
      InputStream stream, Class<S> outputClass) {
    try {
      ObjectInputStream ois = new ObjectInputStream(stream);
      Object o = ois.readObject();
      return outputClass.cast(o);
    } catch (Exception e) {
      throw new BatfishException(
          "Failed to deserialize object of type '" + outputClass.getCanonicalName() + "' from data",
          e);
    }
  }

  /** Deserialize object from file, with support for different compression methods. */
  @SuppressWarnings("PMD.CloseResource") // PMD does not understand Closer
  protected static <S extends Serializable> S deserializeObject(
      Path inputFile, Class<S> outputClass) {
    try {
      // Awkward nested try blocks required because we refuse to throw IOExceptions.
      try (Closer closer = Closer.create()) {
        FileInputStream fis = closer.register(new FileInputStream(inputFile.toFile()));
        BufferedInputStream bis = closer.register(new BufferedInputStream(fis));
        // Allows us to peek at the beginning of the stream and then push the bytes back in for
        // downstream consumers to read.
        PushbackInputStream pbCompressed =
            new PushbackInputStream(bis, DEFAULT_HEADER_LENGTH_BYTES);
        Format f = detectFormat(pbCompressed);
        if (f == Format.GZIP) {
          InputStream gis = closer.register(new GZIPInputStream(pbCompressed));
          // Update format after decompression
          PushbackInputStream pbUncompressed =
              new PushbackInputStream(gis, DEFAULT_HEADER_LENGTH_BYTES);
          return deserializeObject(pbUncompressed, outputClass);
        } else if (f == Format.LZ4) {
          InputStream lis = closer.register(new LZ4FrameInputStream(pbCompressed));
          // Update format after decompression
          PushbackInputStream pbUncompressed =
              new PushbackInputStream(lis, DEFAULT_HEADER_LENGTH_BYTES);
          return deserializeObject(pbUncompressed, outputClass);
        } else {
          return deserializeObject(pbCompressed, outputClass);
        }
      }
    } catch (Exception e) {
      throw new BatfishException(
          String.format(
              "Failed to deserialize object of type %s from file %s",
              outputClass.getCanonicalName(), inputFile),
          e);
    }
  }

  /**
   * Determines the format of stream data. Requires a {@link PushbackInputStream} so that the
   * inspected bytes can be put back into the stream after reading.
   *
   * @return a {@link Format} value.
   */
  public static Format detectFormat(PushbackInputStream stream) throws IOException {
    byte[] header = new byte[DEFAULT_HEADER_LENGTH_BYTES];
    ByteStreams.readFully(stream, header);
    Format format;
    if (Arrays.equals(header, JAVA_SERIALIZED_OBJECT_HEADER)) {
      format = Format.JAVA_SERIALIZED;
    } else if (Arrays.equals(header, LZ4_MAGIC_BYTES)) {
      format = Format.LZ4;
    } else if (Arrays.equals(Arrays.copyOf(header, GZIP_HEADER_LENGTH_BYTES), GZIP_MAGIC_BYTES)) {
      format = Format.GZIP;
    } else {
      format = Format.UNKNOWN;
    }
    stream.unread(header);
    return format;
  }

  public abstract PluginClientType getType();

  protected final void loadPlugins() {
    SortedSet<Plugin> plugins;
    try {
      plugins =
          new TreeSet<>(Lists.newArrayList(ServiceLoader.load(Plugin.class, _currentClassLoader)));
    } catch (ServiceConfigurationError e) {
      throw new BatfishException("Failed to locate and/or instantiate plugins", e);
    }
    List<BatfishException> initializationExceptions = new ArrayList<>();
    for (Plugin plugin : plugins) {
      try {
        plugin.initialize(this);
      } catch (Exception e) {
        initializationExceptions.add(
            new BatfishException(
                "Failed to initialize plugin: " + plugin.getClass().getCanonicalName(), e));
      }
    }
    if (!initializationExceptions.isEmpty()) {
      BatfishException e = new BatfishException("Failed to initialize one or more plugins");
      initializationExceptions.forEach(e::addSuppressed);
      throw e;
    }
  }

  /** Serializes the given object to a file with the given output name. */
  @SuppressWarnings("PMD.CloseResource") // PMD does not understand Closer
  protected static void serializeObject(Serializable object, Path outputFile) {
    try {
      try (Closer closer = Closer.create()) {
        OutputStream out = closer.register(Files.newOutputStream(outputFile));
        BufferedOutputStream bout = closer.register(new BufferedOutputStream(out));
        serializeToLz4Data(object, bout);
      }
    } catch (Exception e) {
      throw new BatfishException("Failed to serialize object to output file: " + outputFile, e);
    }
  }

  /** Serializes the given object to the given stream, using LZ4 compression. */
  @SuppressWarnings("PMD.CloseResource") // PMD does not understand Closer.
  private static void serializeToLz4Data(Serializable object, OutputStream out) {
    try (Closer closer = Closer.create()) {
      OutputStream los = closer.register(new LZ4FrameOutputStream(out));
      ObjectOutputStream oos = closer.register(new ObjectOutputStream(los));
      oos.writeObject(object);
    } catch (Throwable e) {
      throw new BatfishException("Failed to convert object to LZ4 data", e);
    }
  }
}
