package org.batfish.vendor.cisco_nxos.representation;

import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * A {@link RouteMapSetAsPathPrepend} that prepends a fixed list of ASes to the route's as-path
 * attribute.
 */
public final class RouteMapSetAsPathPrependLiteralAs implements RouteMapSetAsPathPrepend {

  private final @Nonnull List<Long> _asNumbers;

  public RouteMapSetAsPathPrependLiteralAs(Iterable<Long> asNumbers) {
    _asNumbers = ImmutableList.copyOf(asNumbers);
  }

  @Override
  public <T> T accept(RouteMapSetVisitor<T> visitor) {
    return visitor.visitRouteMapSetAsPathPrependLiteralAs(this);
  }

  public @Nonnull List<Long> getAsNumbers() {
    return _asNumbers;
  }
}
