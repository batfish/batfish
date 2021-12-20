package org.batfish.representation.frr;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * An access-list that matches a route's standard communities attribute against a set of literal
 * standard communities.
 */
public final class BgpCommunityListStandard extends BgpCommunityList {

  private final @Nonnull List<BgpCommunityListStandardLine> _lines;

  public BgpCommunityListStandard(String name) {
    super(name);
    _lines = new ArrayList<BgpCommunityListStandardLine>();
  }

  @Override
  public <T> T accept(BgpCommunityListVisitor<T> visitor) {
    return visitor.visitBgpCommunityListStandard(this);
  }

  public @Nonnull List<BgpCommunityListStandardLine> getLines() {
    return _lines;
  }
}
