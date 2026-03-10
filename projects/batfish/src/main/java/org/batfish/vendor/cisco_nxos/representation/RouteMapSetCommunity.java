package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;
import org.batfish.datamodel.bgp.community.StandardCommunity;

/** A {@link RouteMapSet} that sets or appends the route's community attribute. */
public final class RouteMapSetCommunity implements RouteMapSet {

  private boolean _additive;
  private @Nonnull List<StandardCommunity> _communities;

  public RouteMapSetCommunity(Iterable<StandardCommunity> communities, boolean additive) {
    _communities = ImmutableList.copyOf(communities);
    _additive = additive;
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetCommunity(this);
  }

  public boolean getAdditive() {
    return _additive;
  }

  public @Nonnull List<StandardCommunity> getCommunities() {
    return _communities;
  }

  public void setAdditive(boolean additive) {
    _additive = additive;
  }

  public void setCommunities(List<StandardCommunity> communities) {
    _communities = communities;
  }
}
