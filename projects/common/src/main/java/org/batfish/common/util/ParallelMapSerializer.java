package org.batfish.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Java serialization of a large map one entry at a time, in parallel. Default serialization writes
 * everything through one {@link ObjectOutputStream} on one thread; here each value is serialized to
 * its own byte array, in parallel, and the arrays are written in the map's iteration order. Reading
 * does the reverse. This suits maps whose values share nothing but strings, such as per-device
 * data: an object referenced from two values is written into both, and comes back as two objects.
 */
@ParametersAreNonnullByDefault
public final class ParallelMapSerializer {

  /** Writes {@code byKey} so that {@link #readMap} can read it back. */
  public static void writeMap(ObjectOutputStream out, Map<String, ?> byKey) throws IOException {
    List<byte[]> chunks =
        byKey.values().parallelStream()
            .map(
                value -> {
                  ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                  try (ObjectOutputStream chunk = new ObjectOutputStream(bytes)) {
                    chunk.writeObject(value);
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                  return bytes.toByteArray();
                })
            .collect(Collectors.toList());
    out.writeInt(chunks.size());
    int i = 0;
    for (String key : byKey.keySet()) {
      out.writeUTF(key);
      byte[] chunk = chunks.get(i++);
      out.writeInt(chunk.length);
      out.write(chunk);
    }
  }

  /** Reads a map written by {@link #writeMap}, with the entries in the order written. */
  @SuppressWarnings("unchecked")
  public static <V> Map<String, V> readMap(ObjectInputStream in) throws IOException {
    int count = in.readInt();
    List<String> keys = new ArrayList<>(count);
    List<byte[]> chunks = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      keys.add(in.readUTF());
      byte[] chunk = new byte[in.readInt()];
      in.readFully(chunk);
      chunks.add(chunk);
    }
    List<V> values =
        chunks.parallelStream()
            .map(
                chunk -> {
                  try (ObjectInputStream chunkIn =
                      new ObjectInputStream(new ByteArrayInputStream(chunk))) {
                    return (V) chunkIn.readObject();
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  } catch (ClassNotFoundException e) {
                    throw new IllegalStateException(e);
                  }
                })
            .collect(Collectors.toList());
    Map<String, V> result = new LinkedHashMap<>(count * 2);
    for (int i = 0; i < count; i++) {
      result.put(keys.get(i), values.get(i));
    }
    return result;
  }

  private ParallelMapSerializer() {}
}
