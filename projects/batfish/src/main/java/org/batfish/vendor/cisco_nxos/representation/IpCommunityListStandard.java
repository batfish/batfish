package org.batfish.vendor.cisco_nxos.representation;

import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/**
 * An access-list that matches a route's standard communities attribute against a set of literal
 * standard communities.
 */
public final class IpCommunityListStandard extends IpCommunityList {

  private final @Nonnull SortedMap<Long, IpCommunityListStandardLine> _lines;

  public IpCommunityListStandard(String name) {
    super(name);
    _lines = new TreeMap<>();
  }

  @Override
  public <T> T accept(IpCommunityListVisitor<T> visitor) {
    return visitor.visitIpCommunityListStandard(this);
  }

  public @Nonnull SortedMap<Long, IpCommunityListStandardLine> getLines() {
    return _lines;
  }
}
