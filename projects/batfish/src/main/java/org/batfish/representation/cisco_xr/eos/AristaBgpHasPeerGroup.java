package org.batfish.representation.cisco_xr.eos;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;

/** Interface for neighbors that can inherit settings from a peer group */
public interface AristaBgpHasPeerGroup {
  @Nullable
  public String getPeerGroup();

  public void setPeerGroup(@Nullable String peerGroup);

  public void inherit(AristaBgpProcess bgpGlobal, AristaBgpVrf bgpVrf, Warnings w);
}
