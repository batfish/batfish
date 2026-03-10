package org.batfish.vendor.cisco_nxos.representation;

import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** An {@link ObjectGroup} of tcp or udp ports. */
public final class ObjectGroupIpPort extends ObjectGroup {

  public ObjectGroupIpPort(String name) {
    super(name);
    _lines = new TreeMap<>();
  }

  @Override
  public <T> T accept(ObjectGroupVisitor<T> visitor) {
    return visitor.visitObjectGroupIpPort(this);
  }

  public @Nonnull SortedMap<Long, ObjectGroupIpPortLine> getLines() {
    return _lines;
  }

  private final @Nonnull SortedMap<Long, ObjectGroupIpPortLine> _lines;
}
