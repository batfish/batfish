package org.batfish.vendor.cisco_nxos.representation;

import java.io.Serializable;
import javax.annotation.Nonnull;
import org.batfish.datamodel.IntegerSpace;

/** A line of an {@link ObjectGroupIpPort}. */
public final class ObjectGroupIpPortLine implements Serializable {

  public ObjectGroupIpPortLine(long line, IntegerSpace ports) {
    _line = line;
    _ports = ports;
  }

  public @Nonnull IntegerSpace getPorts() {
    return _ports;
  }

  public long getLine() {
    return _line;
  }

  private final @Nonnull IntegerSpace _ports;
  private final long _line;
}
