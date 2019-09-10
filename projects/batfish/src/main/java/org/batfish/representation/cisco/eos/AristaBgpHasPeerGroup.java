package org.batfish.representation.cisco.eos;

import java.util.Map;
import javax.annotation.Nullable;

/** Interface for neighbors that can inherit settings from a peer group */
public interface AristaBgpHasPeerGroup {
  @Nullable
  public String getPeerGroup();

  public void setPeerGroup(@Nullable String peerGroup);

  public void inherit(Map<String, AristaBgpPeerGroupNeighbor> peerGroups);
}
