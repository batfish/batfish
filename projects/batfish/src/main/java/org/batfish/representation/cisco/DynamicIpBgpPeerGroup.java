package org.batfish.representation.cisco;

import javax.annotation.Nullable;
import org.batfish.datamodel.Prefix;
import org.batfish.datamodel.Prefix6;

public class DynamicIpBgpPeerGroup extends LeafBgpPeerGroup {

  /** */
  private static final long serialVersionUID = 1L;

  private Prefix _prefix;

  public DynamicIpBgpPeerGroup(Prefix prefix) {
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

  public Prefix getPrefix() {
    return _prefix;
  }
}
