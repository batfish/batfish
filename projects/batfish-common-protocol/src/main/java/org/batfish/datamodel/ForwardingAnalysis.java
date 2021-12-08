package org.batfish.datamodel;

import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;

public interface ForwardingAnalysis {
  /** Mapping: hostname -&gt; inInterface -&gt; ipsToArpReplyTo */
  Map<String, Map<String, IpSpace>> getArpReplies();

  /**
   * Return the forwarding behavior for each VRF. Mapping: hostname -&gt; vrfName -&gt; {@link
   * VrfForwardingBehavior}.
   */
  @Nonnull
  Map<String, Map<String, VrfForwardingBehavior>> getVrfForwardingBehavior();

  /**
   * Find the remote VRFs that could receive traffic from {@code currentNodeName} routed via the
   * given {@code vtepIp} and {@code vni}. Return all such VRFs as a mapping of hostname -&gt; vrf.
   */
  @Nonnull
  Map<String, Set<String>> getVxlanNeighbors(String currentNodeName, Ip vtepIp, int vni);
}
