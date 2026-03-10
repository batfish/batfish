package org.batfish.vendor.arista.representation.eos;

import javax.annotation.Nullable;
import org.batfish.common.Warnings;

/** Interface for neighbors that can inherit settings from a peer group */
public interface AristaBgpHasPeerGroup {
  public @Nullable String getPeerGroup();

  public void setPeerGroup(@Nullable String peerGroup);

  public void inherit(AristaBgpProcess bgpGlobal, AristaBgpVrf bgpVrf, Warnings w);
}
