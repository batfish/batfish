package org.batfish.representation.cumulus_nclu;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * An access-list that matches a route's standard communities attribute against a set of literal
 * standard communities.
 */
public final class IpCommunityListStandard extends IpCommunityList {

  private final @Nonnull List<IpCommunityListStandardLine> _lines;

  public IpCommunityListStandard(String name) {
    super(name);
    _lines = new ArrayList<IpCommunityListStandardLine>();
  }

  @Override
  public <T> T accept(IpCommunityListVisitor<T> visitor) {
    return visitor.visitIpCommunityListStandard(this);
  }

  public @Nonnull List<IpCommunityListStandardLine> getLines() {
    return _lines;
  }
}
