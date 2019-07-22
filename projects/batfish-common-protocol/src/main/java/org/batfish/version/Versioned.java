package org.batfish.version;

import com.google.common.collect.ImmutableSortedMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import org.batfish.common.BatfishException;

/** Interface for versioned objects */
public interface Versioned {
  /** Version string for an unknown version. */
  String UNKNOWN_VERSION = "0.0.0";

  /** Returns the name of the versioned object. Should be globally unique. */
  String getName();

  /** Returns the version of the versioned object corresponding to the name. */
  String getVersion();

  /** Returns a string representation of all versioned objects. */
  static String getVersionsString() {
    StringBuilder sb = new StringBuilder();
    for (Entry<String, String> entry : getVersions().entrySet()) {
      sb.append(entry.getKey());
      sb.append(" version: ");
      sb.append(entry.getValue());
      sb.append("\n");
    }
    return sb.toString();
  }

  static Map<String, String> getVersions() {
    HashMap<String, String> ret = new HashMap<>();
    try {

      for (Versioned v : ServiceLoader.load(Versioned.class, ClassLoader.getSystemClassLoader())) {
        String previous = ret.putIfAbsent(v.getName(), v.getVersion());
        if (previous != null) {
          throw new IllegalArgumentException(
              String.format("%s already has a registered version.", v.getName()));
        }
      }
      return ImmutableSortedMap.copyOf(ret);
    } catch (ServiceConfigurationError e) {
      throw new BatfishException("Failed to load Versioned object", e);
    }
  }
}
