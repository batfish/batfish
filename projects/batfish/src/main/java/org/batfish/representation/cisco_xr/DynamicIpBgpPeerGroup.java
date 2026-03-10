package org.batfish.representation.cisco_xr;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

public class DynamicIpBgpPeerGroup extends LeafBgpPeerGroup {

  private final @Nonnull Prefix _prefix;

  public DynamicIpBgpPeerGroup(@Nonnull Prefix prefix) {
    _prefix = prefix;
  }

  @Override
  public String getName() {
    return _prefix.toString();
  }

  @Override
  public Prefix getNeighborPrefix() {
    return _prefix;
  }

  @Override
  public @Nullable Prefix6 getNeighborPrefix6() {
    return null;
  }

  public @Nonnull Prefix getPrefix() {
    return _prefix;
  }
}
