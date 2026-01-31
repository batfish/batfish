package org.batfish.vendor.cisco_nxos.representation;

import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/** An {@link ObjectGroup} of IP addresses. */
public final class ObjectGroupIpAddress extends ObjectGroup {

  public ObjectGroupIpAddress(String name) {
    super(name);
    _lines = new TreeMap<>();
  }

  @Override
  public <T> T accept(ObjectGroupVisitor<T> visitor) {
    return visitor.visitObjectGroupIpAddress(this);
  }

  public @Nonnull SortedMap<Long, ObjectGroupIpAddressLine> getLines() {
    return _lines;
  }

  private final @Nonnull SortedMap<Long, ObjectGroupIpAddressLine> _lines;
}
