package org.batfish.representation.cisco_asa;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

public class DynamicIpBgpPeerGroup extends LeafBgpPeerGroup {

  @Nonnull private final Prefix _prefix;

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

  @Nullable
  @Override
  public Prefix6 getNeighborPrefix6() {
    return null;
  }

  @Nonnull
  public Prefix getPrefix() {
    return _prefix;
  }
}
