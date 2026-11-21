package org.batfish.representation.frr;

import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;

/**
 * An access-list that matches a route's standard communities attribute against a set of literal
 * standard communities.
 */
public final class BgpCommunityListStandard extends BgpCommunityList {

  private final @Nonnull SortedMap<Long, BgpCommunityListStandardLine> _lines;

  public BgpCommunityListStandard(String name) {
    super(name);
    _lines = new TreeMap<>();
  }

  @Override
  public <T> T accept(BgpCommunityListVisitor<T> visitor) {
    return visitor.visitBgpCommunityListStandard(this);
  }

  /** The lines of this list, by sequence number. */
  public @Nonnull SortedMap<Long, BgpCommunityListStandardLine> getLines() {
    return _lines;
  }
}
