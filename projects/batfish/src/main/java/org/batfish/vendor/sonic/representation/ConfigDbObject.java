package org.batfish.vendor.sonic.representation;

import java.io.Serializable;

/**
 * Represents a top-level object in configdb.json file.
 *
 * <p>Implementations must override equals and hashCode methods, which are used by the container
 * class {@link org.batfish.vendor.sonic.representation.ConfigDb.Data} to determine equality.
 */
public interface ConfigDbObject extends Serializable {
  enum Type {
    DEVICE_METADATA,
    INTERFACE,
    LOOPBACK,
    NTP_SERVER,
    PORT,
    SYSLOG_SERVER
  }
}
