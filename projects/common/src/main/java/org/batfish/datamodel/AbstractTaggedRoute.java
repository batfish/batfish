package org.batfish.datamodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * A route that stores its tag inline. BGP routes, which rarely have a tag, keep it with their other
 * uncommon attributes instead.
 */
@ParametersAreNonnullByDefault
public abstract class AbstractTaggedRoute extends AbstractRoute {

  private final boolean _hasTag;
  // Stored as unsigned 32-bit int for memory savings. Use getTag() to read.
  private final int _tag;

  protected AbstractTaggedRoute(
      @Nullable Prefix network, long admin, long tag, boolean nonRouting, boolean nonForwarding) {
    super(network, admin, nonRouting, nonForwarding);
    checkTag(tag);
    _hasTag = tag != Route.UNSET_ROUTE_TAG;
    _tag = (int) tag;
  }

  @JsonProperty(PROP_TAG)
  @Override
  public final long getTag() {
    return _hasTag ? Integer.toUnsignedLong(_tag) : Route.UNSET_ROUTE_TAG;
  }
}
