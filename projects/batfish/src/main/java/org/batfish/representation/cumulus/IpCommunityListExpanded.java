package org.batfish.representation.cumulus;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import org.batfish.datamodel.LineAction;
import org.batfish.datamodel.bgp.community.StandardCommunity;

/** A expanded Ip community list */
public class IpCommunityListExpanded extends IpCommunityList {

  private final LineAction _action;
  private final @Nonnull List<StandardCommunity> _communities;
  private final @Nonnull List<IpCommunityListExpandedLine> _lines;

  public IpCommunityListExpanded(
      String name, LineAction action, List<StandardCommunity> communities) {
    super(name);
    _action = action;
    _communities = ImmutableList.copyOf(communities);
    _lines = null;
  }

  public IpCommunityListExpanded(String name) {
    super(name);
    _action = null;
    _communities = null;
    _lines = new ArrayList<>();
  }

  @Override
  public <T> T accept(IpCommunityListVisitor<T> visitor) {
    return visitor.visitIpCommunityListExpanded(this);
  }

  public @Nonnull List<StandardCommunity> getCommunities() {
    return _communities;
  }

  public @Nonnull LineAction getAction() {
    return _action;
  }

  public @Nonnull List<IpCommunityListExpandedLine> getLines() {
    return _lines;
  }
}
